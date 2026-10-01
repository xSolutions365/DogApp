package com.example.dogapp.core.domain.draw

import com.example.dogapp.core.domain.breed.Breed
import kotlin.jvm.JvmInline

@JvmInline
value class DrawDay(
    val isoDate: String,
)

data class DailyDraw(
    val day: DrawDay,
    val breedId: String,
    val name: String,
    val drawnAtEpochMillis: Long,
)

data class ResolvedDraw(
    val draw: DailyDraw,
    val breed: Breed?,
)
