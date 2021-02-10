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
package com.mikore.ppqr.fragment

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.fragment.app.DialogFragment
import com.mikore.ppqr.R
import com.mikore.ppqr.database.AppAccount
import com.mikore.ppqr.database.AppHistory
import com.mikore.ppqr.utility.PromptPayUtil

class QRPopup(
    private val history: AppHistory,
    private val account: AppAccount
) : DialogFragment() {

    private lateinit var name: TextView
    private lateinit var amount: TextView
    private lateinit var desc: TextView
    private lateinit var img: ImageView

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.qr_popup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawableResource(R.color.lightTransparent)
        setStyle(STYLE_NO_INPUT, android.R.style.Theme)

        name = view.findViewById(R.id.textView11)
        amount = view.findViewById(R.id.textView12)
        desc = view.findViewById(R.id.textView13)
        img = view.findViewById(R.id.imageView7)

        val nameText = if (account.name.isNullOrEmpty()) {
            account.no
        } else {
            "${account.name} (${account.no})"
        }
        name.text = nameText
        val amountText = "Amount: " + if (history.amount.isNullOrEmpty()) {
            "Not specified"
        } else {
            "${history.amount} Baht."
        }
        amount.text = amountText
        val descText = "Note: " + history.description.ifNullOrEmpty { "Not specified" }
        desc.text = descText

        val util = PromptPayUtil()
        val qr = QRGEncoder(
            util.generateQRData(account.no, history.amount.ifNullOrEmpty { "0" }),
            null,
            QRGContents.Type.TEXT,
            300
        )
        img.setImageBitmap(qr.bitmap)

        view.findViewById<Button>(R.id.qr_dismiss).setOnClickListener {
            dismiss()
        }
    }

    private fun String?.ifNullOrEmpty(d: () -> String): String {
        return if (this.isNullOrEmpty()) d() else this
    }
}