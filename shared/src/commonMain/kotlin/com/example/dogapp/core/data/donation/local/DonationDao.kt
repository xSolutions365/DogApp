package com.example.dogapp.core.data.donation.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DonationDao {
    @Query("SELECT * FROM donation ORDER BY donated_at DESC")
    fun observeAll(): Flow<List<DonationEntity>>

    @Insert
    suspend fun insert(entity: DonationEntity)
}
