package com.example.dogapp.feature.identify.presentation

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.identify.domain.BreedGuess
import com.example.dogapp.feature.identify.domain.IdentifyBreedUseCase
import com.example.dogapp.feature.identify.domain.IdentifyError
import com.example.dogapp.testing.FakeBreedClassifier
import com.example.dogapp.testing.FakeBreedRepository
import com.example.dogapp.testing.testBreed
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.identify_error_unreadable
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ExperimentalCoroutinesApi::class)
class IdentifyViewModelTest {
    private val akita = testBreed("akita", imageUrls = listOf("https://akita.jpg"))
    private val classifier = FakeBreedClassifier()
    private val viewModel = IdentifyViewModel(IdentifyBreedUseCase(classifier, FakeBreedRepository(listOf(akita))))

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun pickedPhotoIsShownWhileItIsIdentifiedThenItsMatchesWithPercentages() {
        val photo = byteArrayOf(1, 2, 3)
        val result = CompletableDeferred<Outcome<List<BreedGuess>, IdentifyError>>().also { classifier.results += it }

        viewModel.onAction(IdentifyAction.PhotoPick(photo))

        assertSame(photo, viewModel.state.value.photo)
        assertEquals(IdentifyContent.Identifying, viewModel.state.value.content)

        result.complete(
            Outcome.Success(
                listOf(
                    BreedGuess("akita", "Akita", 0.456f),
                    BreedGuess("eskimo dog", "Eskimo dog", 0.2f),
                ),
            ),
        )

        assertSame(photo, viewModel.state.value.photo)
        assertEquals(
            IdentifyContent.Matches(
                isConfident = false,
                matches =
                    listOf(
                        BreedMatch(breedId = "akita", name = "Akita", confidencePercent = 46, imageUrls = listOf("https://akita.jpg")),
                        BreedMatch(breedId = null, name = "Eskimo dog", confidencePercent = 20, imageUrls = emptyList()),
                    ),
            ),
            viewModel.state.value.content,
        )
    }

    @Test
    fun aNewPhotoReplacesThePreviousOneAndItsResult() {
        val first = CompletableDeferred<Outcome<List<BreedGuess>, IdentifyError>>().also { classifier.results += it }
        classifier.returns(Outcome.Failure(IdentifyError.UnreadablePhoto))
        val second = byteArrayOf(2)

        viewModel.onAction(IdentifyAction.PhotoPick(byteArrayOf(1)))
        viewModel.onAction(IdentifyAction.PhotoPick(second))
        first.complete(Outcome.Success(listOf(BreedGuess("akita", "Akita", 0.9f))))

        assertSame(second, viewModel.state.value.photo)
        assertEquals(IdentifyContent.Error(Res.string.identify_error_unreadable), viewModel.state.value.content)
    }
}
