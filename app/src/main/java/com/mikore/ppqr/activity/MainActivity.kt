package com.mikore.ppqr.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.mikore.ppqr.R
import com.mikore.ppqr.adapter.PagerAdapter
import com.mikore.ppqr.fragment.AppDialog
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.main_layout.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasAndroidInjector {

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)
        setSupportActionBar(toolbar)

        pager_main.adapter = PagerAdapter(this)
        TabLayoutMediator(tab_layout, pager_main) { tab, position ->
            tab.text = if (position == 0) "Account" else "History"
        }.attach()

        AppDialog(R.layout.ads_dialog).show(supportFragmentManager, "ads")

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.abount_menu) {
            AppDialog(R.layout.about_dialog).show(supportFragmentManager, "about")
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

}