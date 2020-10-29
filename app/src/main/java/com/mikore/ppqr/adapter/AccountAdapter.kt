package com.mikore.ppqr.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.mikore.ppqr.AppConst
import com.mikore.ppqr.R
import com.mikore.ppqr.activity.ResultActivity
import com.mikore.ppqr.database.AppAccount
import com.mikore.ppqr.fragment.AccountFragment
import com.mikore.ppqr.fragment.AddDialog
import com.mikore.ppqr.utility.Utils
import javax.inject.Inject

class AccountAdapter @Inject constructor(
    private val context: Context,
    private val supportFragmentManager: FragmentManager
) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {

    private val accounts: ArrayList<AppAccount> = ArrayList()

    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val root: ConstraintLayout = item as ConstraintLayout
        val accountName: TextView = item.findViewById(R.id.account_name)
        val accountNo: TextView = item.findViewById(R.id.account_no)
        val accountType: TextView = item.findViewById(R.id.account_type)
        val customAmount: CheckBox = item.findViewById(R.id.custom_amount)
        val amountField: EditText = item.findViewById(R.id.custom_amount_field)
        val customDesc: TextView = item.findViewById(R.id.custom_desc)
        val descField: TextView = item.findViewById(R.id.custom_desc_field)
        val frame: FrameLayout = item.findViewById(R.id.account_frame_bg)
        val generate: Button = item.findViewById(R.id.generate_button)
        val edit: ImageButton = item.findViewById(R.id.edit_button)
        val delete: ImageButton = item.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.account_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val account = accounts[position]
        holder.accountName.text = account.name ?: "PromptPay ending XX" + account.no.takeLast(3)
        holder.accountNo.text = account.no
        holder.accountType.text = account.type.asText()
        holder.customAmount.setOnCheckedChangeListener { _, isChecked ->
            holder.amountField.isEnabled = isChecked
        }
        var isVisible = false
        val c = ConstraintSet().apply { clone(holder.root) }
        holder.frame.setOnClickListener {
            if (isVisible) {
                c.setVisibility(holder.customAmount.id, View.GONE)
                c.setVisibility(holder.amountField.id, View.GONE)
                c.setVisibility(holder.generate.id, View.GONE)
                c.setVisibility(holder.customDesc.id, View.GONE)
                c.setVisibility(holder.descField.id, View.GONE)
                c.setVisibility(holder.edit.id, View.GONE)
                c.setVisibility(holder.delete.id, View.GONE)
                c.connect(
                    holder.accountType.id,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM,
                    Utils.dpToPx(10, context.resources)
                )
            } else {
                c.setVisibility(holder.customAmount.id, View.VISIBLE)
                c.setVisibility(holder.amountField.id, View.VISIBLE)
                c.setVisibility(holder.generate.id, View.VISIBLE)
                c.setVisibility(holder.customDesc.id, View.VISIBLE)
                c.setVisibility(holder.descField.id, View.VISIBLE)
                c.setVisibility(holder.edit.id, View.VISIBLE)
                c.setVisibility(holder.delete.id, View.VISIBLE)
                c.clear(holder.accountType.id, ConstraintSet.BOTTOM)
            }
            TransitionManager.beginDelayedTransition(holder.root,
                AutoTransition().apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                })
            c.applyTo(holder.root)
            isVisible = !isVisible
        }
        holder.edit.setOnClickListener {
            AddDialog(account).show(supportFragmentManager, "edit_account")
        }
        holder.delete.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete")
                .setMessage("Do you want to delete?")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    Intent().also {
                        it.action = AccountFragment.DELETE_ACCOUNT
                        it.putExtra(AppConst.ARGS_ACCOUNT_ID_KEY, account.uid)
                        context.sendBroadcast(it)
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
        holder.generate.setOnClickListener {
            val args = Bundle()
            args.putString(AppConst.ARGS_ACCOUNT_ID_KEY, account.uid)
            args.putBoolean(AppConst.ARGS_SAVE_HISTORY_KEY, true)
            val amount = holder.amountField.text.toString()
            args.putString(
                AppConst.ARGS_AMOUNT_KEY,
                if (holder.customAmount.isChecked && amount.isNotEmpty()) {
                    amount
                } else null
            )
            args.putString(
                AppConst.ARGS_DESCRIPTION_KEY,
                holder.descField.text.toString().ifEmpty { null })
            val itn = Intent(context.applicationContext, ResultActivity::class.java)
            itn.putExtras(args)
            itn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(itn)
        }
    }

    fun updateData(data: List<AppAccount>) {
        accounts.clear()
        accounts.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemCount() = accounts.size

}
