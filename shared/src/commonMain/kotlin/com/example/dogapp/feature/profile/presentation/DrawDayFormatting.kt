package com.example.dogapp.feature.profile.presentation

import com.example.dogapp.core.domain.draw.DrawDay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

private val drawDayFormat =
    LocalDate.Format {
        day(padding = Padding.NONE)
        char(' ')
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        char(' ')
        year()
    }

fun DrawDay.toDisplayText(): String = drawDayFormat.format(LocalDate.parse(isoDate))
