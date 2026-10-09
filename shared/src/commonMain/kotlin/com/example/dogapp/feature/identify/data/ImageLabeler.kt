package com.example.dogapp.feature.identify.data

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.identify.domain.IdentifyError

interface ImageLabeler {
    suspend fun label(photo: ByteArray): Outcome<Map<String, Float>, IdentifyError>
}
