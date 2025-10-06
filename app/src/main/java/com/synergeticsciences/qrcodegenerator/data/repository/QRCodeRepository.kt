package com.synergeticsciences.qrcodegenerator.data.repository

import com.synergeticsciences.qrcodegenerator.data.database.QRCodeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

interface QRCodeRepository {

    fun getAllQRCodes(): Flow<List<QRCodeEntity>>

    fun getGeneratedQRCodes(): Flow<List<QRCodeEntity>>

    fun getScannedQRCodes(): Flow<List<QRCodeEntity>>

    fun searchQRCodes(query: String): Flow<List<QRCodeEntity>>

    suspend fun insertQRCode(qrCode: QRCodeEntity): Long

    suspend fun deleteQRCode(qrCode: QRCodeEntity)

    suspend fun deleteQRCodeById(id: Long)

    suspend fun deleteAllQRCodes()

    suspend fun getQRCodeCount(): Int

    fun getQRCodesSince(since: LocalDateTime): Flow<List<QRCodeEntity>>
}
