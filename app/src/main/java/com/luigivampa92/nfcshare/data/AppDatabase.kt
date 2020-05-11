package com.luigivampa92.nfcshare.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [(ProxyRecordEntity::class)],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun proxyDao(): ProxyRecordDao

    companion object {

        private var instance: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room
                    .databaseBuilder(context.applicationContext, AppDatabase::class.java, "proxy")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return instance!!
        }
    }
}
