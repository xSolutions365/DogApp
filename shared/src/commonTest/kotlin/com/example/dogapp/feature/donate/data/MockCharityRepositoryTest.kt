package com.example.dogapp.feature.donate.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockCharityRepositoryTest {
    private val repository = MockCharityRepository()

    @Test
    fun everyListedCharityHasAUniqueIdAndOpensItsDetail() {
        val charities = repository.charities()

        assertTrue(charities.isNotEmpty())
        assertEquals(charities.size, charities.map { it.id }.toSet().size)
        charities.forEach { assertEquals(it, repository.charity(it.id)) }
    }
}
