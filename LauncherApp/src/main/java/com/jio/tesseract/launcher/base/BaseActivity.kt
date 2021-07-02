package com.jio.tesseract.launcher.base

import androidx.appcompat.app.AppCompatActivity
import com.jio.tesseract.launcher.LauncherApp
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {

    @Inject lateinit var launcherApp: LauncherApp
}