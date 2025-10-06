package com.synergeticsciences.qrcodegenerator.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime

@Entity(tableName = "qr_codes")
data class QRCodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val type: QRCodeType,
    val timestamp: LocalDateTime,
    val isGenerated: Boolean = true, // true for generated, false for scanned
)

enum class QRCodeType {
    TEXT,
    URL,
    WIFI,
    CONTACT,
    EMAIL,
    PHONE,
    SMS,
    GEO,
    OTHER
}
