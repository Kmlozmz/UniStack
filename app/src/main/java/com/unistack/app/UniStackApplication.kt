package com.unistack.app

import android.app.Application
import com.unistack.app.core.AppContainer

class UniStackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
