package com.luigivampa92.nfcshare.hce

import android.nfc.NdefMessage
import com.luigivampa92.nfcshare.DataUtil
import com.luigivampa92.nfcshare.NdefUtil
import com.luigivampa92.nfcshare.toPositiveInt
import java.math.BigInteger
import java.util.*

class ShareNdefActor (message: String) : ApduExecutor {

    companion object {
        private val NDEF_AID = byteArrayOf(0xD2.toByte(), 0x76.toByte(), 0x00.toByte(), 0x00.toByte(), 0x85.toByte(), 0x01.toByte(), 0x01.toByte())
        private val NDEF_FID_CC = byteArrayOf(0xE1.toByte(), 0x03.toByte())
        private val NDEF_FID_DATA = byteArrayOf(0xE1.toByte(), 0x04.toByte())
        private val NDEF_SIZE_MAX = byteArrayOf(0x00.toByte(), 0x90.toByte()) // 0x0090 == 144 bytes == sizeof(NTAG213) // 0x0378 == 888 bytes == sizeof(NTAG216)
        private val NDEF_MODE_READ = byteArrayOf(0x00.toByte())
        private val NDEF_MODE_WRITE = byteArrayOf(0xFF.toByte())
    }

    private val ndefMessage: NdefMessage
    private val ndefMessageBinary: ByteArray
    private val ndefMessageBinarySize: ByteArray

    private var adfSelected = false
    private var efCcSelected = false
    private var efDataSelected = false

    init {
        ndefMessage = NdefUtil.createNdefMessage(message)
        ndefMessageBinary = ndefMessage.toByteArray()
        ndefMessageBinarySize = fillByteArrayToFixedDimension(BigInteger.valueOf(ndefMessageBinary.size.toLong()).toByteArray(), 2)
    }

    override fun reset() {
        adfSelected = false
        efCcSelected = false
        efDataSelected = false
    }

    override fun transmitApdu(apdu: ByteArray): ByteArray {
        try {
            val apduCommand = DataUtil.parseCommandApdu(apdu)
            val cla = apduCommand.cla
            val ins = apduCommand.ins
            val p1 = apduCommand.p1
            val p2 = apduCommand.p2
            val lc = apduCommand.lc
            val le = apduCommand.le
            val data = apduCommand.data

            if (cla == 0x00.toByte()
                && ins == 0xA4.toByte()
                && p1 == 0x04.toByte()
                && p2 == 0x00.toByte()
                && lc != null && lc.size == 1 && lc[0] == 0x07.toByte()
                && data != null && data.size == lc[0].toPositiveInt()
            ) {
                return if (Arrays.equals(NDEF_AID, data)) {
                    adfSelected = true
                    ApduConstants.SW_OK
                } else {
                    ApduConstants.SW_ERROR_NO_SUCH_ADF
                }
            }

            if (cla == 0x00.toByte()
                && ins == 0xA4.toByte()
                && p1 == 0x00.toByte()
                && p2 == 0x0C.toByte()
                && lc != null && lc.size == 1 && lc[0] == 0x02.toByte()
                && le == null
                && data != null && data.size == lc[0].toPositiveInt()
                && adfSelected && !efCcSelected && !efDataSelected
            ) {
                return if (Arrays.equals(NDEF_FID_CC, data)) {
                    efCcSelected = true
                    ApduConstants.SW_OK
                } else {
                    ApduConstants.SW_ERROR_NO_SUCH_ADF
                }
            }

            if (cla == 0x00.toByte()
                && ins == 0xB0.toByte()
                && p1 == 0x00.toByte()
                && p2 == 0x00.toByte()
                && lc == null
                && data == null
                && le != null && le.size == 1 && le[0] == 0x0F.toByte()
                && adfSelected && efCcSelected && !efDataSelected
            ) {
                val ccPrefix = byteArrayOf(0x00.toByte(), 0x11.toByte(), 0x20.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x04.toByte(), 0x06.toByte())
                return DataUtil.concatByteArrays(ccPrefix, NDEF_FID_DATA, NDEF_SIZE_MAX, NDEF_MODE_READ, NDEF_MODE_WRITE, ApduConstants.SW_OK)
            }

            if (cla == 0x00.toByte()
                && ins == 0xA4.toByte()
                && p1 == 0x00.toByte()
                && p2 == 0x0C.toByte()
                && lc != null && lc.size == 1 && lc[0] == 0x02.toByte()
                && le == null
                && data != null && data.size == lc[0].toPositiveInt()
                && adfSelected // && efCcSelected && !efDataSelected // iphones send select data fid multiple times !!!
            ) {
                return if (Arrays.equals(NDEF_FID_DATA, data)) {
                    efCcSelected = false
                    efDataSelected = true
                    ApduConstants.SW_OK
                } else {
                    ApduConstants.SW_ERROR_NO_SUCH_ADF
                }
            }

            if (cla == 0x00.toByte()
                && ins == 0xB0.toByte()
                && p1 == 0x00.toByte()
                && p2 == 0x00.toByte()
                && lc == null
                && data == null
                && le != null && le.size == 1 && le[0] == 0x02.toByte()
                && adfSelected && !efCcSelected && efDataSelected
            ) {
                return DataUtil.concatByteArrays(ndefMessageBinarySize, ApduConstants.SW_OK)
            }

            if (cla == 0x00.toByte()
                && ins == 0xB0.toByte()
                && adfSelected && !efCcSelected && efDataSelected
            ) {
                val offset = apdu.sliceToInt(2..3)
                val length = apdu.sliceToInt(4..4)
                val fullResponse = DataUtil.concatByteArrays(ndefMessageBinarySize, ndefMessageBinary)
                val slicedResponse = fullResponse.sliceArray(offset until fullResponse.size)
                val realLength = if (slicedResponse.size <= length) slicedResponse.size else length
                val response = ByteArray(realLength + ApduConstants.SW_OK.size)
                System.arraycopy(slicedResponse, 0, response, 0, realLength)
                System.arraycopy(ApduConstants.SW_OK, 0, response, realLength, ApduConstants.SW_OK.size)
                return response
            }

            return ApduConstants.SW_ERROR_GENERAL
        }
        catch (e: Throwable) {
            return ApduConstants.SW_ERROR_GENERAL
        }
    }

    private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

    private fun ByteArray.toHex() : String{
        val result = StringBuffer()

        forEach {
            val octet = it.toInt()
            val firstIndex = (octet and 0xF0).ushr(4)
            val secondIndex = octet and 0x0F
            result.append(HEX_CHARS[firstIndex])
            result.append(HEX_CHARS[secondIndex])
        }

        return result.toString()
    }

    private fun ByteArray.sliceToInt(range: IntRange): Int {
        return this.sliceArray(range).toHex().toInt(16)
    }

    private fun fillByteArrayToFixedDimension(array: ByteArray, fixedSize: Int): ByteArray {
        if (array.size == fixedSize) {
            return array
        }

        val start = byteArrayOf(0x00.toByte())
        val filledArray = ByteArray(start.size + array.size)
        System.arraycopy(start, 0, filledArray, 0, start.size)
        System.arraycopy(array, 0, filledArray, start.size, array.size)
        return fillByteArrayToFixedDimension(filledArray, fixedSize)
    }
}
