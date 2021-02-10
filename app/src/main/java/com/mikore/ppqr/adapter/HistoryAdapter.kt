package com.mikore.ppqr.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.mikore.ppqr.R
import com.mikore.ppqr.database.AppAccount
import com.mikore.ppqr.database.AppHistory
import com.mikore.ppqr.database.AppRepo
import com.mikore.ppqr.fragment.HistoryFragment
import com.mikore.ppqr.fragment.QRPopup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class HistoryAdapter @Inject constructor(
    private val fragmentManager: FragmentManager,
    private val appRepo: AppRepo
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val histories: ArrayList<AppHistory> = ArrayList()

    private val dateFormat = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.ENGLISH)

    class ViewHolder(private val item: View) : RecyclerView.ViewHolder(item) {
        private val historyFrame: FrameLayout = item.findViewById(R.id.history_frame_bg)
        private val historyTitle: TextView = item.findViewById(R.id.history_title)
        private val historyDesc: TextView = item.findViewById(R.id.history_desc)
        private val historyDate: TextView = item.findViewById(R.id.history_date)
        private val historyAmount: TextView = item.findViewById(R.id.history_amount)
        private val historyBin: ImageButton = item.findViewById(R.id.history_bin)

        fun bind(title: String?, desc: String, date: String, amount: String?) {
            historyTitle.text = title
            historyDesc.text = desc
            historyDate.text = date
            historyAmount.text = amount
        }

        fun bindFrameClick(history: AppHistory, account: AppAccount, manager: FragmentManager) {
            historyFrame.setOnClickListener {
                QRPopup(history, account).show(manager, "dialog_qr")
            }
        }

        fun bindRemoveClick(history: AppHistory, appRepo: AppRepo) {
            historyBin.setOnClickListener {
                AlertDialog.Builder(item.context)
                .setTitle("Delete")
                .setMessage("Confirm to delete?")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    MainScope().launch {
                        appRepo.deleteHistory(history)
                    }
                    Intent().also {
                        it.action = HistoryFragment.REFRESH_FILTER
                        item.context.sendBroadcast(it)
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            }
        }

        fun showAmount(show: Boolean) {
            historyAmount.visibility = if (show) View.VISIBLE else View.GONE
        }

        companion object {
            fun create(parent: ViewGroup): HistoryAdapter.ViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.history_item, parent, false)
                return HistoryAdapter.ViewHolder(view)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryAdapter.ViewHolder {
        return HistoryAdapter.ViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: HistoryAdapter.ViewHolder, position: Int) {
        val history = histories[position]
        MainScope().launch {
            val account = withContext(Dispatchers.IO) {
                appRepo.getAccount(history.accountId)
            }
            holder.bindFrameClick(history, account, fragmentManager)
            var title: String = account.no
            if (!account.name.isNullOrEmpty()) {
                title += " (${account.name})"
            }
            val desc = history.description ?: "No description"
            val date = dateFormat.format(history.time)
            var amount: String? = null
            val haveAmount = !history.amount.isNullOrEmpty()
            if (haveAmount) {
                amount = "${history.amount} Baht"
            }
            holder.bind(title, desc, date, amount)
            holder.showAmount(haveAmount)
            holder.bindRemoveClick(history, appRepo)
        }
    }

    fun updateData(data: List<AppHistory>) {
        histories.clear()
        histories.addAll(data.sortedByDescending { it.time })
        notifyDataSetChanged()
    }

    override fun getItemCount() = histories.size
}
