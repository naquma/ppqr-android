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
package com.mikore.ppqr.database

import android.content.Context
import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppRepo @Inject constructor(private val context: Context) {
    private val appDatabase = AppDatabase.getInstance(context)
    private val accountDao = appDatabase.accountDao()
    private val historyDao = appDatabase.historyDao()

    @WorkerThread
    suspend fun getAccounts(): List<AppAccount> {
        return withContext(Dispatchers.IO) {
            accountDao.getAccounts()
        }
    }

    @WorkerThread
    suspend fun getAccount(uid: String?): AppAccount {
        return withContext(Dispatchers.IO) {
            accountDao.getAccount(uid)
        }
    }

    @WorkerThread
    suspend fun findUid(no: String): String {
        return withContext(Dispatchers.IO) {
            getAccounts().first { it.no == no }.uid
        }
    }

    @WorkerThread
    suspend fun accountExists(no: String): Boolean {
        return withContext(Dispatchers.IO) {
            getAccounts().any { it.no == no }
        }
    }

    @WorkerThread
    suspend fun getHistories(): List<AppHistory> {
        return withContext(Dispatchers.IO) {
            historyDao.getHistories()
        }
    }

    @WorkerThread
    suspend fun saveAccount(account: AppAccount): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                accountDao.add(account).run {
                    true
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    @WorkerThread
    suspend fun deleteAccount(account: AppAccount): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                accountDao.delete(account.uid).run {
                    true
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    @WorkerThread
    suspend fun saveHistory(history: AppHistory): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                historyDao.add(history).run {
                    true
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    @WorkerThread
    suspend fun deleteHistory(history: AppHistory): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                historyDao.delete(history).run {
                    true
                }
            } catch (e: Exception) {
                false
            }
        }
    }
}
