package com.mikore.ppqr.fragment

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment

import com.mikore.ppqr.R
import com.mikore.ppqr.database.AccountType
import com.mikore.ppqr.database.AppAccount
import com.mikore.ppqr.database.AppHistory

class GenDialog : DialogFragment() {

    private lateinit var genBtn: Button
    private lateinit var cancelBtn: Button
    private lateinit var accountNo: EditText
    private lateinit var amount: EditText
    private lateinit var desc: EditText

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
        return inflater.inflate(R.layout.dialog_quick_gen, container, false)
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

        genBtn = view.findViewById(R.id.quick_btn_gen)
        cancelBtn = view.findViewById(R.id.quick_btn_cancel)
        accountNo = view.findViewById(R.id.quick_no_edit)
        amount = view.findViewById(R.id.quick_amount_edit)
        desc = view.findViewById(R.id.quick_desc_edit)

        cancelBtn.setOnClickListener {
            dismiss()
        }

        genBtn.setOnClickListener {
            val no = accountNo.text.toString()
            val am = amount.text.toString()
            QRPopup(
                AppHistory("", "", "Quick Generate", am.ifEmpty { null }),
                AppAccount("", null, no, AccountType.fromLength(no.length))
            ).show(requireActivity().supportFragmentManager, "quick_gen")
        }

    }
}