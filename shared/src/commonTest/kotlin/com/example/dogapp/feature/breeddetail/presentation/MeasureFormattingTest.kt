package com.example.dogapp.feature.breeddetail.presentation

import com.example.dogapp.core.domain.breed.MeasureRange
import kotlin.test.Test
import kotlin.test.assertEquals

class MeasureFormattingTest {
    @Test
    fun measuresShowWholeNumbersOrOneDecimalAndRangesCollapseWhenEqual() {
        assertEquals("14", formatMeasure(14.0))
        assertEquals("25", formatMeasure(24.97))
        assertEquals("13.6", formatMeasure(13.6077711))
        assertEquals("0.5", formatMeasure(0.45))
        assertEquals("14–25", formatRange(MeasureRange(14.0, 25.0)))
        assertEquals("13.6–24.9", formatRange(MeasureRange(13.6077711, 24.947580350000003)))
        assertEquals("12", formatRange(MeasureRange(12.0, 12.01)))
    }
}
