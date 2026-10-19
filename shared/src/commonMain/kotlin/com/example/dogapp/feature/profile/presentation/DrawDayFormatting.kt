package com.example.dogapp.feature.profile.presentation

import com.example.dogapp.core.domain.draw.DrawDay
import com.example.dogapp.core.presentation.toDisplayText
import kotlinx.datetime.LocalDate

fun DrawDay.toDisplayText(): String = LocalDate.parse(isoDate).toDisplayText()
