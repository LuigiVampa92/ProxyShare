package com.luigivampa92.nfcshare

import com.luigivampa92.nfcshare.hce.ApduCommand
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.Arrays
import java.util.Date
import java.util.UUID
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

object DataUtil {

    fun toHexString(array: ByteArray?): String {
        val builder = StringBuilder()
        if (array != null) {
            for (i in array.indices) {
                builder.append(String.format("%02X ", array[i]))
            }
        }

        return builder.toString()
    }

    fun toHexStringLowercase(array: ByteArray?) = toHexString(array).replace(" ", "").toLowerCase()

    internal fun isHexNumber(string: String?): Boolean {
        if (string == null) {
            throw NullPointerException("string was null")
        } else {
            var flag = true

            for (i in 0 until string.length) {
                val cc = string[i]
                if (!isHexNumber(cc.toByte())) {
                    flag = false
                    break
                }
            }

            return flag
        }
    }

    fun hexString2Bytes(string: String?): ByteArray {
        if (string == null) {
            throw NullPointerException("string was null")
        } else {
            val preparedString = string.replace(" ", "")
            val len = preparedString.length
            if (len == 0) {
                return ByteArray(0)
            } else if (len % 2 == 1) {
                throw IllegalArgumentException("string length should be an even number")
            } else {
                val ret = ByteArray(len / 2)
                val tmp = preparedString.toByteArray()

                var i = 0
                while (i < len) {
                    if (!isHexNumber(tmp[i]) || !isHexNumber(tmp[i + 1])) {
                        throw NumberFormatException("string contained invalid value")
                    }

                    ret[i / 2] = uniteBytes(tmp[i], tmp[i + 1])
                    i += 2
                }

                return ret
            }
        }
    }

    fun parseCommandApdu(apdu: ByteArray): ApduCommand {
        val origLen = apdu.size
        if (origLen < 4) {
            throw IllegalArgumentException("Invalid command length. Command is lesser than 4 bytes")
        }

        val cla = apdu[0]
        val ins = apdu[1]
        val p1 = apdu[2]
        val p2 = apdu[3]
        var Lc: ByteArray? = null
        var Le: ByteArray? = null
        var data: ByteArray? = null

        if (origLen == 4) {
            Lc = null
            Le = null
            data = null
        } else if (origLen == 5) {
            Lc = null
            Le = ByteArray(1)
            Le[0] = apdu[4]
            data = null
        } else if (origLen > 5) {
            val lcv = apdu[4].toPositiveInt()
            val fullApduTrailer = Arrays.copyOfRange(apdu, 5, origLen)

            if (fullApduTrailer.size == lcv + 1) { // then Lc is correct, Le is presented
                Lc = ByteArray(1)
                Lc[0] = apdu[4]
                Le = ByteArray(1)
                Le[0] = fullApduTrailer[fullApduTrailer.size - 1]
                data = Arrays.copyOfRange(fullApduTrailer, 0, fullApduTrailer.size - 1)
                if (data!!.size != Lc[0].toPositiveInt()) { // foolproof
                    throw RuntimeException("ApduCommand data corrupted")
                }
            } else if (fullApduTrailer.size == lcv) { // then Lc is correct, Le is not presented
                Lc = ByteArray(1)
                Lc[0] = apdu[4]
                Le = null
                data = fullApduTrailer
                if (data!!.size != Lc[0].toPositiveInt()) { // foolproof
                    throw RuntimeException("ApduCommand data corrupted")
                }
            } else {
                throw IllegalArgumentException("Invalid command length. Invalid Lc field")
            }
        }

        return ApduCommand(cla, ins, p1, p2, Lc, data, Le)
    }

    internal fun byteToBytearray(value: Byte): ByteArray {
        return byteArrayOf(value)
    }

    internal fun validateBinary(data: ByteArray?, size: Int) {
        if (data == null || data.size != size) {
            throw RuntimeException(String.format("Data size is invalid. Expected %d bytes", size))
        }
    }

    internal fun validateStringLTE(string: String?, size: Int) {
        if (string == null || string.length > size) {
            throw RuntimeException(String.format("String size is invalid. Expected %d symbols", size))
        }
    }

    fun concatByteArrays(vararg arrays: ByteArray): ByteArray {
        var len = 0
        var currentIndex = arrays.size

        for (var4 in 0 until currentIndex) {
            val array1 = arrays[var4]
            len += array1.size
        }

        val result = ByteArray(len)
        currentIndex = 0
        val var10 = arrays.size

        for (var6 in 0 until var10) {
            val array = arrays[var6]
            System.arraycopy(array, 0, result, currentIndex, array.size)
            currentIndex += array.size
        }

        return result
    }

