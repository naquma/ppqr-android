package com.mikore.ppqr.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "history",
    foreignKeys = [ForeignKey(
        entity = AppAccount::class,
        parentColumns = ["uid"],
        childColumns = ["account_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.NO_ACTION,
        deferred = true
    )]
)
data class AppHistory(
    @PrimaryKey
    var uid: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "account_id", index = true)
    var accountId: String,
    @ColumnInfo(name = "description")
    var description: String?,
    @ColumnInfo(name = "amount")
    var amount: String?,
    @ColumnInfo(name = "update_at")
    var time: Long = System.currentTimeMillis()
)