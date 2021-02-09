package com.mikore.ppqr.database

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppRepo @Inject constructor(private val context: Context) {
    private val appDatabase = AppDatabase.getInstance(context)
    private val accountDao = appDatabase.accountDao()
    private val historyDao = appDatabase.historyDao()


    suspend fun getAccounts(): List<AppAccount> {
        return withContext(Dispatchers.IO) {
            accountDao.getAccounts()
        }
    }

    suspend fun getAccount(uid: String): AppAccount {
        return withContext(Dispatchers.IO) {
            accountDao.getAccount(uid)
        }
    }

    suspend fun findUid(no: String): String {
        return withContext(Dispatchers.IO) {
            getAccounts().first { it.no == no }.uid
        }
    }

    suspend fun accountExists(no: String): Boolean {
        return withContext(Dispatchers.IO) {
            getAccounts().any { it.no == no }
        }
    }

    suspend fun getHistories(): List<AppHistory> {
        return withContext(Dispatchers.IO) {
            historyDao.getHistories()
        }
    }

    suspend fun getHistory(uid: String): AppHistory {
        return withContext(Dispatchers.IO) {
            getHistories().first { it.uid == uid }
        }
    }

    suspend fun findHistories(account: AppAccount): List<AppHistory> {
        return withContext(Dispatchers.IO) {
            getHistories().filter { it.accountId == account.uid }
        }
    }

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
