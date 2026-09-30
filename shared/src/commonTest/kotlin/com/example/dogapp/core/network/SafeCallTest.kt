package com.example.dogapp.core.network

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

    private suspend fun HttpClient.fetchPayload(): NetworkResult<Payload> =
        safeCall { get("https://example.test/payload") }

    @Test
    fun successfulResponseDecodesBodyIgnoringUnknownKeys() = runTest {
        val client = clientResponding(HttpStatusCode.OK, """{"id":1,"name":"Rex","extra":true}""")

        assertEquals(NetworkResult.Success(Payload(1, "Rex")), client.fetchPayload())
    }

    @Test
    fun malformedSuccessfulBodyMapsToSerializationError() = runTest {
        val client = clientResponding(HttpStatusCode.OK, """{"id":"not-a-number"}""")

        assertEquals(NetworkResult.Failure(NetworkError.Serialization), client.fetchPayload())
    }

    @Test
    fun errorStatusCodesMapToNetworkErrors() = runTest {
        val expectations = mapOf(
            HttpStatusCode.Unauthorized to NetworkError.Unauthorized,
            HttpStatusCode.NotFound to NetworkError.NotFound,
            HttpStatusCode.RequestTimeout to NetworkError.RequestTimeout,
            HttpStatusCode.TooManyRequests to NetworkError.TooManyRequests,
            HttpStatusCode.BadRequest to NetworkError.Client(400),
            HttpStatusCode.Conflict to NetworkError.Client(409),
            HttpStatusCode.InternalServerError to NetworkError.Server(500),
            HttpStatusCode.ServiceUnavailable to NetworkError.Server(503),
        )

        expectations.forEach { (status, expectedError) ->
            assertEquals(
                NetworkResult.Failure(expectedError),
                clientResponding(status).fetchPayload(),
                "Unexpected mapping for $status",
            )
        }
    }

    @Test
    fun transportFailuresMapToNetworkErrors() = runTest {
        val expectations = mapOf(
            HttpRequestTimeoutException("https://example.test/payload", 1L) to NetworkError.RequestTimeout,
            IOException("offline") to NetworkError.NoConnection,
            IllegalStateException("boom") to NetworkError.Unknown,
        )

        expectations.forEach { (error, expectedError) ->
            assertEquals(
                NetworkResult.Failure(expectedError),
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
