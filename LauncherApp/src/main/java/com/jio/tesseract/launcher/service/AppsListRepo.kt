package com.jio.tesseract.launcher.service

import com.jio.tesseract.launchersdk.AppInfo
import com.jio.tesseract.launchersdk.LauncherManager
import dagger.hilt.android.scopes.ViewModelScoped
import java.util.ArrayList
import javax.inject.Inject

/**
 * Repository for Data Operations
 * @property launcherManager LauncherManager
 * @constructor
 */
@ViewModelScoped
class AppsListRepo @Inject constructor(private val launcherManager: LauncherManager) {

    suspend fun readAppsList(): ArrayList<AppInfo>? = launcherManager.readAppsList()

    suspend fun getAppInfoFromPackage(packageName:String): AppInfo? = launcherManager.getAppInfoFromPackage(packageName)
}