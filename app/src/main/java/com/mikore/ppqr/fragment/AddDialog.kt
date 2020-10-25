package com.mikore.ppqr.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.mikore.ppqr.BuildConfig
import com.mikore.ppqr.R
import com.mikore.ppqr.database.AccountType
import com.mikore.ppqr.database.AppAccount
import com.mikore.ppqr.database.AppRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AddDialog(private val account: AppAccount? = null) : DialogFragment() {

    private lateinit var saveBtn: Button
    private lateinit var cancelBtn: Button
    private lateinit var accountName: EditText
    private lateinit var accountNo: EditText
    private lateinit var appRepo: AppRepo

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
        return inflater.inflate(R.layout.dialog_add, container, false)
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
        isCancelable = false

        saveBtn = view.findViewById(R.id.add_btn_save)
        cancelBtn = view.findViewById(R.id.add_btn_cancel)
        accountName = view.findViewById(R.id.add_name_edit)
        accountNo = view.findViewById(R.id.add_no_edit)

        if (account != null) {
            accountName.setText(account.name)
            accountNo.setText(account.no)
        }

        cancelBtn.setOnClickListener {
            dismiss()
        }

        appRepo = AppRepo(requireContext())

        saveBtn.setOnClickListener {
            val no = accountNo.text.toString()
            val name = accountName.text.toString()
            if (no.isNotEmpty() && no.length >= 10 && no.length <= 15) {
                val type = AccountType.fromLength(no.length)
                MainScope().launch {
                    if (appRepo.accountExists(no)) {
                        if (account != null) {
                            addAccount(appRepo.findUid(no), name, no, type)
                            sendThenDismiss()
                        } else {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Account No. already exists.")
                                .setMessage("Do you want to replace it?")
                                .setPositiveButton(android.R.string.ok) { _, _ ->
                                    MainScope().launch {
                                        addAccount(appRepo.findUid(no), name, no, type)
                                    }
                                    sendThenDismiss()
                                }
                                .setNegativeButton(android.R.string.cancel, null)
                                .show()
                        }
                    } else {
                        addAccount(UUID.randomUUID().toString(), name, no, type)
                        sendThenDismiss()
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter a valid account no.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun sendThenDismiss() {
        Intent().also {
            it.action = AccountFragment.REFRESH_FILTER
            context?.sendBroadcast(it)
        }
        dismiss()
    }

    private suspend fun addAccount(uid: String, name: String, no: String, type: AccountType) {
        try {
            if (appRepo.saveAccount(AppAccount(uid, name.ifEmpty { null }, no, type))) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "saved successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(),
                    "Can't save now, try again later.",
                    Toast.LENGTH_LONG
                ).show()
                if (BuildConfig.DEBUG) {
                    Toast.makeText(
                        requireContext(),
                        Log.getStackTraceString(e),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}