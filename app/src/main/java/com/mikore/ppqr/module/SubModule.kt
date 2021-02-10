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

import androidx.fragment.app.FragmentManager
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
abstract class SubModule {
    @ActivityScope
    @ContributesAndroidInjector
    abstract fun contributeResultActivity(): ResultActivity

    @AccountScope
    @ContributesAndroidInjector(modules = [AccountFragmentModule::class])
    abstract fun contributeAccountFragment(): AccountFragment

    @HistoryScope
    @ContributesAndroidInjector(modules = [HistoryFragmentModule::class])
    abstract fun contributeHistoryFragment(): HistoryFragment
}

@Module
class AccountFragmentModule {
    @Provides
    @AccountScope
    fun provideFragmentManager(fragment: AccountFragment): FragmentManager =
        fragment.requireActivity().supportFragmentManager

    @Provides
    @AccountScope
    fun provideAccountAdapter(fragmentManager: FragmentManager) =
        AccountAdapter(fragmentManager)
}

@Module
class HistoryFragmentModule {

    @Provides
    @HistoryScope
    fun provideFragmentManager(fragment: HistoryFragment): FragmentManager =
        fragment.requireActivity().supportFragmentManager

    @Provides
    @HistoryScope
    fun provideHistoryAdapter(fragmentManager: FragmentManager, appRepo: AppRepo) =
        HistoryAdapter(fragmentManager, appRepo)
}
