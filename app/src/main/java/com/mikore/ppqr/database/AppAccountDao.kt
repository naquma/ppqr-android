package com.mikore.ppqr.database

import androidx.room.*

@Dao
interface AppAccountDao {
    @Transaction
    @Query("SELECT * FROM account")
    fun getAccounts(): List<AppAccount>

    @Transaction
    @Query("SELECT * FROM account WHERE uid = :uid")
    fun getAccount(uid: String): AppAccount

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(account: AppAccount)

    @Transaction
    @Query("DELETE FROM account WHERE uid = :uid")
    fun delete(uid: String)
}