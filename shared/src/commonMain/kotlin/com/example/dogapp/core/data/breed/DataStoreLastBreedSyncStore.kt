package com.example.dogapp.core.data.breed

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.dogapp.core.domain.breed.LastBreedSyncStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

class DataStoreLastBreedSyncStore(
    private val dataStore: DataStore<Preferences>,
) : LastBreedSyncStore {
    override fun observeLastSync(): Flow<Instant?> =
        dataStore.data.map { preferences -> preferences[LAST_BREED_SYNC]?.let(Instant::fromEpochMilliseconds) }

    override suspend fun recordSync(at: Instant) {
        dataStore.edit { preferences -> preferences[LAST_BREED_SYNC] = at.toEpochMilliseconds() }
    }

    private companion object {
        val LAST_BREED_SYNC = longPreferencesKey("last_breed_sync")
    }
}
