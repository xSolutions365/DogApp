package com.example.dogapp.core.data.network

import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SafeCallTest {

    @Serializable
    private data class Payload(val id: Int, val name: String)

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun clientResponding(status: HttpStatusCode, body: String = ""): HttpClient =
        HttpClientFactory.create(MockEngine { respond(body, status, jsonHeaders) })

    private fun clientThrowing(error: Throwable): HttpClient =
        HttpClientFactory.create(MockEngine { throw error })

    private suspend fun HttpClient.fetchPayload(): Outcome<Payload, DataError.Remote> =
        safeCall { get("https://example.test/payload") }

    @Test
    fun successfulResponseDecodesBodyIgnoringUnknownKeys() = runTest {
        val client = clientResponding(HttpStatusCode.OK, """{"id":1,"name":"Rex","extra":true}""")

        assertEquals(Outcome.Success(Payload(1, "Rex")), client.fetchPayload())
    }

    @Test
    fun malformedSuccessfulBodyMapsToSerializationError() = runTest {
        val client = clientResponding(HttpStatusCode.OK, """{"id":"not-a-number"}""")

        assertEquals(Outcome.Failure(DataError.Remote.Serialization), client.fetchPayload())
    }

    @Test
    fun errorStatusCodesMapToRemoteDataErrors() = runTest {
        val expectations = mapOf(
            HttpStatusCode.Unauthorized to DataError.Remote.Unauthorized,
            HttpStatusCode.NotFound to DataError.Remote.NotFound,
            HttpStatusCode.RequestTimeout to DataError.Remote.RequestTimeout,
            HttpStatusCode.TooManyRequests to DataError.Remote.TooManyRequests,
            HttpStatusCode.BadRequest to DataError.Remote.Client(400),
            HttpStatusCode.Conflict to DataError.Remote.Client(409),
            HttpStatusCode.InternalServerError to DataError.Remote.Server(500),
            HttpStatusCode.ServiceUnavailable to DataError.Remote.Server(503),
        )

        expectations.forEach { (status, expectedError) ->
            assertEquals(
                Outcome.Failure(expectedError),
                clientResponding(status).fetchPayload(),
                "Unexpected mapping for $status",
            )
        }
    }

    @Test
    fun transportFailuresMapToRemoteDataErrors() = runTest {
        val expectations = mapOf(
            HttpRequestTimeoutException("https://example.test/payload", 1L) to DataError.Remote.RequestTimeout,
            IOException("offline") to DataError.Remote.NoConnection,
            IllegalStateException("boom") to DataError.Remote.Unknown,
        )

        expectations.forEach { (error, expectedError) ->
            assertEquals(
                Outcome.Failure(expectedError),
                clientThrowing(error).fetchPayload(),
                "Unexpected mapping for ${error::class.simpleName}",
            )
        }
    }

    @Test
    fun cancellationIsNotSwallowed() = runTest {
        assertFailsWith<CancellationException> {
            clientThrowing(CancellationException("cancelled")).fetchPayload()
        }
    }
}
