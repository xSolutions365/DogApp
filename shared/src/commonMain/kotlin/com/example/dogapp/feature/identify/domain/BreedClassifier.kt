package com.example.dogapp.feature.identify.domain

import com.example.dogapp.core.domain.Outcome

interface BreedClassifier {
    suspend fun classify(photo: ByteArray): Outcome<List<BreedGuess>, IdentifyError>
}
