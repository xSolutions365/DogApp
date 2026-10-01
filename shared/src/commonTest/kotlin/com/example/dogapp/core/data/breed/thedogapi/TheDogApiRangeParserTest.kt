package com.example.dogapp.core.data.breed.thedogapi

import com.example.dogapp.core.domain.breed.MeasureRange
import kotlin.test.Test
import kotlin.test.assertEquals

class TheDogApiRangeParserTest {
    @Test
    fun everyObservedMeasurementFormatParsesToAnOverallRangeOrNothing() {
        val expectations =
            mapOf(
                "14-25" to MeasureRange(14.0, 25.0),
                "12-15" to MeasureRange(12.0, 15.0),
                "Male: 48-56; Female: 46-53" to MeasureRange(46.0, 56.0),
                "Male: 22.5-27; Female: 20-24.5" to MeasureRange(20.0, 27.0),
                "up to 7" to null,
                "unknown" to null,
                "" to null,
                null to null,
            )

        expectations.forEach { (text, expected) -> assertEquals(expected, parseTheDogApiRange(text), "For '$text'") }
    }
}
