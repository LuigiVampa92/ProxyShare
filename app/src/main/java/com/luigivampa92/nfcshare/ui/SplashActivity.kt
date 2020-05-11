package com.luigivampa92.nfcshare.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashActivity : BaseActivity() {

    companion object {
        private const val SPLASH_DELAY = 400L
    }

    private val navigateHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigateHandler.postDelayed(this::process, SPLASH_DELAY)
    }

    override fun onDestroy() {
        navigateHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun process() {
        finishAffinity()
        if (isFurtherWorkPossible()) {
            startActivity(MainActivity.newIntent(this, intent.data))
        }
        else {
            startActivity(Intent(this, ErrorActivity::class.java))
        }
    }

    private fun isFurtherWorkPossible() = deviceSupportsNfc()

}