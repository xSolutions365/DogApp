package com.example.dogapp.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OutcomeTest {

    private val success: Outcome<Int, DataError.Remote> = Outcome.Success(2)
    private val failure: Outcome<Int, DataError.Remote> = Outcome.Failure(DataError.Remote.NotFound)

    @Test
    fun mapTransformsSuccessAndPassesFailureThrough() {
        assertEquals(Outcome.Success("2"), success.map { it.toString() })
        assertEquals(Outcome.Failure(DataError.Remote.NotFound), failure.map { it.toString() })
    }

    @Test
    fun onSuccessAndOnFailureRunOnlyForTheirCaseAndReturnTheSameOutcome() {
        var seenData: Int? = null
        var seenError: DataError.Remote? = null

        assertEquals(success, success.onSuccess { seenData = it }.onFailure { seenError = it })
        assertEquals(2, seenData)
        assertNull(seenError)

        seenData = null
        assertEquals(failure, failure.onSuccess { seenData = it }.onFailure { seenError = it })
        assertNull(seenData)
        assertEquals(DataError.Remote.NotFound, seenError)
    }
}
