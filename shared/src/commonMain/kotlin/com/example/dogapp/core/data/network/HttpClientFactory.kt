package com.example.dogapp.core.data.network

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.logging.Logger as KtorLogger

const val HTTP_LOG_TAG = "HttpClient"

object HttpClientFactory {
    private const val REQUEST_TIMEOUT_MILLIS = 20_000L
    private const val CONNECT_TIMEOUT_MILLIS = 10_000L
    private const val SOCKET_TIMEOUT_MILLIS = 20_000L

    fun create(
        engine: HttpClientEngine,
        logger: Logger = Logger.withTag(HTTP_LOG_TAG),
    ): HttpClient =
        HttpClient(engine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    },
                )
            }
            install(HttpTimeout) {
                requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
                connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
                socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
            }
            install(Logging) {
                this.logger =
                    object : KtorLogger {
                        override fun log(message: String) {
                            logger.d { message }
                        }
                    }
                level = LogLevel.INFO
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
}
