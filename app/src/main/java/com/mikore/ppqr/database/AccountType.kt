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

enum class AccountType {
    PHONE,
    TAX,
    EWALLET;

    fun asText(): String {
        return when (this) {
            PHONE -> "Phone"
            TAX -> "Tax"
            EWALLET -> "E-Wallet"
        }
    }

    companion object {
        fun asType(type: String): AccountType {
            return when (type) {
                "Phone" -> PHONE
                "Tax" -> TAX
                else -> EWALLET
            }
        }

        fun fromLength(length: Int): AccountType {
            return when (length) {
                10 -> PHONE
                13 -> TAX
                else -> EWALLET
            }
        }
    }
}