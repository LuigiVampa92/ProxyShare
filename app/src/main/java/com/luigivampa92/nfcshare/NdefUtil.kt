package com.luigivampa92.nfcshare

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import java.io.UnsupportedEncodingException

object NdefUtil {

    private val NDEF_FID_DATA = byteArrayOf(0xE1.toByte(), 0x04.toByte())
    private val NDEF_MIME_TYPE_VCARD = "text/vcard".toByteArray()

    fun createNdefMessage(message: String): NdefMessage {
        return if (isRecordVcardContact(message))
            NdefMessage(createContactRecord("", message, NDEF_FID_DATA))
        else
            NdefMessage(createUriRecord("", message, NDEF_FID_DATA))
    }

    private fun isRecordVcardContact(message: String) =
        message.startsWith("BEGIN:VCARD") && message.endsWith("END:VCARD")

    private fun createUriRecord(language: String, text: String, id: ByteArray): NdefRecord {
        val languageBytes: ByteArray
        val textBytes: ByteArray
        try {
            languageBytes = language.toByteArray(charset("US-ASCII"))
            textBytes = text.toByteArray(charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            throw AssertionError(e)
        }

        val recordPayload = ByteArray(1 + (languageBytes.size and 0x03F) + textBytes.size)

        recordPayload[0] = (languageBytes.size and 0x03F).toByte()
        System.arraycopy(languageBytes, 0, recordPayload, 1, languageBytes.size and 0x03F)
        System.arraycopy(textBytes, 0, recordPayload, 1 + (languageBytes.size and 0x03F), textBytes.size)

        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, id, recordPayload)
    }

    private fun createContactRecord(language: String, text: String, id: ByteArray): NdefRecord {
        val languageBytes: ByteArray
        val textBytes: ByteArray
        try {
            languageBytes = language.toByteArray(charset("US-ASCII"))
            textBytes = text.toByteArray(charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            throw AssertionError(e)
        }

        val recordPayload = ByteArray(1 + (languageBytes.size and 0x03F) + textBytes.size)

        recordPayload[0] = (languageBytes.size and 0x03F).toByte()
        System.arraycopy(languageBytes, 0, recordPayload, 1, languageBytes.size and 0x03F)
        System.arraycopy(textBytes, 0, recordPayload, 1 + (languageBytes.size and 0x03F), textBytes.size)

        return NdefRecord(NdefRecord.TNF_MIME_MEDIA, NDEF_MIME_TYPE_VCARD, id, recordPayload)
    }
}