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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikore.ppqr.App.Companion.appScope
import com.mikore.ppqr.Contracts
import com.mikore.ppqr.R
import com.mikore.ppqr.adapter.AccountAdapter
import com.mikore.ppqr.database.AppRepo
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AccountFragment : Fragment() {

    companion object {
        const val REFRESH_FILTER = "com.mikore.ppqr.ACCOUNT_REFRESH"
        const val DELETE_ACCOUNT = "com.mikore.ppqr.DELETE_ACCOUNT"
    }

    private lateinit var accountRecycler: RecyclerView
    private lateinit var accountSwipe: SwipeRefreshLayout
    private lateinit var notFound: MaterialCardView

    @Inject
    lateinit var appRepo: AppRepo

    @Inject
    lateinit var accountAdapter: AccountAdapter

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstance: Bundle?
    ): View? {
        return inflater.inflate(R.layout.account_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstance: Bundle?) {
        super.onViewCreated(view, savedInstance)
        appRepo = AppRepo(requireContext())
        accountRecycler = view.findViewById(R.id.account_list)
        accountSwipe = view.findViewById(R.id.account_swipe)
        notFound = view.findViewById(R.id.not_found)
        accountSwipe.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.colorAccent)
        )
        accountRecycler.adapter = accountAdapter
        accountRecycler.layoutManager = LinearLayoutManager(requireContext())
        accountRecycler.setHasFixedSize(true)
        accountSwipe.setOnRefreshListener {
            update()
        }

        update()

        val fabGroup = view.findViewById<FloatingActionButton>(R.id.fab_group)
        val fabGen = view.findViewById<FloatingActionButton>(R.id.fab_gen)
        val fabGenText = view.findViewById<TextView>(R.id.fab_gen_text)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fab_add)
        val fabAddText = view.findViewById<TextView>(R.id.fab_add_text)

        fabGroup.setTint(Color.WHITE)
        fabGen.setTint(Color.WHITE)
        fabAdd.setTint(Color.WHITE)

        var expanded = false
        fabGroup.setOnClickListener {
            val cs = ConstraintSet()
            cs.clone(view as ConstraintLayout)
            if (expanded) {
                cs.setVisibility(fabGenText.id, View.GONE)
                cs.setVisibility(fabAddText.id, View.GONE)
                cs.connect(fabGen.id, ConstraintSet.BOTTOM, fabGroup.id, ConstraintSet.BOTTOM)
                cs.connect(fabAdd.id, ConstraintSet.BOTTOM, fabGroup.id, ConstraintSet.BOTTOM)
                cs.setRotation(fabGroup.id, 180f)
            } else {
                cs.setVisibility(fabGenText.id, View.VISIBLE)
                cs.setVisibility(fabAddText.id, View.VISIBLE)
                cs.connect(fabGen.id, ConstraintSet.BOTTOM, fabGroup.id, ConstraintSet.TOP)
                cs.connect(fabAdd.id, ConstraintSet.BOTTOM, fabGen.id, ConstraintSet.TOP)
                cs.setRotation(fabGroup.id, 0f)
            }
            TransitionManager.beginDelayedTransition(view, AutoTransition().apply {
                duration = 200
                interpolator = AccelerateDecelerateInterpolator()
            })
            cs.applyTo(view)
            expanded = !expanded
        }

        fabAdd.setOnClickListener {
            AddDialog().show(requireActivity().supportFragmentManager, "add_dialog")
        }

        fabGen.setOnClickListener {
            GenDialog().show(requireActivity().supportFragmentManager, "gen_dialog")
        }
        context?.registerReceiver(refreshBroadcast, IntentFilter(REFRESH_FILTER))
        context?.registerReceiver(deleteBroadcast, IntentFilter(DELETE_ACCOUNT))
    }

    private fun update() {
        accountSwipe.isRefreshing = true
        appScope.launch(Dispatchers.Main) {
            val data = withContext(Dispatchers.IO) {
                appRepo.getAccounts()
            }
            if (data.isNotEmpty()) {
                notFound.visibility = View.GONE
            } else {
                notFound.visibility = View.VISIBLE
            }
            accountAdapter.updateData(data)
            accountSwipe.isRefreshing = false
        }
    }

    override fun onDestroy() {
        context?.unregisterReceiver(refreshBroadcast)
        context?.unregisterReceiver(deleteBroadcast)
        super.onDestroy()
    }

    private fun FloatingActionButton.setTint(color: Int) = DrawableCompat.setTint(drawable, color)


    private val refreshBroadcast = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, it: Intent?) {
            update()
        }
    }

    private val deleteBroadcast = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val uid = intent?.getStringExtra(Contracts.KEY_ACCOUNT_ID)
            if (!uid.isNullOrEmpty()) {
                appScope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        appRepo.getAccount(uid)
                    }.run {
                        appRepo.deleteAccount(this)
                        update()
                        Intent().also {
                            it.action = HistoryFragment.REFRESH_FILTER
                            requireContext().sendBroadcast(it)
                        }
                        Toast.makeText(
                            requireContext(),
                            "Successfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
        }
    }
}