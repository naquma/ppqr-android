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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.mikore.ppqr.R
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
    private val context: Context,
    private val supportFragmentManager: FragmentManager,
    private val appRepo: AppRepo
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val histories: ArrayList<AppHistory> = ArrayList()

    private val dateFormat = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.ENGLISH)

    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val root: ConstraintLayout = item as ConstraintLayout
        val frame: FrameLayout = item.findViewById(R.id.history_frame_bg)
        val historyTitle: TextView = item.findViewById(R.id.history_title)
        val historyDesc: TextView = item.findViewById(R.id.history_desc)
        val historyDate: TextView = item.findViewById(R.id.history_date)
        val historyAmount: TextView = item.findViewById(R.id.history_amount)
        val historyBin: ImageButton = item.findViewById(R.id.history_bin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.history_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = histories[position]
        MainScope().launch {
            val account = withContext(Dispatchers.IO) {
                appRepo.getAccount(history.accountId)
            }
            holder.historyTitle.text = if (account.name.isNullOrEmpty()) {
                account.no
            } else {
                "${account.no} (${account.name})"
            }
            holder.frame.setOnClickListener {
                QRPopup(history, account).show(supportFragmentManager, "dialog_qr")
            }
        }
        holder.historyDesc.text = history.description ?: "No description"
        holder.historyDate.text = dateFormat.format(history.time)
        if (!history.amount.isNullOrEmpty()) {
            holder.historyAmount.text = "${history.amount} Baht"
            ConstraintSet().apply {
                clone(holder.root)
                setVisibility(holder.historyAmount.id, View.VISIBLE)
                clear(holder.historyDate.id, ConstraintSet.BOTTOM)
            }.applyTo(holder.root)
        }

        holder.historyBin.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete")
                .setMessage("Confirm to delete?")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    MainScope().launch {
                        appRepo.deleteHistory(history)
                    }
                    Intent().also {
                        it.action = HistoryFragment.REFRESH_FILTER
                        context.sendBroadcast(it)
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    fun updateData(data: List<AppHistory>) {
        histories.clear()
        histories.addAll(data.sortedByDescending { it.time })
        notifyDataSetChanged()
    }

    override fun getItemCount() = histories.size
}