    internal fun enxorEqualSized(a: ByteArray, b: ByteArray): ByteArray {
        val res = ByteArray(a.size)

        for (i in a.indices) {
            res[i] = (a[i] xor b[i]).toByte()
        }

        return res
    }

    internal fun enxor(a: ByteArray, b: ByteArray): ByteArray {
        if (b.size > a.size) {
            throw IllegalArgumentException("A must be longer than B")
        } else {
            val res = a.clone()
            var index = a.size - 1

            val sizediff = a.size - b.size
            while (index >= sizediff) {
                res[index] = res[index] xor b[index]
                --index
            }

            return res
        }
    }

    internal fun bitRotl48(value: Long): Long {
        val mask = -281474976710656L
        if (value and mask > 0L) {
            throw IllegalArgumentException("Long value is larger than 48 bits")
        } else {
            val rotated = java.lang.Long.rotateLeft(value, 1)
            return rotated and mask.inv() or (rotated and mask shr 48)
        }
    }

    internal fun byteRotl(bytes: ByteArray?, bytesCount: Int): ByteArray? {
        if (bytes == null) {
            return null
        } else {
            val len = bytes.size
            val rot = bytesCount % len
            return if (len > 1) concatByteArrays(Arrays.copyOfRange(bytes, rot, len), Arrays.copyOfRange(bytes, 0, rot)) else bytes
        }
    }

    internal fun byteRotr(bytes: ByteArray?, bytesCount: Int): ByteArray? {
        if (bytes == null) {
            return null
        } else {
            val len = bytes.size
            val rot = bytesCount % len
            return if (len > 1) concatByteArrays(Arrays.copyOfRange(bytes, len - rot, len), Arrays.copyOfRange(bytes, 0, len - rot)) else bytes
        }
    }

    internal fun uuidToBytes(uuid: UUID): ByteArray {
        val result = ByteArray(16)
        uuidToByteArray(uuid, result, 0, 16)
        return result
    }

    internal fun bytesToUuid(bytes: ByteArray): UUID {
        validateBinary(bytes, 16)
        return byteArrayToUuid(reverseArray(bytes), 0)
    }

    internal fun longToBytes(value: Long): ByteArray {
        val result = ByteArray(8)
        longToByteArray(value, 0, result, 0, 8)
        val data = result.clone()
        var start = 0

        var end = data.size - 1
        while (start <= end) {
            val tmp = data[start]
            data[start] = data[end]
            data[end] = tmp
            ++start
            --end
        }

        return data
    }

    internal fun bytesToLong(bytes: ByteArray): Long {
        val result = 0L
        return byteArrayToLong(bytes, 0, result, 0, 8)
    }

    internal fun datetimeToBytes(date: Date): ByteArray {
        return longToBytes(date.time)
    }

    internal fun bytesToDatetime(bytes: ByteArray): Date {
        return Date(bytesToLong(bytes))
    }

    internal fun shortToBytes(value: Int): ByteArray {
        return byteArrayOf((value shr 8 and 255).toByte(), (value and 255).toByte())
    }

    internal fun bytesToShort(bytes: ByteArray): Short {
        validateBinary(bytes, 2)
        var result = (bytes[0] and 255.toByte()).toShort()
        result = (result.toInt() shl 8).toShort()
        result = result or (bytes[1] and 255.toByte()).toShort()
        return result
    }

    internal fun splitBytes(data: ByteArray, chunkSize: Int): Array<ByteArray?> {
        val length = data.size
        val dest = arrayOfNulls<ByteArray>((length + chunkSize - 1) / chunkSize)
        var destIndex = 0
        var stopIndex = 0

        var startIndex = 0
        while (startIndex + chunkSize <= length) {
            stopIndex += chunkSize
            dest[destIndex++] = Arrays.copyOfRange(data, startIndex, stopIndex)
            startIndex += chunkSize
        }

        if (stopIndex < length) {
            dest[destIndex] = Arrays.copyOfRange(data, stopIndex, length)
        }

        return dest
    }

    internal fun stringToBytesNcs(string: String?): ByteArray? {
        return if (string == null) {
            null
        } else {
            try {
                string.toByteArray(charset("cp1251"))
            } catch (var2: UnsupportedEncodingException) {
                null
            }

        }
    }

    internal fun stringToBytesNcs(string: String?, size: Int): ByteArray? {
        return if (string == null) {
            null
        } else {
            try {
                val binary = string.toByteArray(charset("cp1251"))
                val len = binary.size
                if (len > size) {
                    null
                } else {
                    if (len == size) binary else concatByteArrays(binary, ByteArray(size - len))
                }
            } catch (var4: UnsupportedEncodingException) {
                null
            }

        }
    }

