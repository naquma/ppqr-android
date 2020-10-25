package com.mikore.ppqr.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account")
data class AppAccount(
    @PrimaryKey
    var uid: String,
    @ColumnInfo(name = "name")
    var name: String?,
    @ColumnInfo(name = "no")
    var no: String,
    @ColumnInfo(name = "type")
    var type: AccountType
)