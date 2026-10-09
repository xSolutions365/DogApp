package com.example.dogapp.feature.identify.data

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.breed.BreedNameNormalizer
import com.example.dogapp.core.domain.map
import com.example.dogapp.feature.identify.domain.BreedClassifier
import com.example.dogapp.feature.identify.domain.BreedGuess
import com.example.dogapp.feature.identify.domain.IdentifyError

class ImageNetBreedClassifier(
    private val labeler: ImageLabeler,
) : BreedClassifier {
    override suspend fun classify(photo: ByteArray): Outcome<List<BreedGuess>, IdentifyError> =
        labeler.label(photo).map { scores ->
            scores.mapNotNull { (label, confidence) ->
                val name = label.substringBefore(',').trim()
                if (name !in imageNetDogLabels) return@mapNotNull null
                val normalized = BreedNameNormalizer.normalize(name)
                BreedGuess(
                    breedId = imageNetBreedAliases[normalized] ?: normalized,
                    name = name.replaceFirstChar(Char::uppercaseChar),
                    confidence = confidence,
                )
            }
        }
}
