package com.jio.tesseract.launcher.hilt

import android.content.Context
import com.jio.tesseract.launcher.LauncherApp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideLauncherApp(@ApplicationContext context: Context) = context.applicationContext as LauncherApp
}