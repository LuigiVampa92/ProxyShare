package com.luigivampa92.nfcshare.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.luigivampa92.nfcshare.ProxyRecord

@Entity(tableName = "proxy")
class ProxyRecordEntity (
    @PrimaryKey val address: String,
    val port: String,
    val secret: String
) {
    fun toProxyRecord() = ProxyRecord(address, port, secret)
}