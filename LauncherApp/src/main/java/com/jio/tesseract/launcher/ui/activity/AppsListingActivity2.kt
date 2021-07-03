package com.jio.tesseract.launcher.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.MenuItemCompat
import androidx.core.view.MenuItemCompat.OnActionExpandListener
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jio.tesseract.launcher.R
import com.jio.tesseract.launcher.base.BaseActivity
import com.jio.tesseract.launcher.databinding.ActivityApplistBinding
import com.jio.tesseract.launcher.ui.adapter.AppsListAdapter
import com.jio.tesseract.launcher.viewmodel.LauncherViewModel
import com.jio.tesseract.launchersdk.AppInfo
import com.jio.tesseract.launchersdk.AppStatus
import com.jio.tesseract.launchersdk.AppStatus.ADDED
import com.jio.tesseract.launchersdk.AppStatus.NA
import com.jio.tesseract.launchersdk.AppStatus.REMOVED
import com.jio.tesseract.launchersdk.Constant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "AppsListingActivity2"

/**
 * Launcher Activity with <category android:name="android.intent.category.HOME" />
<category android:name="android.intent.category.DEFAULT" /> so can be replaceable with device default launcher
 * @property binding ActivityApplistBinding
 * @property launcherViewModel LauncherViewModel
 * @property appsListAdapter AppsListAdapter
 * @property searchItem MenuItem?
 * @property appStatusUpdateReceiver <no name provided>
 */
@AndroidEntryPoint
class AppsListingActivity2 : BaseActivity() {

    private lateinit var binding: ActivityApplistBinding
    private val launcherViewModel: LauncherViewModel by viewModels()
    private lateinit var appsListAdapter: AppsListAdapter
    private var searchItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUi()
        readAppsListFromDevice()
        registerAppStatusReceiver()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.launcher_menu, menu)
        searchItem = menu.findItem(R.id.action_search)
        val searchView: SearchView = MenuItemCompat.getActionView(searchItem) as SearchView

        MenuItemCompat.setOnActionExpandListener(searchItem, object : OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?) = true

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                appsListAdapter.reSubmitOrgList()
                return true
            }
        })

        searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.apply {
                    appsListAdapter.appInfoOrgList?.let { appsList ->
                        val filteredAppsList: List<AppInfo> = appsList.filter { appInfo ->
                            appInfo.appName.startsWith(this, true)
                        }
                        appsListAdapter.submitList(filteredAppsList)
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun initUi() {
        appsListAdapter = AppsListAdapter {
            launchApp(it)
        }

        binding.appsListRv.apply {
            layoutManager = LinearLayoutManager(launcherApp, RecyclerView.VERTICAL, false)
            adapter = appsListAdapter
        }
    }

    /**
     *
     * @param appInfo AppInfo
     */
    private fun launchApp(appInfo: AppInfo) {
        packageManager.getLaunchIntentForPackage(appInfo.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
        }
    }

    private fun readAppsListFromDevice() {
        binding.progressBarBinding.progressBarLayout.isVisible = true
        supportActionBar?.hide()
        launcherViewModel.appInfoListObserver.observe(this, {
            binding.progressBarBinding.progressBarLayout.isVisible = false
            supportActionBar?.show()

            appsListAdapter.submitOrgList(it)
        })
        launcherViewModel.readAppsList()
    }

    /**
     * Register local broadcast to receive app Aad/Remove Status
     */
    private fun registerAppStatusReceiver() {
        val filter = IntentFilter()
        filter.addAction(Constant.ACTION_BROADCAST_APP_STATUS)
        LocalBroadcastManager.getInstance(this).registerReceiver(appStatusUpdateReceiver, filter)
    }

    /**
     * BroadcastReceiver, to receive Local Broadcast Intent with packageName and app status
     */
    private val appStatusUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            Log.d(TAG, "appStatusUpdateReceiver: onReceive")
            intent?.extras?.let {
                val packageName = it.getString(Constant.KEY_BUNDLE_APP_PACKAGE_NAME, "")
                val appStatus = (it.getSerializable(Constant.KEY_BUNDLE_APP_STATUS) as AppStatus?) ?: NA

                updateLauncherList(packageName, appStatus)
            }
        }
    }

    /**
     * To Update launcher List with newly added or removed app
     * @param packageName String
     * @param appStatus AppStatus
     */
    private fun updateLauncherList(packageName: String, appStatus: AppStatus) {
        lifecycleScope.launch(Dispatchers.Main) {
            searchItem?.collapseActionView()
            appsListAdapter.reSubmitOrgList()

            if (!appsListAdapter.appInfoOrgList.isNullOrEmpty()) {
                val appsList = appsListAdapter.appInfoOrgList!!
                when (appStatus) {
                    REMOVED -> {
                        appsList.indexOfFirst {
                            it.packageName == packageName
                        }.takeIf {
                            it >= 0
                        }?.let { index ->
                            appsList.removeAt(index)
                            appsListAdapter.notifyItemRemoved(index)
                            //appsListAdapter.notifyDataSetChanged()
                            launcherViewModel.updateAppsList(appsList)
                        }
                    }
                    ADDED -> {
                        appsList.indexOfFirst {
                            it.packageName == packageName
                        }.takeIf {
                            it == -1
                        }?.apply {
                            launcherViewModel.getAppInfoFromPackage(packageName)?.let { appInfo ->
                                appsList.add(appInfo)
                                appsListAdapter.notifyItemInserted(appsList.size - 1)
                                appsList.sortBy {
                                    it.appName
                                }
                                appsListAdapter.notifyDataSetChanged()
                                launcherViewModel.updateAppsList(appsList)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * To make this activity as Launcher we disable onBackPress since we do not need to exit it and this activity screen is our Home Screen
     */
    override fun onBackPressed() {
        //super.onBackPressed()
    }

    override fun onDestroy() {
        viewModelStore.clear()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(appStatusUpdateReceiver)
        super.onDestroy()
    }
}