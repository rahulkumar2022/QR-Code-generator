package com.synergeticsciences.qrcodegenerator.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

@Dao
interface QRCodeDao {

    @Query("SELECT * FROM qr_codes ORDER BY timestamp DESC")
    fun getAllQRCodes(): Flow<List<QRCodeEntity>>

    @Query("SELECT * FROM qr_codes WHERE isGenerated = :isGenerated ORDER BY timestamp DESC")
    fun getQRCodesByType(isGenerated: Boolean): Flow<List<QRCodeEntity>>

    @Query("SELECT * FROM qr_codes WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchQRCodes(query: String): Flow<List<QRCodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQRCode(qrCode: QRCodeEntity): Long

    @Delete
    suspend fun deleteQRCode(qrCode: QRCodeEntity)

    @Query("DELETE FROM qr_codes WHERE id = :id")
    suspend fun deleteQRCodeById(id: Long)

    @Query("DELETE FROM qr_codes")
    suspend fun deleteAllQRCodes()

    @Query("SELECT COUNT(*) FROM qr_codes")
    suspend fun getQRCodeCount(): Int

    @Query("SELECT * FROM qr_codes WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getQRCodesSince(since: LocalDateTime): Flow<List<QRCodeEntity>>
}
