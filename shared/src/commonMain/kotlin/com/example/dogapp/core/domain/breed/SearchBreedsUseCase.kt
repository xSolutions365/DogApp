package com.example.dogapp.core.domain.breed

class SearchBreedsUseCase {
    operator fun invoke(
        breeds: List<Breed>,
        query: String,
    ): List<Breed> {
        val queryTokens = BreedNameNormalizer.normalize(query).split(' ').filter { it.isNotEmpty() }
        if (queryTokens.isEmpty()) return breeds
        return breeds.filter { breed ->
            val nameTokens = breed.id.split(' ')
            queryTokens.all { queryToken -> nameTokens.any { nameToken -> nameToken.startsWith(queryToken) } }
        }
    }
}
