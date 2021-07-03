//package com.jio.tesseract.launchersdk.base
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import android.os.Bundle
//import android.util.Log
//import androidx.appcompat.app.AppCompatActivity
//import com.jio.tesseract.launchersdk.AppStatus
//
//private const val TAG = "LauncherBaseActivity"
///**
// * abstract Activity for Launcher app for SDK >= 8
// * @property appStatusReceiver BroadcastReceiver
// */
//abstract class LauncherBaseActivity : AppCompatActivity() {
//
//    abstract fun onAppStatusChanged(packageName: String, appStatus: AppStatus)
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        registerAppStatusReceiver()
//    }
//
//    /**
//     * Register local broadcast to receive app Aad/Remove Status
//     */
//    private fun registerAppStatusReceiver() {
//        val filter = IntentFilter().apply {
//            addAction(Intent.ACTION_PACKAGE_ADDED)
//            addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
//            addDataScheme("package")
//        }
//        registerReceiver(appStatusReceiver, filter)
//    }
//
//    private val appStatusReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent?) {
//            intent?.data?.encodedSchemeSpecificPart?.let { packageName ->
//                Log.d(TAG, "packageName $packageName, action: ${intent.action}")
//
//                val appStatus = when (intent.action) {
//                    Intent.ACTION_PACKAGE_ADDED -> AppStatus.ADDED
//                    Intent.ACTION_PACKAGE_FULLY_REMOVED -> AppStatus.REMOVED
//                    else -> AppStatus.NA
//                }
//                notifyAppStatus(packageName, appStatus)
//            }
//        }
//    }
//
//    /**
//     *
//     * @param packageName String
//     * @param appStatus AppStatus
//     */
//    private fun notifyAppStatus(packageName: String, appStatus: AppStatus) {
//        onAppStatusChanged(packageName, appStatus)
//    }
//
//    override fun onDestroy() {
//        unregisterReceiver(appStatusReceiver)
//        super.onDestroy()
//    }
//}