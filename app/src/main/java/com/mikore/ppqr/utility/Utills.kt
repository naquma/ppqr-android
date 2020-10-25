package com.mikore.ppqr.utility

import android.content.res.Resources
import android.util.DisplayMetrics
import kotlin.math.roundToInt

class Utills {
    companion object {
        fun dpToPx(dp: Int, resources: Resources) =
            (dp * (resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }
}