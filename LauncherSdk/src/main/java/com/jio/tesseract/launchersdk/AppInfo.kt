package com.jio.tesseract.launchersdk

import android.graphics.drawable.Drawable

data class AppInfo(
    val appName: String,
    val packageName: String,
    val mainActivityClassName: String,
    val versionCode: Int,
    val versionName: String,
    val appDrawable: Drawable
)

enum class AppStatus {
    NA,
    ADDED,
    REMOVED
}
