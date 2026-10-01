package com.example.dogapp.core.data.breed.thedogapi

import com.example.dogapp.core.domain.breed.MeasureRange

private val number = Regex("""\d+(?:\.\d+)?""")

fun parseTheDogApiRange(text: String?): MeasureRange? {
    val values = text?.let { number.findAll(it).map { match -> match.value.toDouble() }.toList() }.orEmpty()
    return if (values.size < 2) null else MeasureRange(min = values.min(), max = values.max())
}
