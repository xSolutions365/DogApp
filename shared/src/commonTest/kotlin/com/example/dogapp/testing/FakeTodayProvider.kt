package com.example.dogapp.testing

import com.example.dogapp.core.domain.draw.DrawDay
import com.example.dogapp.core.domain.draw.TodayProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeTodayProvider(
    initial: String,
) : TodayProvider {
    val day = MutableStateFlow(DrawDay(initial))

    override fun today(): DrawDay = day.value

    override fun observeToday(): StateFlow<DrawDay> = day
}
