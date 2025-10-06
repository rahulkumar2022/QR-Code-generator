package com.synergeticsciences.qrcodegenerator.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [QRCodeEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(LocalDateTimeConverter::class)
abstract class QRCodeDatabase : RoomDatabase() {

    abstract fun qrCodeDao(): QRCodeDao

    companion object {
        @Volatile
        private var INSTANCE: QRCodeDatabase? = null

        fun getDatabase(context: Context): QRCodeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QRCodeDatabase::class.java,
                    "qrcode_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
