package com.jio.tesseract.launchersdk

object Constant {
    const val ACTION_BROADCAST_APP_STATUS = "com.jio.tesseract.launchersdk.AppStatusReceiver.action"
    const val KEY_BUNDLE_APP_PACKAGE_NAME = "packageName"
    const val KEY_BUNDLE_APP_STATUS = "appStatus"
}

enum class AppStatus {
    NA,
    ADDED,
    REMOVED
}