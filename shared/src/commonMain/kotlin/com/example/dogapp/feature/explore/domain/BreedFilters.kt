package com.example.dogapp.feature.explore.domain

import com.example.dogapp.core.domain.breed.BreedTrait

enum class BreedSize {
    Toy,
    Small,
    Medium,
    Large,
    Giant,
}

enum class LifeSpanBand {
    UnderTen,
    TenToTwelve,
    ThirteenPlus,
}

enum class EnergyLevel {
    Low,
    Medium,
    High,
}

enum class TraitFilter(
    val rating: BreedTrait,
) {
    GoodWithChildren(BreedTrait.GoodWithChildren),
    LowShedding(BreedTrait.Shedding),
    EasyToTrain(BreedTrait.Trainability),
}

enum class FilterCategory {
    Size,
    LifeSpan,
    Group,
    Energy,
    Traits,
}

enum class BreedInfo {
    Size,
    LifeSpan,
    Group,
    Traits,
}

data class BreedFilters(
    val sizes: Set<BreedSize> = emptySet(),
    val lifeSpans: Set<LifeSpanBand> = emptySet(),
    val groups: Set<String> = emptySet(),
    val energyLevels: Set<EnergyLevel> = emptySet(),
    val traits: Set<TraitFilter> = emptySet(),
    val favouritesOnly: Boolean = false,
) {
    val isActive: Boolean
        get() =
            sizes.isNotEmpty() ||
                lifeSpans.isNotEmpty() ||
                groups.isNotEmpty() ||
                energyLevels.isNotEmpty() ||
                traits.isNotEmpty() ||
                favouritesOnly
}
