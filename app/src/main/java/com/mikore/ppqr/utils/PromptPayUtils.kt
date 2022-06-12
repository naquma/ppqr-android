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
package com.mikore.ppqr.utils

class PromptPayUtils {
    companion object {
        fun build(account: String, amount: Double = 0.0): String? {
            if (account.length !in arrayOf(15, 13, 11, 10)) {
                return null
            }
            var data = "0002000102" + if (amount > 0.009) "12" else "11"
            val t = "0016A000000677010111" + when (account.length) {
                15 -> "0315$account"
                13 -> "0213$account"
                else -> "01130066" + account.takeLast(9)
            }
            data += "29${t.length}${t}5802TH5303764"
            if (amount > 0.009) {
                val f = "%.2f".format(amount)
                data += "54${f.length}${f}"
            }
            data += "6304" + checksum("${data}6304")
            return data
        }

        private fun checksum(data: String): String {
            var crc = 0xFFFF
            data.toByteArray().forEach { byte ->
                for (i in 0..7) {
                    val bit = byte.toInt() shr 7 - i and 1 == 1
                    val c15 = crc shr 15 and 1 == 1
                    crc = crc shl 1
                    if (c15 xor bit) crc = crc xor 0x1021
                }
            }
            return "%04x".format(crc and 0xFFFF).uppercase()
        }
    }
}
