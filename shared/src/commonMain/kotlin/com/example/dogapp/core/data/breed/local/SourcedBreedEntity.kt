package com.example.dogapp.core.data.breed.local

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "sourced_breed", primaryKeys = ["source", "normalized_name"])
data class SourcedBreedEntity(
    val source: String,
    @ColumnInfo(name = "normalized_name") val normalizedName: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "source_reference") val sourceReference: String?,
    @ColumnInfo(name = "weight_kg_min") val weightKgMin: Double?,
    @ColumnInfo(name = "weight_kg_max") val weightKgMax: Double?,
    @ColumnInfo(name = "height_cm_min") val heightCmMin: Double?,
    @ColumnInfo(name = "height_cm_max") val heightCmMax: Double?,
    @ColumnInfo(name = "life_span_years_min") val lifeSpanYearsMin: Double?,
    @ColumnInfo(name = "life_span_years_max") val lifeSpanYearsMax: Double?,
    val temperament: List<String>,
    @ColumnInfo(name = "breed_group") val breedGroup: String?,
    val origin: String?,
    val description: String?,
    val history: String?,
    val traits: Map<String, Int>,
    @ColumnInfo(name = "image_urls") val imageUrls: List<String>,
)
