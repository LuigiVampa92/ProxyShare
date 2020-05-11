package com.luigivampa92.nfcshare

import android.content.Context
import android.view.View
import android.widget.Toast

fun Byte.toPositiveInt() = toInt() and 0xFF

fun View.setVisibility(visibility: Boolean) {
    this.visibility = if (visibility) View.VISIBLE else View.GONE
}

fun Context.toast(message: String?) {
    message?.let {
        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
    }
}