package com.example.dogapp.core.data.breed.local

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class BreedTypeConverters {
    @TypeConverter
    fun fromStringList(values: List<String>): String = Json.encodeToString(values)

    @TypeConverter
    fun toStringList(json: String): List<String> = Json.decodeFromString(json)

    @TypeConverter
    fun fromIntMap(values: Map<String, Int>): String = Json.encodeToString(values)

    @TypeConverter
    fun toIntMap(json: String): Map<String, Int> = Json.decodeFromString(json)
}
