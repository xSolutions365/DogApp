package com.example.dogapp.core.domain.draw

import com.example.dogapp.core.domain.DomainError

sealed interface DrawError : DomainError {
    data object NoBreeds : DrawError
}
