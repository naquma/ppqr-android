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
import com.mikore.ppqr.App.Companion.appScope
import com.mikore.ppqr.BuildConfig
import com.mikore.ppqr.Contracts
import com.mikore.ppqr.R
import com.mikore.ppqr.database.AppHistory
import com.mikore.ppqr.database.AppRepo
import com.mikore.ppqr.fragment.HistoryFragment
import com.mikore.ppqr.utility.PromptPayUtil
import dagger.android.AndroidInjection
import kotlinx.coroutines.Dispatchers
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

        val uid = intent.getStringExtra(Contracts.KEY_ACCOUNT_ID)
        val saveHistory = intent.getBooleanExtra(Contracts.KEY_SAVE_HISTORY, false)
        val haveAmount = intent.getBooleanExtra(Contracts.KEY_HAVE_AMOUNT, false)
        val amount = if (haveAmount) intent.getStringExtra(Contracts.KEY_AMOUNT) else null
        val haveDesc = intent.getBooleanExtra(Contracts.KEY_HAVE_DESCRIPTION, false)
        val desc = if (haveDesc) intent.getStringExtra(Contracts.KEY_DESCRIPTION) else null

        val params = qrView.layoutParams as ConstraintLayout.LayoutParams
        params.width = size(300.0 / 400.0)
        params.height = size(300.0 / 400.0)
        qrView.layoutParams = params

        amountText = "Amount: " + if (haveAmount) "$amount Baht." else "Not specified"
        amountView.text = amountText
        descText = "Note: " + if (haveDesc) desc else "Not specified"
        descView.text = descText

        appScope.launch(Dispatchers.Main) {
            val account = withContext(Dispatchers.IO) {
                appRepo.getAccount(uid)
            }
            titleText = account.no
            if (!account.name.isNullOrEmpty()) {
                titleText += " (${account.name})"
            }
            nameView.text = titleText
            val data = promptPay.generateQRData(account.no, amount)
            val qrg = QRGEncoder(data, null, QRGContents.Type.TEXT, size())
            qrBitmap = qrg.bitmap
            qrView.setImageBitmap(qrBitmap)
            if (saveHistory) {
                appRepo.saveHistory(
                    AppHistory(account.uid, desc, amount)
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
        permissions: Array<String>,
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
            val image = exportToBitmap()
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

    private fun exportToBitmap(): Bitmap {
        var baseBitmap = BitmapFactory.decodeResource(resources, R.drawable.optimize)
        baseBitmap = baseBitmap.copy(Bitmap.Config.ARGB_8888, true).applyCanvas {
            val heightSize = size(width, 15.0 / 400.0)
            val scaleSize = size(width, 0.75).toInt()
            drawBitmap(
                Bitmap.createScaledBitmap(qrBitmap, scaleSize, scaleSize, false),
                heightSize + size(width, 35.0 / 400.0),
                heightSize + size(width, 35.0 / 400.0),
                null
            )
            val paint = Paint().apply {
                color = Color.BLACK
                style = Paint.Style.FILL
                textSize = 42f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            drawText(
                titleText,
                width / 2f,
                qrBitmap.height + heightSize,
                paint
            )
            paint.textSize = 35f
            paint.color = Color.GRAY
            drawText(
                descText,
                width / 2f,
                qrBitmap.height + heightSize + size(width, 30.0 / 400.0),
                paint
            )
            drawText(
                amountText,
                width / 2f,
                qrBitmap.height + heightSize + size(width, 60.0 / 400.0),
                paint
            )
        }
        return baseBitmap
    }

    private fun size(percent: Double = 0.925): Int {
        val width = BitmapFactory.decodeResource(resources, R.drawable.optimize).width
        return size(width, percent).toInt()
    }

    private fun size(baseSize: Int, percent: Double): Float {
        return (baseSize * percent).toFloat()
    }
}
