package com.mikore.ppqr.activity

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.applyCanvas
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.mikore.ppqr.AppConst
import com.mikore.ppqr.BuildConfig
import com.mikore.ppqr.R
import com.mikore.ppqr.database.AppHistory
import com.mikore.ppqr.database.AppRepo
import com.mikore.ppqr.fragment.HistoryFragment
import com.mikore.ppqr.utility.PromptPayUtil
import com.mikore.ppqr.utility.Utils
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.result_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

class ResultActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private val requestCode = 8641
    private val nameView by lazy { findViewById<TextView>(R.id.textView7) }
    private val amountView by lazy { findViewById<TextView>(R.id.textView8) }
    private val descView by lazy { findViewById<TextView>(R.id.textView9) }
    private val qrView by lazy { findViewById<ImageView>(R.id.imageView2) }
    private val qrRoot by lazy { findViewById<MaterialCardView>(R.id.qr_root) }
    private val bnv by lazy { findViewById<BottomNavigationView>(R.id.rs_bnv) }
    private val promptPay = PromptPayUtil()

    @Inject
    lateinit var appRepo: AppRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.result_layout)
        setSupportActionBar(result_toolbar)

        val saveHistory = intent.getBooleanExtra(AppConst.ARGS_SAVE_HISTORY_KEY, false)
        val uid = intent.getStringExtra(AppConst.ARGS_ACCOUNT_ID_KEY)
        val amount = intent.getStringExtra(AppConst.ARGS_AMOUNT_KEY)
        val desc = intent.getStringExtra(AppConst.ARGS_DESCRIPTION_KEY)

        if (uid == null) {
            finish()
            return
        }

        val params = qrView.layoutParams as ConstraintLayout.LayoutParams
        params.width = getSize()
        params.height = getSize()
        qrView.layoutParams = params


        val amountText = "Amount: " + if (amount == null) {
            "Not specified"
        } else {
            "$amount Baht."
        }
        amountView.text = amountText
        val descText = "Note: " + (desc ?: "Not specified")
        descView.text = descText

        MainScope().launch {
            val account = withContext(Dispatchers.IO) {
                appRepo.getAccount(uid)
            }
            nameView.text = if (account.name.isNullOrEmpty()) {
                account.no
            } else {
                "${account.name} (${account.no})"
            }
            val data = promptPay.generateQRData(account.no, amount)
            val qrg = QRGEncoder(data, null, QRGContents.Type.TEXT, getSize())
            qrView.setImageBitmap(qrg.bitmap)
            if (saveHistory) {
                appRepo.saveHistory(
                    AppHistory(
                        accountId = account.uid,
                        description = desc,
                        amount = amount
                    )
                )
                Intent().also {
                    it.action = HistoryFragment.REFRESH_FILTER
                    sendBroadcast(it)
                }
            }
        }

        bnv.setOnNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.rs_close -> finish()
            R.id.rs_save -> {
                storeImage()
                showToast("Saved successfully.", Toast.LENGTH_SHORT)
            }
            R.id.rs_share -> {
                val it = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, storeImage())
                }
                startActivity(Intent.createChooser(it, "Share Image"))
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.requestCode) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    showToast("Please allow permission for do this action")
                    if (BuildConfig.DEBUG) {
                        showToast(permissions[i])
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun storeImage(): Uri? {
        try {
            val image = exportToBitmap(qrRoot)
            val dist = File(Environment.DIRECTORY_PICTURES, getString(R.string.app_name))
            val date = System.currentTimeMillis()
            val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-", Locale.ENGLISH)
                .format(date) + getString(R.string.app_name).replace(" ", "-")
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$name.jpeg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.DATE_ADDED, date)
                put(MediaStore.Images.Media.DATE_MODIFIED, date)
                put(MediaStore.Images.Media.SIZE, image.byteCount)
                put(MediaStore.Images.Media.WIDTH, image.width)
                put(MediaStore.Images.Media.HEIGHT, image.height)
            }
            var collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            if (Build.VERSION.SDK_INT >= 29) {
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "$dist${File.separator}")
                values.put(MediaStore.Images.Media.IS_PENDING, 1)
                collection =
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }
            val insertUri = contentResolver.insert(collection, values)
            contentResolver.openOutputStream(insertUri!!, "w").use {
                image.compress(Bitmap.CompressFormat.JPEG, 80, it)
            }
            values.clear()
            if (Build.VERSION.SDK_INT >= 29) {
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            contentResolver.update(insertUri, values, null, null)
            return insertUri
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                showToast(Log.getStackTraceString(e))
            }
            return null
        }
    }

    private fun showToast(message: String, length: Int = Toast.LENGTH_LONG) {
        Toast.makeText(baseContext, message, length).show()
    }

    private fun exportToBitmap(v: View): Bitmap {
        val rootView = window.decorView.rootView
        val bmp = Bitmap.createBitmap(
            Utils.screenWidth(windowManager),
            Utils.screenHeight(windowManager),
            Bitmap.Config.ARGB_8888
        )
            .applyCanvas {
                rootView.layout(0, 0, width, height)
                translate(-rootView.scrollX.toFloat(), -rootView.scrollY.toFloat())
                rootView.draw(this)
            }
        val location = IntArray(2)
        v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        v.getLocationOnScreen(location)
        return Bitmap.createBitmap(
            bmp,
            location[0] + toPx(6),
            location[1] + toPx(6),
            bmp.width - (location[0] * 2) - toPx(12),
            v.measuredHeight - toPx(12)
        )
    }

    private fun toPx(dp: Int): Int =
        (dp * (resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()


    private fun getSize(): Int {
        val width = Utils.screenWidth(windowManager)
        val height = Utils.screenHeight(windowManager)
        return if (width < height) {
            (width * 0.7).roundToInt()
        } else {
            (height * 0.7).roundToInt()
        }
    }
}
