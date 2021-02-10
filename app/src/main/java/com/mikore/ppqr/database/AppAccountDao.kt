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

import androidx.room.*

@Dao
interface AppAccountDao {
    @Transaction
    @Query("SELECT * FROM account")
    fun getAccounts(): List<AppAccount>

    @Transaction
    @Query("SELECT * FROM account WHERE uid = :uid")
    fun getAccount(uid: String?): AppAccount

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(account: AppAccount)

    @Transaction
    @Query("DELETE FROM account WHERE uid = :uid")
    fun delete(uid: String)
}