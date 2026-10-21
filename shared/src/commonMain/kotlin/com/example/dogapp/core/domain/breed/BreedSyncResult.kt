package com.example.dogapp.core.domain.breed

import com.example.dogapp.core.domain.DataError

data class BreedSyncResult(
    val succeededSources: Set<BreedSourceId>,
    val failures: List<BreedSourceFailure>,
)

data class BreedSourceFailure(
    val sourceId: BreedSourceId,
    val sourceName: String,
    val error: DataError.Remote,
)
