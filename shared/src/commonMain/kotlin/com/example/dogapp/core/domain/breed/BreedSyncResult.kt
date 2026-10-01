package com.example.dogapp.core.domain.breed

import com.example.dogapp.core.domain.DataError

data class BreedSyncResult(
    val succeededSources: Set<BreedSourceId>,
    val failures: Map<BreedSourceId, DataError.Remote>,
)
