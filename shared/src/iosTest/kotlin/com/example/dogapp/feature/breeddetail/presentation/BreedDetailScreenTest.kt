package com.example.dogapp.feature.breeddetail.presentation

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedSourceId
import com.example.dogapp.core.domain.breed.BreedTrait
import com.example.dogapp.core.domain.breed.MeasureRange
import com.example.dogapp.core.presentation.theme.DogAppTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class BreedDetailScreenTest {
    private val fullBreed =
        Breed(
            id = "border collie",
            name = "Border Collie",
            weightKg = MeasureRange(14.0, 25.0),
            heightCm = MeasureRange(46.0, 56.0),
            lifeSpanYears = MeasureRange(12.0, 15.0),
            temperament = listOf("Intelligent", "energetic"),
            breedGroup = "Herding",
            origin = "Anglo-Scottish border region",
            description = "Medium-sized herding dog.",
            history = "Developed by shepherds.",
            traits = mapOf(BreedTrait.GoodWithChildren to 3, BreedTrait.Grooming to 3, BreedTrait.Energy to 5),
            imageUrls = listOf("https://a.jpg", "https://b.jpg"),
            sources = listOf(BreedSourceId("the-dog-api"), BreedSourceId("api-ninjas")),
            sourceNames = listOf("TheDogAPI", "API Ninjas"),
            sourceReferences = emptyMap(),
        )

    private val sparseBreed =
        fullBreed.copy(
            id = "collie",
            name = "Collie",
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
            sourceNames = listOf("API Ninjas"),
        )

    private fun heading(text: String) = isHeading() and hasText(text)

    @Test
    fun everyAvailableSectionIsShown() =
        runComposeUiTest {
            setContent {
                DogAppTheme {
                    BreedDetailScreen(
                        state = BreedDetailUiState.Loaded(fullBreed, images = fullBreed.imageUrls, isFavourite = false),
                        onAction = {},
                        onBack = {},
                    )
                }
            }
            val list = onNode(hasScrollToNodeAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))

            onNodeWithText("1 / 2").assertExists()
            listOf(
                heading("Border Collie"),
                hasText("Herding · Anglo-Scottish border region"),
                hasText("14–25 kg"),
                hasText("46–56 cm"),
                hasText("12–15 yrs"),
            ).forEach { matcher ->
                list.performScrollToNode(matcher)
                onNode(matcher).assertExists()
            }
            listOf("Temperament", "About", "History", "Family & social", "Care & grooming", "Personality").forEach { title ->
                list.performScrollToNode(heading(title))
                onNode(heading(title)).assertExists()
            }
            list.performScrollToNode(hasContentDescription("Energy, 5 out of 5"))
            list.performScrollToNode(hasText("Data from TheDogAPI, API Ninjas"))
        }

    @Test
    fun sectionsWithoutDataAreHidden() =
        runComposeUiTest {
            setContent {
                DogAppTheme {
                    BreedDetailScreen(
                        state = BreedDetailUiState.Loaded(sparseBreed, images = emptyList(), isFavourite = false),
                        onAction = {},
                        onBack = {},
                    )
                }
            }

            val list = onNode(hasScrollToNodeAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))
            list.performScrollToNode(heading("Collie"))
            onNode(heading("Collie")).assertExists()
            list.performScrollToNode(hasText("Data from API Ninjas"))
            onNodeWithText("Data from API Ninjas").assertExists()
            listOf("Temperament", "About", "History", "Family & social", "Care & grooming", "Personality").forEach { title ->
                onNode(heading(title)).assertDoesNotExist()
            }
            onNodeWithText("Weight").assertDoesNotExist()
            onNodeWithText("1 / 0").assertDoesNotExist()
        }

    @Test
    fun heartTogglesFavouriteAndBackNavigates() =
        runComposeUiTest {
            val actions = mutableListOf<BreedDetailAction>()
            var backClicks = 0
            setContent {
                DogAppTheme {
                    BreedDetailScreen(
                        state = BreedDetailUiState.Loaded(fullBreed, images = fullBreed.imageUrls, isFavourite = true),
                        onAction = { actions += it },
                        onBack = { backClicks++ },
                    )
                }
            }

            onNodeWithContentDescription("Remove from favourites").performClick()
            onNodeWithContentDescription("Back").performClick()

            assertEquals(listOf<BreedDetailAction>(BreedDetailAction.ToggleFavourite), actions)
            assertEquals(1, backClicks)
        }

    @Test
    fun missingBreedExplainsAndOffersBack() =
        runComposeUiTest {
            var backClicks = 0
            setContent { DogAppTheme { BreedDetailScreen(state = BreedDetailUiState.NotFound, onAction = {}, onBack = { backClicks++ }) } }

            onNodeWithText("This breed isn't available anymore.").assertExists()
            onNodeWithContentDescription("Back").performClick()
            assertEquals(1, backClicks)
        }
}
