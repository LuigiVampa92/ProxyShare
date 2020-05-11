package com.luigivampa92.nfcshare.ui

import android.content.Context
import android.content.Intent
import android.nfc.NfcManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.luigivampa92.nfcshare.BuildConfig
import com.luigivampa92.nfcshare.R

abstract class BaseActivity : AppCompatActivity() {

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    protected fun deviceSupportsNfc(): Boolean {
        try {
            if (!(packageManager!!.hasSystemFeature("android.hardware.nfc"))) return false
            if (!(packageManager!!.hasSystemFeature("android.hardware.nfc.hce"))) return false
            val nfcManager = getSystemService(Context.NFC_SERVICE) as NfcManager
            if (nfcManager == null) return false
            val nfcAdapter = nfcManager.defaultAdapter
            if (nfcAdapter == null) return false
            return true
        }
        catch (e: Throwable) {
            return false
        }
    }

    protected fun deviceHasActiveNfc(): Boolean {
        try {
            if (!(packageManager!!.hasSystemFeature("android.hardware.nfc"))) return false
            if (!(packageManager!!.hasSystemFeature("android.hardware.nfc.hce"))) return false
            val nfcManager = getSystemService(Context.NFC_SERVICE) as NfcManager
            if (nfcManager == null) return false
            val nfcAdapter = nfcManager.defaultAdapter
            if (nfcAdapter == null) return false
            if (!nfcAdapter.isEnabled) return false
            return true
        }
        catch (e: Throwable) {
            return false
        }
    }

    protected fun log(message: String?) {
        message?.let {
            if (BuildConfig.LOGS_ENABLED) {
                Log.d(BuildConfig.LOG_TAG, it)
            }
        }
    }
}