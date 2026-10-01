package com.example.dogapp.core.data.network

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import com.example.dogapp.testing.RecordingLogWriter
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HttpClientFactoryTest {
    @Test
    fun requestsAreLoggedThroughKermitUnderHttpTagWithoutCredentials() =
        runTest {
            val testLogWriter = RecordingLogWriter()
            val client =
                HttpClientFactory.create(
                    engine = MockEngine { respond("", HttpStatusCode.OK) },
                    logger = Logger(config = loggerConfigInit(testLogWriter), tag = HTTP_LOG_TAG),
                )

            client.get("https://example.test/dogs") { header(HttpHeaders.Authorization, "Bearer secret-token") }

            val logs = testLogWriter.entries.toList()
            assertTrue(logs.isNotEmpty(), "Expected HTTP logs")
            assertTrue(logs.all { it.tag == HTTP_LOG_TAG && it.severity == Severity.Debug })
            assertTrue(logs.any { "https://example.test/dogs" in it.message }, "Request URL not logged: $logs")
            assertTrue(logs.none { "secret-token" in it.message }, "Credentials leaked into logs: $logs")
        }

    private fun MockRequestHandleScope.recording(
        requests: MutableList<HttpRequestData>,
        request: HttpRequestData,
    ) = respond("", HttpStatusCode.OK).also { requests += request }

    private fun HttpRequestData.contentTypeHeader(): String? = headers[HttpHeaders.ContentType] ?: body.contentType?.toString()

    @Test
    fun imageClientLogsUnderItsOwnTagWithoutApiRequestDefaults() =
        runTest {
            val apiRequests = mutableListOf<HttpRequestData>()
            val imageRequests = mutableListOf<HttpRequestData>()
            val imageLogWriter = RecordingLogWriter()
            val apiClient = HttpClientFactory.create(engine = MockEngine { recording(apiRequests, it) })
            val imageClient =
                HttpClientFactory.createForImages(
                    engine = MockEngine { recording(imageRequests, it) },
                    logger = Logger(config = loggerConfigInit(imageLogWriter), tag = IMAGE_HTTP_LOG_TAG),
                )

            apiClient.get("https://api.example.test/dogs")
            imageClient.get("https://images.example.test/dog.png")

            assertEquals("application/json", apiRequests.single().contentTypeHeader())
            assertNull(imageRequests.single().contentTypeHeader(), "Image requests must not inherit API request defaults")
            assertTrue(imageLogWriter.entries.isNotEmpty(), "Expected image HTTP logs")
            assertTrue(imageLogWriter.entries.all { it.tag == IMAGE_HTTP_LOG_TAG })
            assertTrue(imageLogWriter.entries.any { "https://images.example.test/dog.png" in it.message })
        }
}
