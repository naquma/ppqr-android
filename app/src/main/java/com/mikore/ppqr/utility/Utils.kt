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
        fun dpToPx(dp: Int, resources: Resources) =
            (dp * (resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()

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