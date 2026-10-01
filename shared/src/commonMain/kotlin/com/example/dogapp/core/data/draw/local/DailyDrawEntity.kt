package com.example.dogapp.core.data.draw.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_draw")
data class DailyDrawEntity(
    @PrimaryKey val day: String,
    @ColumnInfo(name = "breed_id") val breedId: String,
    val name: String,
    @ColumnInfo(name = "drawn_at") val drawnAtEpochMillis: Long,
)
