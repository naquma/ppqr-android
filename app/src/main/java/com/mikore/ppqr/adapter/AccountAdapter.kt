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
package com.mikore.ppqr.adapter

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.mikore.ppqr.Contracts
import com.mikore.ppqr.R
import com.mikore.ppqr.activity.ResultActivity
import com.mikore.ppqr.database.AppAccount
import com.mikore.ppqr.fragment.AccountFragment
import com.mikore.ppqr.fragment.AddDialog
import javax.inject.Inject

class AccountAdapter @Inject constructor(
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    private val accounts: ArrayList<AppAccount> = ArrayList()

    class AccountViewHolder(private val item: View) : RecyclerView.ViewHolder(item) {
        private val accountName: TextView = item.findViewById(R.id.account_name)
        private val accountNo: TextView = item.findViewById(R.id.account_no)
        private val accountType: TextView = item.findViewById(R.id.account_type)
        private val customAmount: CheckBox = item.findViewById(R.id.custom_amount)
        private val amountField: EditText = item.findViewById(R.id.custom_amount_field)
        private val descField: TextView = item.findViewById(R.id.custom_desc_field)
        private val genClick: Button = item.findViewById(R.id.generate_button)
        private val editClick: ImageButton = item.findViewById(R.id.edit_button)
        private val deleteClick: ImageButton = item.findViewById(R.id.delete_button)
        private val section2: LinearLayout = item.findViewById(R.id.account_section_2)

        fun bind(name: String, no: String, type: String) {
            accountName.text = name
            accountNo.text = no
            accountType.text = type
            changeVisible(amountField, customAmount.isChecked)
            customAmount.setOnCheckedChangeListener { _, isChecked ->
                changeVisible(amountField, isChecked)
            }
            item.setOnClickListener {
                changeVisible(section2, section2.visibility == View.GONE)
            }
        }

        fun bindClick(account: AppAccount, manager: FragmentManager) {
            editClick.setOnClickListener {
                AddDialog(account).show(manager, "add_account")
            }
            deleteClick.setOnClickListener {
                AlertDialog.Builder(item.context)
                    .setTitle("Delete")
                    .setMessage("Do you want to delete?")
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        Intent().also {
                            it.action = AccountFragment.DELETE_ACCOUNT
                            it.putExtra(Contracts.KEY_ACCOUNT_ID, account.uid)
                            item.context.sendBroadcast(it)
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
            genClick.setOnClickListener {
                val args = Bundle().apply {
                    putString(Contracts.KEY_ACCOUNT_ID, account.uid)
                    putBoolean(Contracts.KEY_SAVE_HISTORY, true)
                    val amount = amountField.text.toString()
                    if (amount.isEmpty()) {
                        putBoolean(Contracts.KEY_HAVE_AMOUNT, false)
                    } else {
                        putBoolean(Contracts.KEY_HAVE_AMOUNT, true)
                        putString(Contracts.KEY_AMOUNT, amount)
                    }
                    val desc = descField.text.toString()
                    if (desc.isEmpty()) {
                        putBoolean(Contracts.KEY_HAVE_DESCRIPTION, false)
                    } else {
                        putBoolean(Contracts.KEY_HAVE_DESCRIPTION, true)
                        putString(Contracts.KEY_DESCRIPTION, desc)
                    }
                }
                val resultIntent =
                    Intent(item.context.applicationContext, ResultActivity::class.java)
                resultIntent.putExtras(args)
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                item.context.startActivity(resultIntent)
            }
        }

        private fun changeVisible(view: View, show: Boolean) {
            view.visibility = if (show) View.VISIBLE else View.GONE
            TransitionManager.beginDelayedTransition(item as ConstraintLayout,
                AutoTransition().apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                })
        }

        companion object {
            fun create(parent: ViewGroup): AccountViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.account_item, parent, false)
                return AccountViewHolder(view)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        return AccountViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = accounts[position]
        val name = account.name ?: "PromptPay ending XX" + account.no.takeLast(3)
        holder.bind(name, account.no, account.type.asText())
        holder.bindClick(account, fragmentManager)
    }

    fun updateData(data: List<AppAccount>) {
        accounts.clear()
        accounts.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemCount() = accounts.size

}
