package com.synergeticsciences.qrcodegenerator.data.database

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime

class LocalDateTimeConverter {

    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let {
            try {
                LocalDateTime.parse(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.toString()
    }
}
