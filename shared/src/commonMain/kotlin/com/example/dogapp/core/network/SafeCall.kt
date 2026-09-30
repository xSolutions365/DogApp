package com.example.dogapp.core.network

import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.ContentConvertException
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException

suspend inline fun <reified T> safeCall(execute: () -> HttpResponse): NetworkResult<T> {
    val response = try {
        execute()
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpRequestTimeoutException) {
        return NetworkResult.Failure(NetworkError.RequestTimeout)
    } catch (e: SocketTimeoutException) {
        return NetworkResult.Failure(NetworkError.RequestTimeout)
    } catch (e: IOException) {
        return NetworkResult.Failure(NetworkError.NoConnection)
    } catch (e: Exception) {
        return NetworkResult.Failure(NetworkError.Unknown)
    }
    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(response: HttpResponse): NetworkResult<T> =
    when (val statusCode = response.status.value) {
        in 200..299 -> try {
            NetworkResult.Success(response.body<T>())
        } catch (e: NoTransformationFoundException) {
            NetworkResult.Failure(NetworkError.Serialization)
        } catch (e: ContentConvertException) {
            NetworkResult.Failure(NetworkError.Serialization)
        }
        401 -> NetworkResult.Failure(NetworkError.Unauthorized)
        404 -> NetworkResult.Failure(NetworkError.NotFound)
        408 -> NetworkResult.Failure(NetworkError.RequestTimeout)
        429 -> NetworkResult.Failure(NetworkError.TooManyRequests)
        in 400..499 -> NetworkResult.Failure(NetworkError.Client(statusCode))
        in 500..599 -> NetworkResult.Failure(NetworkError.Server(statusCode))
        else -> NetworkResult.Failure(NetworkError.Unknown)
    }
