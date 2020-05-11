package com.luigivampa92.nfcshare

import android.app.Application
import com.luigivampa92.nfcshare.data.AppDatabase

class NfcShareApplication : Application() {

    companion object {
        private lateinit var database: AppDatabase
        @JvmStatic fun getDatabase() = database
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
    }
}