    internal fun bytesToStringNcs(bytes: ByteArray?): String? {
        return if (bytes == null) {
            null
        } else {
            try {
                val res = String(stripTrailingZeroes(bytes), Charset.forName("cp1251"))
                if (res.isEmpty()) null else res
            } catch (var2: UnsupportedEncodingException) {
                null
            }

        }
    }

    internal fun stripTrailingZeroes(bytes: ByteArray): ByteArray {
        val len = bytes.size
        if (bytes[len - 1].toInt() != 0) {
            return bytes
        } else {
            for (i in len - 1 downTo 0) {
                if (bytes[i].toInt() != 0) {
                    return Arrays.copyOfRange(bytes, 0, i + 1)
                }
            }

            return ByteArray(0)
        }
    }

    internal fun doubleToBytes(value: Double): ByteArray {
        val intValue = (value * 100.0).toInt()
        val value8b = longToBytes(intValue.toLong())
        return Arrays.copyOfRange(value8b, 4, 8)
    }

    internal fun bytesToDouble(bytes: ByteArray): Double {
        validateBinary(bytes, 4)
        val intValue = bytesToLong(concatByteArrays(ByteArray(4), bytes)).toInt()
        return intValue.toDouble() / 100.0
    }

    private fun reverseArray(data: ByteArray): ByteArray {
        val transformedData = data.clone()

        for (i in 0 until transformedData.size / 2) {
            val temp = transformedData[i]
            transformedData[i] = transformedData[transformedData.size - i - 1]
            transformedData[transformedData.size - i - 1] = temp
        }

        return transformedData
    }

    private fun uuidToByteArray(src: UUID, dst: ByteArray, dstPos: Int, nBytes: Int): ByteArray {
        if (0 == nBytes) {
            return dst
        } else if (nBytes > 16) {
            throw IllegalArgumentException("nBytes is greater than 16")
        } else {
            longToByteArray(src.mostSignificantBits, 0, dst, dstPos, if (nBytes > 8) 8 else nBytes)
            if (nBytes >= 8) {
                longToByteArray(src.leastSignificantBits, 0, dst, dstPos + 8, nBytes - 8)
            }

            return dst
        }
    }

    private fun byteArrayToUuid(src: ByteArray, srcPos: Int): UUID {
        return if (src.size - srcPos < 16) {
            throw IllegalArgumentException("Need at least 16 bytes for UUID")
        } else {
            UUID(byteArrayToLong(src, srcPos, 0L, 0, 8), byteArrayToLong(src, srcPos + 8, 0L, 0, 8))
        }
    }

    private fun byteArrayToLong(src: ByteArray, srcPos: Int, dstInit: Long, dstPos: Int, nBytes: Int): Long {
        if ((src.size != 0 || srcPos != 0) && 0 != nBytes) {
            if ((nBytes - 1) * 8 + dstPos >= 64) {
                throw IllegalArgumentException("(nBytes-1)*8+dstPos is greater or equal to than 64")
            } else {
                val data = src.clone()
                var start = 0

                var end = data.size - 1
                while (start <= end) {
                    val tmp = data[start]
                    data[start] = data[end]
                    data[end] = tmp
                    ++start
                    --end
                }

                var out = dstInit

                for (i in 0 until nBytes) {
                    val shift = i * 8 + dstPos
                    val bits = 255L and data[i + srcPos].toLong() shl shift
                    val mask = 255L shl shift
                    out = out and mask.inv() or bits
                }

                return out
            }
        } else {
            return dstInit
        }
    }

    private fun longToByteArray(src: Long, srcPos: Int, dst: ByteArray, dstPos: Int, nBytes: Int): ByteArray {
        if (0 == nBytes) {
            return dst
        } else if ((nBytes - 1) * 8 + srcPos >= 64) {
            throw IllegalArgumentException("(nBytes-1)*8+srcPos is greater or equal to than 64")
        } else {
            for (i in 0 until nBytes) {
                val shift = i * 8 + srcPos
                dst[dstPos + i] = (255L and (src shr shift)).toInt().toByte()
            }

            return dst
        }
    }

    private fun uniteBytes(src0: Byte, src1: Byte): Byte {
        var _b0 = java.lang.Byte.decode("0x" + String(byteArrayOf(src0)))
        _b0 = (_b0.toInt() shl 4).toByte()
        val _b1 = java.lang.Byte.decode("0x" + String(byteArrayOf(src1)))
        return (_b0 xor _b1).toByte()
    }

    private fun isHexNumber(value: Byte): Boolean {
        return value in 48..57 || value in 65..70 || value in 97..102
    }
}
