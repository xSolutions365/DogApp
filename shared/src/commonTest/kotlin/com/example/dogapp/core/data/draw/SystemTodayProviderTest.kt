package com.example.dogapp.core.data.draw

import com.example.dogapp.core.domain.draw.DrawDay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SystemTodayProviderTest {
    private class MutableClock(
        var now: Instant,
    ) : Clock {
        override fun now(): Instant = now
    }

    private val lateEvening = Instant.parse("2026-10-01T21:30:00Z")

    @Test
    fun todayIsTheCalendarDateInTheDevicesTimeZone() {
        val clock = MutableClock(lateEvening)

        assertEquals(DrawDay("2026-10-01"), SystemTodayProvider(clock, { TimeZone.UTC }).today())
        assertEquals(DrawDay("2026-10-02"), SystemTodayProvider(clock, { TimeZone.of("Europe/Sofia") }).today())
    }

    @Test
    fun observedDayChangesOnceMidnightPasses() =
        runTest {
            val clock = MutableClock(lateEvening)
            val provider = SystemTodayProvider(clock, { TimeZone.UTC }, checkInterval = 1.minutes)
            val days = mutableListOf<DrawDay>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { provider.observeToday().toList(days) }

            clock.now = Instant.parse("2026-10-01T23:59:00Z")
            advanceTimeBy(1.minutes)
            runCurrent()
            clock.now = Instant.parse("2026-10-02T00:00:30Z")
            advanceTimeBy(1.minutes)
            runCurrent()
            advanceTimeBy(1.minutes)
            runCurrent()

            assertEquals(listOf(DrawDay("2026-10-01"), DrawDay("2026-10-02")), days)
        }
}
