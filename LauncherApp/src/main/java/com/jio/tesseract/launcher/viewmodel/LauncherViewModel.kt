package com.jio.tesseract.launcher.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jio.tesseract.launcher.service.AppsListRepo
import com.jio.tesseract.launchersdk.AppInfo
import com.jio.tesseract.launchersdk.AppStatus
import com.jio.tesseract.launchersdk.AppStatus.ADDED
import com.jio.tesseract.launchersdk.AppStatus.REMOVED
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel to communicate with Repository for data communication in MVVM
 * @property appsListRepo AppsListRepo
 * @property appInfoListObserver MutableLiveData<MutableList<AppInfo>?>
 * @constructor
 */
@HiltViewModel
class LauncherViewModel @Inject constructor(private val appsListRepo: AppsListRepo) : ViewModel() {

    val appInfoListObserver = MutableLiveData<MutableList<AppInfo>?>()

    fun readAppsList() {
        viewModelScope.launch(Dispatchers.IO) {
            appInfoListObserver.postValue(appsListRepo.readAppsList())
        }
    }

    suspend fun getAppInfoFromPackage(packageName: String) = appsListRepo.getAppInfoFromPackage(packageName)

    /**
     *
     * @param appsList MutableList<AppInfo>
     */
    fun updateAppsList(appsList: MutableList<AppInfo>) {
        appInfoListObserver.value = appsList
    }
}