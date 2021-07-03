package com.jio.tesseract.launcher.ui.activity

import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jio.tesseract.launcher.R
import com.jio.tesseract.launcher.base.BaseActivity
import com.jio.tesseract.launcher.databinding.ActivityAppsListBinding
import com.jio.tesseract.launcher.ui.adapter.AppsListAdapter
import com.jio.tesseract.launcher.viewmodel.LauncherViewModel
import com.jio.tesseract.launchersdk.AppEventCallback
import com.jio.tesseract.launchersdk.AppInfo
import com.jio.tesseract.launchersdk.AppStatus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "AppsListingActivity"

/**
 * Launcher Activity with <category android:name="android.intent.category.HOME" />
<category android:name="android.intent.category.DEFAULT" /> so can be replaceable with device default launcher
 * @property binding ActivityAppsListBinding
 * @property launcherViewModel LauncherViewModel
 * @property appsListAdapter AppsListAdapter
 * @property searchItem MenuItem?
 */
@AndroidEntryPoint
class AppsListingActivity : BaseActivity() {

    private lateinit var binding: ActivityAppsListBinding
    private val launcherViewModel: LauncherViewModel by viewModels()
    private lateinit var appsListAdapter: AppsListAdapter
    private var searchItem: MenuItem? = null

    @Inject lateinit var appEventCallback: AppEventCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppsListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUi()
        readAppsListFromDevice()
        appEventCallback.registerAppEventCallback { packageName, appStatus ->
            onAppStatusChanged(packageName, appStatus)
        }
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
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
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
     *
     * @param packageName String
     * @param appStatus AppStatus
     */
    private fun onAppStatusChanged(packageName: String, appStatus: AppStatus) {
        Log.d(TAG, "onAppStatusChanged: packageName: $packageName, appStatus: ${appStatus.name}")
        lifecycleScope.launch(Dispatchers.Main) {
            searchItem?.collapseActionView()
            appsListAdapter.reSubmitOrgList()

            if (!appsListAdapter.appInfoOrgList.isNullOrEmpty()) {
                val appsList = appsListAdapter.appInfoOrgList!!
                when (appStatus) {
                    AppStatus.REMOVED -> {
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
                    AppStatus.ADDED -> {
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
                    AppStatus.NA -> {

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
        appEventCallback.unRegisterAppEventCallback()
        super.onDestroy()
    }
}