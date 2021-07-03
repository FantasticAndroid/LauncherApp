package com.jio.tesseract.launcher.hilt

import androidx.appcompat.app.AppCompatActivity
import com.jio.tesseract.launcher.base.BaseActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {

    @Provides
    fun provideBaseActivity(activity: AppCompatActivity): BaseActivity {
        return activity as BaseActivity
    }
}