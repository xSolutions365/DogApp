package com.example.dogapp.feature.donate.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DonationAmountTest {
    @Test
    fun otherAmountIsReadAsPoundsAndPenceWithinOneToFiveHundredPounds() {
        assertEquals(DonationAmount(750), DonationAmount.fromPounds("7.50"))
        assertEquals(DonationAmount(750), DonationAmount.fromPounds("£7.5"))
        assertEquals(DonationAmount(100), DonationAmount.fromPounds(" 1 "))
        assertEquals(DonationAmount(50_000), DonationAmount.fromPounds("500"))

        listOf("0.99", "500.01", "7.505", "7,50", "1.2.3", "", "abc", "99999999999").forEach { text ->
            assertNull(DonationAmount.fromPounds(text), text)
        }
    }

    @Test
    fun presetsAreThreeFiveTenAndTwentyPounds() {
        assertEquals(listOf(300, 500, 1000, 2000), DonationAmount.presets.map { it.pence })
    }
}
