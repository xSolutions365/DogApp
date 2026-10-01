package com.example.dogapp.core.domain.breed

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchBreedsUseCaseTest {
    private fun breed(name: String) =
        Breed(
            id = BreedNameNormalizer.normalize(name),
            name = name,
            weightKg = null,
            heightCm = null,
            lifeSpanYears = null,
            temperament = emptyList(),
            breedGroup = null,
            origin = null,
            description = null,
            history = null,
            traits = emptyMap(),
            imageUrls = emptyList(),
            sources = emptyList(),
            sourceNames = emptyList(),
            sourceReferences = emptyMap(),
        )

    private val breeds = listOf(breed("Border Collie"), breed("Rough Collie"), breed("Löwchen"), breed("German Shepherd"))
    private val search = SearchBreedsUseCase()

    private fun names(query: String) = search(breeds, query).map { it.name }

    @Test
    fun queriesMatchWordPrefixesInAnyOrderIgnoringCaseAndAccents() {
        assertEquals(listOf("Border Collie", "Rough Collie"), names("collie"))
        assertEquals(listOf("Border Collie"), names("coll bord"))
        assertEquals(listOf("Border Collie"), names("  BORDER-collie "))
        assertEquals(listOf("Löwchen"), names("lowchen"))
        assertEquals(listOf("Löwchen"), names("Löw"))
        assertEquals(emptyList(), names("ollie"))
        assertEquals(emptyList(), names("poodle"))
    }

    @Test
    fun blankQueriesReturnEveryBreed() {
        assertEquals(breeds, search(breeds, ""))
        assertEquals(breeds, search(breeds, "   "))
        assertEquals(breeds, search(breeds, "--"))
    }
}
