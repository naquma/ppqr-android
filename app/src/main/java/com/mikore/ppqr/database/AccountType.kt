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