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
package com.mikore.ppqr.module

import androidx.fragment.app.FragmentActivity
import com.mikore.ppqr.activity.MainActivity
import com.mikore.ppqr.activity.ResultActivity
import com.mikore.ppqr.adapter.AccountAdapter
import com.mikore.ppqr.adapter.HistoryAdapter
import com.mikore.ppqr.database.AppRepo
import com.mikore.ppqr.fragment.AccountFragment
import com.mikore.ppqr.fragment.HistoryFragment
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {
    @ActivityScope
    @ContributesAndroidInjector(modules = [MainActivityModule::class, MainActivityFragmentModule::class])
    abstract fun contributeMainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun contributeResultActivity(): ResultActivity
}

@Module
class MainActivityModule {
    @Provides
    @ActivityScope
    fun provideFragmentActivity(mainActivity: MainActivity): FragmentActivity = mainActivity
}

@Module
abstract class MainActivityFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector(modules = [AccountFragmentModule::class])
    abstract fun provideAccountFragment(): AccountFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [HistoryFragmentModule::class])
    abstract fun provideHistoryFragment(): HistoryFragment
}

@Module
class AccountFragmentModule {
    @Provides
    @FragmentScope
    fun provideAccountAdapter(fragmentActivity: FragmentActivity) =
        AccountAdapter(fragmentActivity, fragmentActivity.supportFragmentManager)
}

@Module
class HistoryFragmentModule {
    @Provides
    @FragmentScope
    fun provideHistoryAdapter(fragmentActivity: FragmentActivity, appRepo: AppRepo) =
        HistoryAdapter(fragmentActivity.supportFragmentManager, appRepo)
}
