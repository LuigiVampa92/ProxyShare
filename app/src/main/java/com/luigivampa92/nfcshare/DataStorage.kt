package com.luigivampa92.nfcshare

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

@SuppressLint("ApplySharedPref")
class DataStorage(context: Context) {

    companion object {
        private const val PREF_NAME = "nfc_ndef_data"
        private const val KEY_HCE_ENABLED = "hce_enabled"
        private const val KEY_PERSISTED_NDEF_MSG = "message"
    }

    private val preferences: SharedPreferences

    init {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isMessagePersisted(): Boolean {
        val map = preferences.all
        return map != null
                && map.containsKey(KEY_HCE_ENABLED)
                && map.containsKey(KEY_PERSISTED_NDEF_MSG)
    }

    fun getMessage(): String? =
        try {
            preferences.getString(KEY_PERSISTED_NDEF_MSG, null)
        } catch (e: Exception) {
            wipe()
            null
        }

    fun setMessage(message: String) {
        try {
            preferences.edit()
                .putString(KEY_PERSISTED_NDEF_MSG, message)
                .commit()
        } catch (e: Exception) {
            wipe()
        }
    }

    fun isHceEnabled() = preferences.getBoolean(KEY_HCE_ENABLED, false)

    fun setHceEnabled(enable: Boolean) {
        preferences.edit()
            .putBoolean(KEY_HCE_ENABLED, enable)
            .commit()
    }

    fun wipe() {
        preferences.edit().clear().commit()
    }
}
