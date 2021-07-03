package com.jio.tesseract.launchersdk

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

private const val TAG = "AppEventCallback"

@ActivityScoped
class AppEventCallback @Inject constructor(private val activity: Activity) {

    private val appStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            intent?.data?.encodedSchemeSpecificPart?.let { packageName ->
                Log.d(TAG, "packageName $packageName, action: ${intent.action}")

                val appStatus = when (intent.action) {
                    Intent.ACTION_PACKAGE_ADDED -> AppStatus.ADDED
                    Intent.ACTION_PACKAGE_FULLY_REMOVED -> AppStatus.REMOVED
                    else -> AppStatus.NA
                }
                appEventListener?.invoke(packageName, appStatus)
            }
        }
    }

    private var appEventListener: ((String, AppStatus) -> Unit?)? = null

    /**
     *
     * @param appEventListener Function2<[@kotlin.ParameterName] String, [@kotlin.ParameterName] AppStatus, Unit>
     */
    fun registerAppEventCallback(appEventListener: (packageName: String, appStatus: AppStatus) -> Unit) {
        this.appEventListener = appEventListener
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
            addDataScheme("package")
        }
        activity.registerReceiver(appStatusReceiver, filter)
    }

    fun unRegisterAppEventCallback() {
        activity.unregisterReceiver(appStatusReceiver)
    }
}