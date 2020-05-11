package com.luigivampa92.nfcshare.ui

import android.os.Bundle
import com.luigivampa92.nfcshare.R

class ErrorActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)
    }
}