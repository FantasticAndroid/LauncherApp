package com.jio.tesseract.launchersdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class AppStatusReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        intent?.data?.encodedSchemeSpecificPart?.let { packageName ->
            Log.d("AppStatusReceiver", "packageName $packageName, action: ${intent.action}")

            LocalBroadcastManager.getInstance(context)
                .sendBroadcast(Intent(Constant.ACTION_BROADCAST_APP_STATUS).apply {
                    putExtra(Constant.KEY_BUNDLE_APP_PACKAGE_NAME, packageName)
                    val appStatus = when (intent.action) {
                        Intent.ACTION_PACKAGE_ADDED -> AppStatus.ADDED
                        Intent.ACTION_PACKAGE_FULLY_REMOVED -> AppStatus.REMOVED
                        else -> AppStatus.NA
                    }
                    putExtra(Constant.KEY_BUNDLE_APP_STATUS, appStatus)
                })
        }
    }
}