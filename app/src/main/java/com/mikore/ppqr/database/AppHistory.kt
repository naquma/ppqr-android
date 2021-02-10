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
    var uid: String? = UUID.randomUUID().toString(),
    @ColumnInfo(name = "account_id", index = true)
    var accountId: String,
    @ColumnInfo(name = "description")
    var description: String?,
    @ColumnInfo(name = "amount")
    var amount: String?,
    @ColumnInfo(name = "update_at")
    var time: Long = System.currentTimeMillis()
)