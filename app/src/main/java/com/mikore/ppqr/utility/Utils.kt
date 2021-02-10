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
package com.mikore.ppqr.utility

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowManager
import kotlin.math.roundToInt

class Utils {
    companion object {

        @SuppressLint("NewApi")
        @Suppress("DEPRECATION")
        fun screenWidth(windowManager: WindowManager): Int {
            return if (Build.VERSION.SDK_INT >= 30) {
                val windowMetrics = windowManager.currentWindowMetrics
                val insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.statusBars())
                windowMetrics.bounds.width() - insets.left - insets.right
            } else {
                val display = windowManager.defaultDisplay
                val outMetrics = DisplayMetrics()
                display.getMetrics(outMetrics)
                outMetrics.widthPixels
            }
        }

        @SuppressLint("NewApi")
        @Suppress("DEPRECATION")
        fun screenHeight(windowManager: WindowManager): Int {
            return if (Build.VERSION.SDK_INT >= 30) {
                val windowMetrics = windowManager.currentWindowMetrics
                val insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.statusBars())
                windowMetrics.bounds.height() - insets.top - insets.bottom
            } else {
                val display = windowManager.defaultDisplay
                val outMetrics = DisplayMetrics()
                display.getMetrics(outMetrics)
                outMetrics.heightPixels
            }
        }

        fun screenDensity(resources: Resources): Float = resources.displayMetrics.density
    }
}