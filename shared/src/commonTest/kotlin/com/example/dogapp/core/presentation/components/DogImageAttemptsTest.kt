package com.example.dogapp.core.presentation.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DogImageAttemptsTest {
    @Test
    fun eachPhotoIsTriedThenRetriedWithoutTheCacheBeforeMovingToTheNextUntilExhausted() {
        val urls = listOf("https://a.jpg", "https://b.jpg")

        assertEquals(
            listOf(
                DogImageAttempt("https://a.jpg", bypassCache = false),
                DogImageAttempt("https://a.jpg", bypassCache = true),
                DogImageAttempt("https://b.jpg", bypassCache = false),
                DogImageAttempt("https://b.jpg", bypassCache = true),
            ),
            (0..3).map { failures -> dogImageAttempt(urls, failures) },
        )
        assertTrue(dogImageAttempt(urls, failures = 4).isExhausted)
        assertTrue(dogImageAttempt(emptyList(), failures = 0).isExhausted)
    }
}
