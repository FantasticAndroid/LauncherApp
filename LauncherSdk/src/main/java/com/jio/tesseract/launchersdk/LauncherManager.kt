package com.jio.tesseract.launchersdk

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import java.util.ArrayList
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Injectable LauncherManager to read apps info from device
 * @property context Context
 * @constructor
 */
@ViewModelScoped    // This scope can be removed if client app does not deal with MVVM pattern or changed if accessed by other than ViewModel.
class LauncherManager @Inject constructor(@ApplicationContext private val context: Context) {

    /**
     * Read all apps having launcher intent
     */
    suspend fun readAppsList(): ArrayList<AppInfo>? = withContext(Dispatchers.IO) {
        try {
            val packageManager: PackageManager = context.packageManager
            val appsList = ArrayList<AppInfo>()

            val i = Intent(Intent.ACTION_MAIN, null)
            i.addCategory(Intent.CATEGORY_LAUNCHER)

            val allApps = packageManager.queryIntentActivities(i, 0)
            for (app in allApps) {
                val packageInfo = packageManager.getPackageInfo(app.activityInfo.packageName, 0)
                appsList.add(
                    AppInfo(
                        app.loadLabel(packageManager).toString(),
                        app.activityInfo.packageName,
                        app.activityInfo.name,
                        packageInfo.versionCode,
                        packageInfo.versionName,
                        app.activityInfo.loadIcon(packageManager)
                    )
                )
            }
            appsList.sortBy {
                it.appName
            }
            appsList
        } catch (e: Exception) {
            null
        }
    }

    /**
     * All app info from package
     * @param packageName String
     * @return AppInfo?
     */
    suspend fun getAppInfoFromPackage(packageName: String): AppInfo? = withContext(Dispatchers.IO) {
        try {
            val packageManager: PackageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(packageName, 0)

            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(packageName)
            }
            packageManager.resolveActivity(intent, 0)?.let { resolveInfo ->
                AppInfo(
                    resolveInfo.loadLabel(packageManager).toString(),
                    resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.name,
                    packageInfo.versionCode,
                    packageInfo.versionName,
                    resolveInfo.activityInfo.loadIcon(packageManager)
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}