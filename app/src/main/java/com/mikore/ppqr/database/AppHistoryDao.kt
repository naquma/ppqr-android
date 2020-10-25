package com.mikore.ppqr.database

import androidx.room.*

@Dao
interface AppHistoryDao {
    @Query("SELECT * FROM history")
    fun getHistories(): List<AppHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(history: AppHistory)

    @Delete
    fun delete(history: AppHistory)
}