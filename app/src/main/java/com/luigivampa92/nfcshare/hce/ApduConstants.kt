package com.luigivampa92.nfcshare.hce

object ApduConstants {
    val SW_OK = byteArrayOf(0x90.toByte(), 0x00.toByte())
    val SW_ERROR_GENERAL = byteArrayOf(0x69.toByte(), 0x30.toByte())
    val SW_ERROR_NO_SUCH_ADF = byteArrayOf(0x69.toByte(), 0x82.toByte())
    val SW_ERROR_HCE_IS_NOT_ENABLED = byteArrayOf(0x69.toByte(), 0x83.toByte())
    val SW_ERROR_INPUT_DATA_ABSENT = byteArrayOf(0x69.toByte(), 0x8B.toByte())
    val SW_ERROR_OUTPUT_DATA_ABSENT = byteArrayOf(0x69.toByte(), 0x8C.toByte())
    val SW_ERROR_NO_DATA_PERSISTED = byteArrayOf(0x69.toByte(), 0x8D.toByte())
}