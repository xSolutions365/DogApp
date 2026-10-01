package com.example.dogapp.feature.breeddetail.presentation

import com.example.dogapp.core.domain.breed.MeasureRange
import kotlin.math.abs
import kotlin.math.roundToInt

private const val WHOLE_NUMBER_TOLERANCE = 0.05

fun formatMeasure(value: Double): String {
    val rounded = value.roundToInt()
    if (abs(value - rounded) < WHOLE_NUMBER_TOLERANCE) return rounded.toString()
    val tenths = (value * 10).roundToInt()
    return "${tenths / 10}.${abs(tenths % 10)}"
}

fun formatRange(range: MeasureRange): String {
    val min = formatMeasure(range.min)
    val max = formatMeasure(range.max)
    return if (min == max) min else "$min–$max"
}
