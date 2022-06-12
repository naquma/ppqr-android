package com.mikore.ppqr

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.mikore.ppqr.database.AppDatabase

class PromptPayApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
