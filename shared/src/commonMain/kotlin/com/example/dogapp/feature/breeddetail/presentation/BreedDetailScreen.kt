package com.example.dogapp.feature.breeddetail.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dogapp.core.domain.breed.Breed
import com.example.dogapp.core.domain.breed.BreedTrait
import com.example.dogapp.core.presentation.animation.breedImageSharedKey
import com.example.dogapp.core.presentation.animation.navSharedElement
import com.example.dogapp.core.presentation.components.DogImage
import com.example.dogapp.core.presentation.components.platformBackIcon
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.detail_about
import dogapp.shared.generated.resources.detail_back
import dogapp.shared.generated.resources.detail_favourite_add
import dogapp.shared.generated.resources.detail_favourite_remove
import dogapp.shared.generated.resources.detail_height
import dogapp.shared.generated.resources.detail_history
import dogapp.shared.generated.resources.detail_life_span
import dogapp.shared.generated.resources.detail_not_found
import dogapp.shared.generated.resources.detail_photo_position
import dogapp.shared.generated.resources.detail_sources
import dogapp.shared.generated.resources.detail_temperament
import dogapp.shared.generated.resources.detail_trait_rating
import dogapp.shared.generated.resources.detail_traits_care
import dogapp.shared.generated.resources.detail_traits_personality
import dogapp.shared.generated.resources.detail_traits_social
import dogapp.shared.generated.resources.detail_value_cm
import dogapp.shared.generated.resources.detail_value_kg
import dogapp.shared.generated.resources.detail_value_years
import dogapp.shared.generated.resources.detail_weight
import dogapp.shared.generated.resources.ic_favorite_filled
import dogapp.shared.generated.resources.ic_favorite_outlined
import dogapp.shared.generated.resources.trait_barking
import dogapp.shared.generated.resources.trait_coat_length
import dogapp.shared.generated.resources.trait_drooling
import dogapp.shared.generated.resources.trait_energy
import dogapp.shared.generated.resources.trait_good_with_children
import dogapp.shared.generated.resources.trait_good_with_other_dogs
import dogapp.shared.generated.resources.trait_good_with_strangers
import dogapp.shared.generated.resources.trait_grooming
import dogapp.shared.generated.resources.trait_playfulness
import dogapp.shared.generated.resources.trait_protectiveness
import dogapp.shared.generated.resources.trait_shedding
import dogapp.shared.generated.resources.trait_trainability
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val MAX_TRAIT_RATING = 5

private data class TraitGroup(
    val title: StringResource,
    val traits: List<Pair<BreedTrait, StringResource>>,
)

private val traitGroups =
    listOf(
        TraitGroup(
            Res.string.detail_traits_social,
            listOf(
                BreedTrait.GoodWithChildren to Res.string.trait_good_with_children,
                BreedTrait.GoodWithOtherDogs to Res.string.trait_good_with_other_dogs,
                BreedTrait.GoodWithStrangers to Res.string.trait_good_with_strangers,
            ),
        ),
        TraitGroup(
            Res.string.detail_traits_care,
            listOf(
                BreedTrait.Shedding to Res.string.trait_shedding,
                BreedTrait.Grooming to Res.string.trait_grooming,
                BreedTrait.Drooling to Res.string.trait_drooling,
                BreedTrait.CoatLength to Res.string.trait_coat_length,
            ),
        ),
        TraitGroup(
            Res.string.detail_traits_personality,
            listOf(
                BreedTrait.Playfulness to Res.string.trait_playfulness,
                BreedTrait.Protectiveness to Res.string.trait_protectiveness,
                BreedTrait.Trainability to Res.string.trait_trainability,
                BreedTrait.Energy to Res.string.trait_energy,
                BreedTrait.Barking to Res.string.trait_barking,
            ),
        ),
    )

