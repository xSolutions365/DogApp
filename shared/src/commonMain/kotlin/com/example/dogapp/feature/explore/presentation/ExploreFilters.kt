package com.example.dogapp.feature.explore.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dogapp.feature.explore.domain.BreedFilters
import com.example.dogapp.feature.explore.domain.BreedInfo
import com.example.dogapp.feature.explore.domain.BreedSize
import com.example.dogapp.feature.explore.domain.EnergyLevel
import com.example.dogapp.feature.explore.domain.FilterCategory
import com.example.dogapp.feature.explore.domain.LifeSpanBand
import com.example.dogapp.feature.explore.domain.TraitFilter
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.explore_energy_high
import dogapp.shared.generated.resources.explore_energy_low
import dogapp.shared.generated.resources.explore_energy_medium
import dogapp.shared.generated.resources.explore_filter_energy
import dogapp.shared.generated.resources.explore_filter_favourites
import dogapp.shared.generated.resources.explore_filter_group
import dogapp.shared.generated.resources.explore_filter_life_span
import dogapp.shared.generated.resources.explore_filter_size
import dogapp.shared.generated.resources.explore_filter_summary
import dogapp.shared.generated.resources.explore_filter_traits
import dogapp.shared.generated.resources.explore_filters_clear
import dogapp.shared.generated.resources.explore_hidden_group
import dogapp.shared.generated.resources.explore_hidden_life_span
import dogapp.shared.generated.resources.explore_hidden_size
import dogapp.shared.generated.resources.explore_hidden_traits
import dogapp.shared.generated.resources.explore_life_span_ten_to_twelve
import dogapp.shared.generated.resources.explore_life_span_thirteen_plus
import dogapp.shared.generated.resources.explore_life_span_under_ten
import dogapp.shared.generated.resources.explore_size_giant
import dogapp.shared.generated.resources.explore_size_giant_range
import dogapp.shared.generated.resources.explore_size_large
import dogapp.shared.generated.resources.explore_size_large_range
import dogapp.shared.generated.resources.explore_size_medium
import dogapp.shared.generated.resources.explore_size_medium_range
import dogapp.shared.generated.resources.explore_size_small
import dogapp.shared.generated.resources.explore_size_small_range
import dogapp.shared.generated.resources.explore_size_toy
import dogapp.shared.generated.resources.explore_size_toy_range
import dogapp.shared.generated.resources.explore_trait_easy_to_train
import dogapp.shared.generated.resources.explore_trait_low_shedding
import dogapp.shared.generated.resources.trait_good_with_children
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExploreFilterBar(
    filters: BreedFilters,
    availableFilters: Set<FilterCategory>,
    groupOptions: List<String>,
    onFiltersChange: (BreedFilters) -> Unit,
    modifier: Modifier = Modifier,
) {
    var openSheet by remember { mutableStateOf<FilterCategory?>(null) }
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (FilterCategory.Size in availableFilters) {
            item {
                SheetChip(
                    title = stringResource(Res.string.explore_filter_size),
                    selection = filters.sizes.sorted().map { stringResource(it.label) },
                    onClick = { openSheet = FilterCategory.Size },
                )
            }
        }
        if (FilterCategory.LifeSpan in availableFilters) {
            item {
                SheetChip(
                    title = stringResource(Res.string.explore_filter_life_span),
                    selection = filters.lifeSpans.sorted().map { stringResource(it.label) },
                    onClick = { openSheet = FilterCategory.LifeSpan },
                )
            }
        }
        if (FilterCategory.Group in availableFilters) {
            item {
                SheetChip(
                    title = stringResource(Res.string.explore_filter_group),
                    selection = filters.groups.sorted(),
                    onClick = { openSheet = FilterCategory.Group },
                )
            }
        }
        if (FilterCategory.Energy in availableFilters) {
            item {
                SheetChip(
                    title = stringResource(Res.string.explore_filter_energy),
                    selection = filters.energyLevels.sorted().map { stringResource(it.label) },
                    onClick = { openSheet = FilterCategory.Energy },
                )
            }
        }
        if (FilterCategory.Traits in availableFilters) {
            item {
                SheetChip(
                    title = stringResource(Res.string.explore_filter_traits),
                    selection = filters.traits.sorted().map { stringResource(it.label) },
                    onClick = { openSheet = FilterCategory.Traits },
                )
            }
        }
        item {
            FilterChip(
                selected = filters.favouritesOnly,
                onClick = { onFiltersChange(filters.copy(favouritesOnly = !filters.favouritesOnly)) },
                label = { Text(stringResource(Res.string.explore_filter_favourites)) },
            )
        }
        if (filters.isActive) {
            item {
                AssistChip(
                    onClick = { onFiltersChange(BreedFilters()) },
                    label = { Text(stringResource(Res.string.explore_filters_clear)) },
                )
            }
        }
    }
    when (openSheet) {
        FilterCategory.Size -> {
            OptionsSheet(
                title = stringResource(Res.string.explore_filter_size),
                options = BreedSize.entries,
                selected = filters.sizes,
                label = { "${stringResource(it.label)} · ${stringResource(it.range)}" },
                onSelectedChange = { onFiltersChange(filters.copy(sizes = it)) },
                onDismiss = { openSheet = null },
            )
        }

        FilterCategory.LifeSpan -> {
            OptionsSheet(
                title = stringResource(Res.string.explore_filter_life_span),
                options = LifeSpanBand.entries,
                selected = filters.lifeSpans,
                label = { stringResource(it.label) },
                onSelectedChange = { onFiltersChange(filters.copy(lifeSpans = it)) },
                onDismiss = { openSheet = null },
            )
        }

        FilterCategory.Group -> {
            OptionsSheet(
                title = stringResource(Res.string.explore_filter_group),
                options = groupOptions,
                selected = filters.groups,
                label = { it },
                onSelectedChange = { onFiltersChange(filters.copy(groups = it)) },
                onDismiss = { openSheet = null },
            )
        }

        FilterCategory.Energy -> {
            OptionsSheet(
                title = stringResource(Res.string.explore_filter_energy),
                options = EnergyLevel.entries,
                selected = filters.energyLevels,
                label = { stringResource(it.label) },
                onSelectedChange = { onFiltersChange(filters.copy(energyLevels = it)) },
                onDismiss = { openSheet = null },
            )
        }

        FilterCategory.Traits -> {
            OptionsSheet(
                title = stringResource(Res.string.explore_filter_traits),
                options = TraitFilter.entries,
                selected = filters.traits,
                label = { stringResource(it.label) },
                onSelectedChange = { onFiltersChange(filters.copy(traits = it)) },
                onDismiss = { openSheet = null },
            )
        }

        null -> {
            Unit
        }
    }
}

