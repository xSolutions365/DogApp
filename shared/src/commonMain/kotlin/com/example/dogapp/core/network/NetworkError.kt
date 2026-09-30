package com.example.dogapp.core.network

sealed interface NetworkError {
    data object RequestTimeout : NetworkError
    data object NoConnection : NetworkError
    data object Unauthorized : NetworkError
    data object NotFound : NetworkError
    data object TooManyRequests : NetworkError
    data object Serialization : NetworkError
    data class Client(val statusCode: Int) : NetworkError
    data class Server(val statusCode: Int) : NetworkError
    data object Unknown : NetworkError
}
