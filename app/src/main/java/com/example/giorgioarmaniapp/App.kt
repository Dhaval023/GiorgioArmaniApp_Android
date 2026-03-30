package com.example.giorgioarmaniapp

import android.app.Application
import com.example.giorgioarmaniapp.helper.base.Settings
import com.example.giorgioarmaniapp.models.ReaderModel
import com.example.giorgioarmaniapp.ui.login_page.BaseViewModel

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Settings.init(this)
        BaseViewModel.rfidModel = ReaderModel.getInstance(this)
    }
}