package com.luigivampa92.nfcshare.hce

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import com.luigivampa92.nfcshare.BuildConfig
import com.luigivampa92.nfcshare.DataStorage
import com.luigivampa92.nfcshare.DataUtil

class NdefHceService : HostApduService() {

    private val logTransmission = BuildConfig.LOGS_ENABLED
    private var dataStorage: DataStorage? = null
    private var message: String? = null
    private var hceActor: ApduExecutor? = null

    override fun onDeactivated(reason: Int) {
        dataStorage = null
        message = null
        hceActor = null
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray? {
        try {
            if (dataStorage == null || hceActor == null) {
                dataStorage = DataStorage(this)
                if (dataStorage != null && dataStorage!!.isMessagePersisted()) {
                    message = dataStorage?.getMessage()
                    if (message != null) {
                        this.hceActor = ShareNdefActor(message!!)
                    }
                    else {
                        return ApduConstants.SW_ERROR_NO_DATA_PERSISTED
                    }
                }
                else {
                    return ApduConstants.SW_ERROR_NO_DATA_PERSISTED
                }
            }
            if (!dataStorage!!.isHceEnabled()) {
                return ApduConstants.SW_ERROR_HCE_IS_NOT_ENABLED
            }
            if (commandApdu == null) {
                return ApduConstants.SW_ERROR_INPUT_DATA_ABSENT
            }
            if (logTransmission) {
                log("RX: " + DataUtil.toHexString(commandApdu))
            }
            val response = hceActor?.transmitApdu(commandApdu)
            if (logTransmission) {
                log("TX: " + DataUtil.toHexString(response))
            }
            return if (response == null || response.isEmpty()) {
                return ApduConstants.SW_ERROR_OUTPUT_DATA_ABSENT
            } else response
        } catch (e: Exception) {
            return ApduConstants.SW_ERROR_GENERAL
        }
    }

    private fun log(message: String) {
        if (logTransmission) {
            Log.d(BuildConfig.LOG_TAG, message)
        }
    }
}