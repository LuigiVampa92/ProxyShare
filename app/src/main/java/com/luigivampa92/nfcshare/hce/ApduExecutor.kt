package com.luigivampa92.nfcshare.hce

interface ApduExecutor {
    fun reset()
    fun transmitApdu(apdu: ByteArray): ByteArray
}