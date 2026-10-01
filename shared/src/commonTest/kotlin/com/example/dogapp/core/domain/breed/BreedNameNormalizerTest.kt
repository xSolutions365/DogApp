package com.example.dogapp.core.domain.breed

import kotlin.test.Test
import kotlin.test.assertEquals

class BreedNameNormalizerTest {
    @Test
    fun namesFromEverySourceNormalizeToTheSameIdentity() {
        val expectations =
            mapOf(
                "Border Collie" to "border collie",
                "  border   collie " to "border collie",
                "BORDER-COLLIE" to "border collie",
                "Cão da Serra de Aires" to "cao da serra de aires",
                "Löwchen" to "lowchen",
                "Xoloitzcuintli (Mexican Hairless)" to "xoloitzcuintli mexican hairless",
                "Pharaoh's Hound" to "pharaoh s hound",
            )

        expectations.forEach { (raw, expected) -> assertEquals(expected, BreedNameNormalizer.normalize(raw), "For '$raw'") }
    }

    @Test
    fun subBreedsBecomeSubBreedFollowedByBreed() {
        assertEquals("border collie", BreedNameNormalizer.normalize(breed = "collie", subBreed = "border"))
        assertEquals("afghan hound", BreedNameNormalizer.normalize(breed = "hound", subBreed = "afghan"))
        assertEquals(BreedNameNormalizer.normalize("Afghan Hound"), BreedNameNormalizer.normalize(breed = "hound", subBreed = "afghan"))
    }
}