@Composable
fun BreedDetailRoot(
    breedId: String,
    onBack: () -> Unit,
    viewModel: BreedDetailViewModel = koinViewModel { parametersOf(breedId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BreedDetailScreen(state = state, onAction = viewModel::onAction, onBack = onBack)
}

@Composable
fun BreedDetailScreen(
    state: BreedDetailUiState,
    onAction: (BreedDetailAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        BreedDetailUiState.Loading -> Box(modifier.fillMaxSize())
        BreedDetailUiState.NotFound -> NotFoundContent(onBack = onBack, modifier = modifier)
        is BreedDetailUiState.Loaded -> LoadedContent(state = state, onAction = onAction, onBack = onBack, modifier = modifier)
    }
}

@Composable
private fun LoadedContent(
    state: BreedDetailUiState.Loaded,
    onAction: (BreedDetailAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val breed = state.breed
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item(key = "hero") {
            Hero(
                breedId = breed.id,
                images = state.images,
                isFavourite = state.isFavourite,
                onBack = onBack,
                onToggleFavourite = { onAction(BreedDetailAction.ToggleFavourite) },
            )
        }
        item(key = "header") { Header(breed) }
        if (breed.weightKg != null || breed.heightCm != null || breed.lifeSpanYears != null) {
            item(key = "stats") { Stats(breed) }
        }
        if (breed.temperament.isNotEmpty()) {
            item(key = "temperament") { Temperament(breed.temperament) }
        }
        breed.description?.let { description -> item(key = "about") { TextSection(Res.string.detail_about, description) } }
        breed.history?.let { history -> item(key = "history") { TextSection(Res.string.detail_history, history) } }
        traitGroups.forEach { group ->
            val ratings = group.traits.mapNotNull { (trait, label) -> breed.traits[trait]?.let { label to it } }
            if (ratings.isNotEmpty()) {
                item(key = "traits-${group.title.key}") { TraitSection(group.title, ratings) }
            }
        }
        if (breed.sourceNames.isNotEmpty()) {
            item(key = "sources") { Sources(breed.sourceNames) }
        }
    }
}

@Composable
private fun Hero(
    breedId: String,
    images: List<String>,
    isFavourite: Boolean,
    onBack: () -> Unit,
    onToggleFavourite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val failedImages = remember(breedId) { mutableStateListOf<String>() }
    val visibleImages = images.filterNot { it in failedImages }
    Box(modifier = modifier.fillMaxWidth()) {
        if (visibleImages.isEmpty()) {
            DogImage(
                urls = emptyList(),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .navSharedElement(breedImageSharedKey(breedId)),
            )
        } else {
            val pagerState = rememberPagerState { visibleImages.size }
            HorizontalPager(
                state = pagerState,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            ) { page ->
                val url = visibleImages[page]
                DogImage(
                    urls = listOf(url),
                    onLoadFailure = { failedImages += url },
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .then(if (page == 0) Modifier.navSharedElement(breedImageSharedKey(breedId)) else Modifier),
                )
            }
            if (visibleImages.size > 1) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.detail_photo_position, pagerState.currentPage + 1, visibleImages.size),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OverlayIconButton(onClick = onBack) {
                Icon(painterResource(platformBackIcon), contentDescription = stringResource(Res.string.detail_back))
            }
            FavouriteButton(isFavourite = isFavourite, onToggle = onToggleFavourite)
        }
    }
}

@Composable
private fun OverlayIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        colors =
            IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
    ) {
        content()
    }
}

@Composable
private fun FavouriteButton(
    isFavourite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(if (isFavourite) Res.string.detail_favourite_remove else Res.string.detail_favourite_add)
    IconButton(
        onClick = onToggle,
        modifier = modifier.semantics { contentDescription = description },
        colors =
            IconButtonDefaults.iconButtonColors(
                containerColor =
                    if (isFavourite) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                contentColor = if (isFavourite) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface,
            ),
    ) {
        Icon(
            painter = painterResource(if (isFavourite) Res.drawable.ic_favorite_filled else Res.drawable.ic_favorite_outlined),
            contentDescription = null,
        )
    }
}

@Composable
private fun Header(
    breed: Breed,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = breed.name, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.semantics { heading() })
        val subtitle = listOfNotNull(breed.breedGroup, breed.origin).joinToString(" · ")
        if (subtitle.isNotEmpty()) {
            Text(text = subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Stats(
    breed: Breed,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        breed.weightKg?.let { StatCard(Res.string.detail_weight, stringResource(Res.string.detail_value_kg, formatRange(it))) }
        breed.heightCm?.let { StatCard(Res.string.detail_height, stringResource(Res.string.detail_value_cm, formatRange(it))) }
        breed.lifeSpanYears?.let { StatCard(Res.string.detail_life_span, stringResource(Res.string.detail_value_years, formatRange(it))) }
    }
}

@Composable
private fun StatCard(
    label: StringResource,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = stringResource(label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun Temperament(
    temperament: List<String>,
    modifier: Modifier = Modifier,
) {
    Section(title = Res.string.detail_temperament, modifier = modifier) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            temperament.forEach { word ->
                Text(
                    text = word.replaceFirstChar(Char::uppercaseChar),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun TextSection(
    title: StringResource,
    text: String,
    modifier: Modifier = Modifier,
) {
    Section(title = title, modifier = modifier) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun TraitSection(
    title: StringResource,
    ratings: List<Pair<StringResource, Int>>,
    modifier: Modifier = Modifier,
) {
    Section(title = title, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ratings.forEach { (label, rating) -> TraitRow(label = stringResource(label), rating = rating) }
        }
    }
}

@Composable
private fun TraitRow(
    label: String,
    rating: Int,
    modifier: Modifier = Modifier,
) {
    val ratingDescription = stringResource(Res.string.detail_trait_rating, rating)
    Row(
        modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) { contentDescription = "$label, $ratingDescription" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(MAX_TRAIT_RATING) { index ->
                Box(
                    Modifier
                        .size(width = 20.dp, height = 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < rating) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceContainerHighest,
                        ),
                )
            }
        }
    }
}

@Composable
private fun Sources(
    sourceNames: List<String>,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(Res.string.detail_sources, sourceNames.joinToString(", ")),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 24.dp),
    )
}

@Composable
private fun Section(
    title: StringResource,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = stringResource(title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
        content()
    }
}

@Composable
private fun NotFoundContent(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.padding(8.dp)) {
            OverlayIconButton(onClick = onBack) {
                Icon(painterResource(platformBackIcon), contentDescription = stringResource(Res.string.detail_back))
            }
        }
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(Res.string.detail_not_found),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
