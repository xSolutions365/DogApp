package com.example.dogapp.core.data.breed.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SourcedBreedDao {
    @Query("SELECT * FROM sourced_breed")
    fun observeAll(): Flow<List<SourcedBreedEntity>>

    @Query("DELETE FROM sourced_breed WHERE source = :source")
    suspend fun deleteBySource(source: String)

    @Query("DELETE FROM sourced_breed WHERE source NOT IN (:sources)")
    suspend fun deleteSourcesNotIn(sources: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<SourcedBreedEntity>)

    @Transaction
    suspend fun replaceSource(
        source: String,
        entities: List<SourcedBreedEntity>,
    ) {
        deleteBySource(source)
        insertAll(entities)
    }
}
