package com.synergeticsciences.qrcodegenerator.data.repository

import com.synergeticsciences.qrcodegenerator.data.database.QRCodeDao
import com.synergeticsciences.qrcodegenerator.data.database.QRCodeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

class QRCodeRepositoryImpl(
    private val qrCodeDao: QRCodeDao
) : QRCodeRepository {

    override fun getAllQRCodes(): Flow<List<QRCodeEntity>> = qrCodeDao.getAllQRCodes()

    override fun getGeneratedQRCodes(): Flow<List<QRCodeEntity>> = qrCodeDao.getQRCodesByType(true)

    override fun getScannedQRCodes(): Flow<List<QRCodeEntity>> = qrCodeDao.getQRCodesByType(false)

    override fun searchQRCodes(query: String): Flow<List<QRCodeEntity>> = qrCodeDao.searchQRCodes(query)

    override suspend fun insertQRCode(qrCode: QRCodeEntity): Long = qrCodeDao.insertQRCode(qrCode)

    override suspend fun deleteQRCode(qrCode: QRCodeEntity) = qrCodeDao.deleteQRCode(qrCode)

    override suspend fun deleteQRCodeById(id: Long) = qrCodeDao.deleteQRCodeById(id)

    override suspend fun deleteAllQRCodes() = qrCodeDao.deleteAllQRCodes()

    override suspend fun getQRCodeCount(): Int = qrCodeDao.getQRCodeCount()

    override fun getQRCodesSince(since: LocalDateTime): Flow<List<QRCodeEntity>> = qrCodeDao.getQRCodesSince(since)
}
