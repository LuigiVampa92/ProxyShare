package com.luigivampa92.nfcshare.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface ProxyRecordDao {

    @get:Query("SELECT * FROM proxy")
    val all: Single<List<ProxyRecordEntity>>

    @Insert
    fun insert(proxy: ProxyRecordEntity): Completable

    @Delete
    fun delete(proxy: ProxyRecordEntity): Completable

}