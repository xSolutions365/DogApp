package com.example.dogapp.core.presentation.error

import com.example.dogapp.core.domain.DataError
import kotlin.test.Test
import kotlin.test.assertEquals

class DataErrorTextTest {

    @Test
    fun everyRemoteErrorMapsToItsUserFacingMessage() {
        val expectations = mapOf(
            DataError.Remote.RequestTimeout to "error_request_timeout",
            DataError.Remote.NoConnection to "error_no_connection",
            DataError.Remote.Unauthorized to "error_unauthorized",
            DataError.Remote.NotFound to "error_not_found",
            DataError.Remote.TooManyRequests to "error_too_many_requests",
            DataError.Remote.Serialization to "error_serialization",
            DataError.Remote.Client(400) to "error_client",
            DataError.Remote.Client(409) to "error_client",
            DataError.Remote.Server(500) to "error_server",
            DataError.Remote.Server(503) to "error_server",
            DataError.Remote.Unknown to "error_unknown",
        )

        expectations.forEach { (error, expectedKey) ->
            assertEquals(expectedKey, error.toStringResource().key, "Unexpected message for $error")
        }
    }
}
