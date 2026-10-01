package com.example.dogapp.core.data.draw.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyDrawDao {
    @Query("SELECT * FROM daily_draw ORDER BY day DESC")
    fun observeAll(): Flow<List<DailyDrawEntity>>

    @Query("SELECT * FROM daily_draw WHERE day = :day")
    fun observe(day: String): Flow<DailyDrawEntity?>

    @Query("SELECT * FROM daily_draw WHERE day = :day")
    suspend fun get(day: String): DailyDrawEntity?

    @Query("SELECT breed_id FROM daily_draw")
    suspend fun drawnBreedIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: DailyDrawEntity)

    @Transaction
    suspend fun insertOrGetExisting(entity: DailyDrawEntity): DailyDrawEntity {
        insert(entity)
        return requireNotNull(get(entity.day))
    }
}
