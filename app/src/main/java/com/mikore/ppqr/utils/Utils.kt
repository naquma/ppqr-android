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
package com.mikore.ppqr.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.applyCanvas
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

class Utils {
    companion object {
        fun makeQr(data: String): Bitmap {
            val hint = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1
            )
            val matrix = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, 880, 880, hint)

            return Bitmap.createBitmap(880, 880, Bitmap.Config.RGB_565)
                .apply { eraseColor(Color.WHITE) }
                .applyCanvas {
                    val black = Paint().apply {
                        color = Color.BLACK
                        style = Paint.Style.FILL
                    }
                    for (x in 0 until matrix.width)
                        for (y in 0 until matrix.height)
                            if (matrix.get(x, y))
                                drawPoint(x.toFloat(), y.toFloat(), black)
                }
        }

        fun makeSlip(slipData: String, account: String, amount: String): Bitmap {
            val paint = Paint().apply {
                color = 0xff212121.toInt()
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            return Bitmap.createBitmap(1000, 1400, Bitmap.Config.ARGB_8888).applyCanvas {
                drawRoundRect(RectF(30F, 30F, 970F, 1370F), 25F, 25F, paint)
            }
        }
    }
}
