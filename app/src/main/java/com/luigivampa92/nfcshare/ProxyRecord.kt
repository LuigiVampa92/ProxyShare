package com.luigivampa92.nfcshare

data class ProxyRecord (
    val address: String,
    val port: String,
    val secret: String
) {

    companion object {
        private const val PATTERN_TG = "tg://proxy?server=%s&port=%s&secret=%s"
        private const val PATTERN_HTTPS_SHORT = "https://t.me/proxy?server=%s&port=%s&secret=%s"
        private const val PATTERN_HTTPS_FULL = "https://telegram.me/proxy?server=%s&port=%s&secret=%s"
    }

    fun asTgUri() = applyDataToPattern(PATTERN_TG)

    private fun applyDataToPattern(pattern: String) = String.format(pattern, address, port, secret)

}