@Composable
fun HiddenBreedsNote(
    hiddenForMissingInfo: Map<BreedInfo, Int>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        BreedInfo.entries.forEach { info ->
            val count = hiddenForMissingInfo[info] ?: 0
            if (count > 0) {
                Text(
                    text = pluralStringResource(info.hiddenNote, count, count),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SheetChip(
    title: String,
    selection: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selection.isNotEmpty(),
        onClick = onClick,
        label = {
            Text(
                if (selection.isEmpty()) {
                    title
                } else {
                    stringResource(Res.string.explore_filter_summary, title, selection.joinToString(", "))
                },
            )
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> OptionsSheet(
    title: String,
    options: List<T>,
    selected: Set<T>,
    label: @Composable (T) -> String,
    onSelectedChange: (Set<T>) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    val isSelected = option in selected
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectedChange(if (isSelected) selected - option else selected + option) },
                        label = { Text(label(option)) },
                    )
                }
            }
        }
    }
}

private val BreedSize.label: StringResource
    get() =
        when (this) {
            BreedSize.Toy -> Res.string.explore_size_toy
            BreedSize.Small -> Res.string.explore_size_small
            BreedSize.Medium -> Res.string.explore_size_medium
            BreedSize.Large -> Res.string.explore_size_large
            BreedSize.Giant -> Res.string.explore_size_giant
        }

private val BreedSize.range: StringResource
    get() =
        when (this) {
            BreedSize.Toy -> Res.string.explore_size_toy_range
            BreedSize.Small -> Res.string.explore_size_small_range
            BreedSize.Medium -> Res.string.explore_size_medium_range
            BreedSize.Large -> Res.string.explore_size_large_range
            BreedSize.Giant -> Res.string.explore_size_giant_range
        }

private val LifeSpanBand.label: StringResource
    get() =
        when (this) {
            LifeSpanBand.UnderTen -> Res.string.explore_life_span_under_ten
            LifeSpanBand.TenToTwelve -> Res.string.explore_life_span_ten_to_twelve
            LifeSpanBand.ThirteenPlus -> Res.string.explore_life_span_thirteen_plus
        }

private val EnergyLevel.label: StringResource
    get() =
        when (this) {
            EnergyLevel.Low -> Res.string.explore_energy_low
            EnergyLevel.Medium -> Res.string.explore_energy_medium
            EnergyLevel.High -> Res.string.explore_energy_high
        }

private val TraitFilter.label: StringResource
    get() =
        when (this) {
            TraitFilter.GoodWithChildren -> Res.string.trait_good_with_children
            TraitFilter.LowShedding -> Res.string.explore_trait_low_shedding
            TraitFilter.EasyToTrain -> Res.string.explore_trait_easy_to_train
        }

private val BreedInfo.hiddenNote: PluralStringResource
    get() =
        when (this) {
            BreedInfo.Size -> Res.plurals.explore_hidden_size
            BreedInfo.LifeSpan -> Res.plurals.explore_hidden_life_span
            BreedInfo.Group -> Res.plurals.explore_hidden_group
            BreedInfo.Traits -> Res.plurals.explore_hidden_traits
        }
