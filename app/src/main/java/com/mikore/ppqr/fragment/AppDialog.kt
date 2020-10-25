package com.mikore.ppqr.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.DialogFragment
import com.mikore.ppqr.R
import com.mikore.ppqr.utility.Utills
import kotlin.math.roundToInt

class AppDialog(@LayoutRes val layoutId: Int) : DialogFragment() {
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawableResource(R.color.lightTransparent)
        setStyle(STYLE_NO_INPUT, android.R.style.Theme)

        if (layoutId == R.layout.ads_dialog) {
            val layout: ImageView = view.findViewById(R.id.ads_frame)
            val bmp = BitmapFactory.decodeResource(resources, R.drawable.simple_ads).run {
                RoundedBitmapDrawableFactory.create(
                    resources,
                    aspectScreenWidth(0.8f)
                ).apply {
                    cornerRadius = Utills.dpToPx(10, resources).toFloat()
                }
            }
            layout.setImageDrawable(bmp)
        }

        view.findViewById<View>(R.id.dialog_close).setOnClickListener { dismiss() }

    }

    private fun Bitmap.aspectScreenWidth(percent: Float): Bitmap {
        var scaledWidth = width
        var scaledHeight = height

        if (scaledWidth > screenWidth) {
            val ratio = screenWidth.toFloat() / scaledWidth.toFloat()
            scaledWidth = screenWidth
            scaledHeight = (scaledHeight * ratio).roundToInt()
        }

        return Bitmap.createScaledBitmap(
            this,
            (scaledWidth * percent).roundToInt(),
            (scaledHeight + percent).roundToInt(),
            true
        )
    }

    private val screenWidth: Int
        get() {
            val metrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(metrics)
            return metrics.widthPixels
        }

}