package com.example.dogapp.core.domain

sealed interface DataError : DomainError {
    sealed interface Remote : DataError {
        data object RequestTimeout : Remote

        data object NoConnection : Remote

        data object Unauthorized : Remote

        data object NotFound : Remote

        data object TooManyRequests : Remote

        data object Serialization : Remote

        data class Client(
            val statusCode: Int,
        ) : Remote

        data class Server(
            val statusCode: Int,
        ) : Remote

        data object Unknown : Remote
    }
}
