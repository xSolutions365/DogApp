package com.example.dogapp.core.data.network

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import com.example.dogapp.testing.RecordingLogWriter
import kotlin.test.Test
import kotlin.test.assertTrue

class HttpClientFactoryTest {

    @Test
    fun requestsAreLoggedThroughKermitUnderHttpTagWithoutCredentials() = runTest {
        val testLogWriter = RecordingLogWriter()
        val client = HttpClientFactory.create(
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
}
