/*
 * Copyright (C) 2021 Software with Kao.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mikore.ppqr.activity

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.applyCanvas
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mikore.ppqr.AppConst
import com.mikore.ppqr.BuildConfig
import com.mikore.ppqr.R
import com.mikore.ppqr.database.AppHistory
import com.mikore.ppqr.database.AppRepo
import com.mikore.ppqr.fragment.HistoryFragment
import com.mikore.ppqr.utility.PromptPayUtil
import dagger.android.AndroidInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ResultActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private val requestCode = 8641
    private val nameView by lazy { findViewById<TextView>(R.id.textView7) }
    private val amountView by lazy { findViewById<TextView>(R.id.textView8) }
    private val descView by lazy { findViewById<TextView>(R.id.textView9) }
    private val qrView by lazy { findViewById<ImageView>(R.id.imageView2) }
    private val bnv by lazy { findViewById<BottomNavigationView>(R.id.rs_bnv) }
    private val promptPay = PromptPayUtil()

    private lateinit var qrBitmap: Bitmap
    private lateinit var titleText: String
    private lateinit var descText: String
    private lateinit var amountText: String

    @Inject
    lateinit var appRepo: AppRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.result_layout)
        val toolbar = findViewById<Toolbar>(R.id.result_toolbar)
        setSupportActionBar(toolbar)

        val saveHistory = intent.getBooleanExtra(AppConst.ARGS_SAVE_HISTORY_KEY, false)
        val uid = intent.getStringExtra(AppConst.ARGS_ACCOUNT_ID_KEY)
        val amount = intent.getStringExtra(AppConst.ARGS_AMOUNT_KEY)
        val desc = intent.getStringExtra(AppConst.ARGS_DESCRIPTION_KEY)

        if (uid == null) {
            finish()
            return
        }

        val params = qrView.layoutParams as ConstraintLayout.LayoutParams
        params.width = 300
        params.height = 300
        qrView.layoutParams = params

        amountText = "Amount: " + if (amount == null) {
            "Not specified"
        } else {
            "$amount Baht."
        }
        amountView.text = amountText
        descText = "Note: " + (desc ?: "Not specified")
        descView.text = descText

        MainScope().launch {
            val account = withContext(Dispatchers.IO) {
                appRepo.getAccount(uid)
            }
            titleText = account.no
            if (!account.name.isNullOrEmpty()) {
                titleText += " (${account.name})"
            }
            nameView.text = titleText
            val data = promptPay.generateQRData(account.no, amount)
            val qrg = QRGEncoder(data, null, QRGContents.Type.TEXT, 300)
            qrBitmap = qrg.bitmap
            qrView.setImageBitmap(qrBitmap)
            if (saveHistory) {
                appRepo.saveHistory(
                    AppHistory(null, account.uid, desc, amount)
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
                storeImage(false)
                showToast("Saved successfully.", Toast.LENGTH_SHORT)
            }
            R.id.rs_share -> {
                val it = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, storeImage(true))
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

    private fun storeImage(isShare: Boolean): Uri? {
        try {
            val image = exportToBitmap()
            val dist = if (isShare) {
                File(Environment.DIRECTORY_PICTURES, getString(R.string.app_name))
            } else {
                getExternalFilesDir("images_share")
            }
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

    private fun exportToBitmap(): Bitmap {
        val baseBitmap = BitmapFactory.decodeResource(resources, R.drawable.optimize)
        baseBitmap.applyCanvas {
            drawBitmap(qrBitmap, 50f, 50f, null)
            val paint = Paint().apply {
                color = Color.BLACK
                style = Paint.Style.FILL
                textSize = 18f
                textAlign = Paint.Align.CENTER
            }
            drawText(titleText, baseBitmap.width / 2f, 360f, paint)
            paint.textSize = 14f
            paint.color = 0x323232
            drawText(descText, baseBitmap.width / 2f, 375f, paint)
            drawText(amountText, baseBitmap.width / 2f, 390f, paint)
        }
        return baseBitmap
    }

}
