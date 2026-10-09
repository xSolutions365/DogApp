package com.example.dogapp.testing

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.identify.domain.BreedClassifier
import com.example.dogapp.feature.identify.domain.BreedGuess
import com.example.dogapp.feature.identify.domain.IdentifyError
import kotlinx.coroutines.CompletableDeferred

class FakeBreedClassifier : BreedClassifier {
    val results = ArrayDeque<CompletableDeferred<Outcome<List<BreedGuess>, IdentifyError>>>()
    val photos = mutableListOf<ByteArray>()

    override suspend fun classify(photo: ByteArray): Outcome<List<BreedGuess>, IdentifyError> {
        photos += photo
        return results.removeFirst().await()
    }

    fun returns(outcome: Outcome<List<BreedGuess>, IdentifyError>) {
        results += CompletableDeferred(outcome)
    }
}
