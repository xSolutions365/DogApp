package com.example.dogapp.core.data.breed

import kotlin.test.Test
import kotlin.test.assertEquals

class MetricConversionsTest {
    @Test
    fun imperialMeasurementsConvertToMetric() {
        assertEquals(0.45359237, poundsToKilograms(1.0), 1e-9)
        assertEquals(22.6796185, poundsToKilograms(50.0), 1e-9)
        assertEquals(2.54, inchesToCentimetres(1.0), 1e-9)
        assertEquals(55.88, inchesToCentimetres(22.0), 1e-9)
    }
}
