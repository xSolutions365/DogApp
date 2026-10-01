package com.example.dogapp.core.data.favourite.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteBreedDao {
    @Query("SELECT * FROM favourite_breed ORDER BY added_at DESC")
    fun observeAll(): Flow<List<FavouriteBreedEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favourite_breed WHERE breed_id = :breedId)")
    fun observeIsFavourite(breedId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favourite_breed WHERE breed_id = :breedId)")
    suspend fun isFavourite(breedId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: FavouriteBreedEntity)

    @Query("DELETE FROM favourite_breed WHERE breed_id = :breedId")
    suspend fun delete(breedId: String)

    @Transaction
    suspend fun toggle(entity: FavouriteBreedEntity) {
        if (isFavourite(entity.breedId)) delete(entity.breedId) else insert(entity)
    }
}
