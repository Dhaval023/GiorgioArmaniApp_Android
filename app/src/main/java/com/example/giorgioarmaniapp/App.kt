package com.example.giorgioarmaniapp

import android.app.Application
import com.example.giorgioarmaniapp.helper.base.Settings

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Settings.init(this)
    }
}