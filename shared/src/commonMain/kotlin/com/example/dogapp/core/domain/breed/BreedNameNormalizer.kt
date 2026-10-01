package com.example.dogapp.core.domain.breed

object BreedNameNormalizer {
    private val diacritics =
        mapOf(
            'à' to 'a',
            'á' to 'a',
            'â' to 'a',
            'ä' to 'a',
            'ã' to 'a',
            'å' to 'a',
            'ç' to 'c',
            'è' to 'e',
            'é' to 'e',
            'ê' to 'e',
            'ë' to 'e',
            'ì' to 'i',
            'í' to 'i',
            'î' to 'i',
            'ï' to 'i',
            'ñ' to 'n',
            'ò' to 'o',
            'ó' to 'o',
            'ô' to 'o',
            'ö' to 'o',
            'õ' to 'o',
            'ø' to 'o',
            'ù' to 'u',
            'ú' to 'u',
            'û' to 'u',
            'ü' to 'u',
            'ý' to 'y',
            'ÿ' to 'y',
        )
    private val nonAlphanumeric = Regex("[^a-z0-9]+")

    fun normalize(name: String): String =
        name
            .lowercase()
            .map { character -> diacritics[character] ?: character }
            .joinToString("")
            .replace(nonAlphanumeric, " ")
            .trim()

    fun normalize(
        breed: String,
        subBreed: String,
    ): String = normalize("$subBreed $breed")
}
