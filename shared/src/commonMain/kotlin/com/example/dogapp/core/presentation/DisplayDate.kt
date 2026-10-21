package com.example.dogapp.core.presentation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

private val displayDateFormat =
    LocalDate.Format {
        day(padding = Padding.NONE)
        char(' ')
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        char(' ')
        year()
    }

fun LocalDate.toDisplayText(): String = displayDateFormat.format(this)

private val displayDateTimeFormat =
    LocalDateTime.Format {
        date(displayDateFormat)
        chars(", ")
        hour()
        char(':')
        minute()
    }

fun LocalDateTime.toDisplayText(): String = displayDateTimeFormat.format(this)
