package com.mikore.ppqr.database

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun StringToAccountType(type: String): AccountType = AccountType.asType(type)

    @TypeConverter
    fun AccountTypeToString(type: AccountType): String = type.asText()
}