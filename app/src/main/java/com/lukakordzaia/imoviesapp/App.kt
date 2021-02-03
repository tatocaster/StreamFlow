package com.lukakordzaia.imoviesapp

import android.app.Application
import com.lukakordzaia.imoviesapp.di.generalModule
import com.lukakordzaia.imoviesapp.di.repositoryModule
import com.lukakordzaia.imoviesapp.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@App)
            modules(listOf(repositoryModule, viewModelModule, generalModule))
        }
    }
}