package com.mikore.ppqr.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mikore.ppqr.R
import com.mikore.ppqr.adapter.HistoryAdapter
import com.mikore.ppqr.database.AppRepo
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HistoryFragment : Fragment() {

    companion object {
        const val REFRESH_FILTER = "com.mikore.ppqr.HISTORY_REFRESH"
    }

    private lateinit var historySwipe: SwipeRefreshLayout

    private val refreshBroadcast = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, it: Intent?) {
            update()
        }
    }

    @Inject
    lateinit var adapter: HistoryAdapter

    @Inject
    lateinit var appRepo: AppRepo

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.history_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstance: Bundle?) {
        super.onViewCreated(view, savedInstance)
        val historyList = view.findViewById<RecyclerView>(R.id.history_list)
        historySwipe = view.findViewById(R.id.history_swipe)

        historyList.adapter = adapter
        historyList.layoutManager = LinearLayoutManager(context)
        historyList.setHasFixedSize(true)

        historySwipe.setOnRefreshListener {
            update()
        }

        update()

        val filter = IntentFilter(REFRESH_FILTER)
        context?.registerReceiver(refreshBroadcast, filter)
    }

    override fun onDestroy() {
        context?.unregisterReceiver(refreshBroadcast)
        super.onDestroy()
    }

    private fun update() {
        historySwipe.isRefreshing = true
        MainScope().launch {
            val data = withContext(Dispatchers.IO) {
                appRepo.getHistories()
            }
            adapter.updateData(data)
            historySwipe.isRefreshing = false
        }
    }
}