package com.example.dogapp.core.data.network

import com.example.dogapp.core.domain.DataError
import com.example.dogapp.core.domain.Outcome
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.ContentConvertException
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException

suspend inline fun <reified T> safeCall(execute: () -> HttpResponse): Outcome<T, DataError.Remote> {
    val response = try {
        execute()
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpRequestTimeoutException) {
        return Outcome.Failure(DataError.Remote.RequestTimeout)
    } catch (e: SocketTimeoutException) {
        return Outcome.Failure(DataError.Remote.RequestTimeout)
    } catch (e: IOException) {
        return Outcome.Failure(DataError.Remote.NoConnection)
    } catch (e: Exception) {
        return Outcome.Failure(DataError.Remote.Unknown)
    }
    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(response: HttpResponse): Outcome<T, DataError.Remote> =
    when (val statusCode = response.status.value) {
        in 200..299 -> try {
            Outcome.Success(response.body<T>())
        } catch (e: NoTransformationFoundException) {
            Outcome.Failure(DataError.Remote.Serialization)
        } catch (e: ContentConvertException) {
            Outcome.Failure(DataError.Remote.Serialization)
        }
        401 -> Outcome.Failure(DataError.Remote.Unauthorized)
        404 -> Outcome.Failure(DataError.Remote.NotFound)
        408 -> Outcome.Failure(DataError.Remote.RequestTimeout)
        429 -> Outcome.Failure(DataError.Remote.TooManyRequests)
        in 400..499 -> Outcome.Failure(DataError.Remote.Client(statusCode))
        in 500..599 -> Outcome.Failure(DataError.Remote.Server(statusCode))
        else -> Outcome.Failure(DataError.Remote.Unknown)
    }
