package com.example.dogapp.core.domain

sealed interface Outcome<out D, out E : DomainError> {
    data class Success<out D>(val data: D) : Outcome<D, Nothing>
    data class Failure<out E : DomainError>(val error: E) : Outcome<Nothing, E>
}

inline fun <D, E : DomainError, R> Outcome<D, E>.map(transform: (D) -> R): Outcome<R, E> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(data))
    is Outcome.Failure -> this
}

inline fun <D, E : DomainError> Outcome<D, E>.onSuccess(action: (D) -> Unit): Outcome<D, E> {
    if (this is Outcome.Success) action(data)
    return this
}

inline fun <D, E : DomainError> Outcome<D, E>.onFailure(action: (E) -> Unit): Outcome<D, E> {
    if (this is Outcome.Failure) action(error)
    return this
}
