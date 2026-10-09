package com.example.dogapp.feature.identify.domain

import com.example.dogapp.core.domain.DomainError

sealed interface IdentifyError : DomainError {
    data object UnreadablePhoto : IdentifyError

    data object ModelUnavailable : IdentifyError
}
