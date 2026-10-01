package com.example.dogapp.core.data.draw

import com.example.dogapp.core.domain.draw.DrawDay
import com.example.dogapp.core.domain.draw.TodayProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class SystemTodayProvider(
    private val clock: Clock = Clock.System,
    private val timeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
    private val checkInterval: Duration = 1.minutes,
) : TodayProvider {
    override fun today(): DrawDay =
        DrawDay(
            clock
                .now()
                .toLocalDateTime(timeZone())
                .date
                .toString(),
        )

    override fun observeToday(): Flow<DrawDay> =
        flow {
            while (true) {
                emit(today())
                delay(checkInterval)
            }
        }.distinctUntilChanged()
}
