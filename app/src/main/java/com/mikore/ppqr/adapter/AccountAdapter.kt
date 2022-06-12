package com.mikore.ppqr.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikore.ppqr.R
import com.mikore.ppqr.database.Account

class AccountAdapter : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    private val _accounts: MutableList<Account> = ArrayList()

    class AccountViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val name: TextView = item.findViewById(R.id.account_title)
        val number: TextView = item.findViewById(R.id.account_number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        return AccountViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.account_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = _accounts[position]
        holder.name.text = account.name
        holder.number.text = account.number
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAccounts(accounts: List<Account>) {
        _accounts.clear()
        _accounts.addAll(accounts)
        notifyDataSetChanged()
    }

    override fun getItemCount() = _accounts.size
}
