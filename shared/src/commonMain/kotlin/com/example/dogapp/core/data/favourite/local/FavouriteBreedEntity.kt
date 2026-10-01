package com.example.dogapp.core.data.favourite.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourite_breed")
data class FavouriteBreedEntity(
    @PrimaryKey @ColumnInfo(name = "breed_id") val breedId: String,
    val name: String,
    @ColumnInfo(name = "added_at") val addedAtEpochMillis: Long,
)
