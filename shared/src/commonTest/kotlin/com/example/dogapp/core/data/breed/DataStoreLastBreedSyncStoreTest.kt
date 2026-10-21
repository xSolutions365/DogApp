package com.example.dogapp.core.data.breed

import com.example.dogapp.core.data.datastore.PREFERENCES_FILE_NAME
import com.example.dogapp.core.data.datastore.createPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class DataStoreLastBreedSyncStoreTest {
    private val filePath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "test-${Random.nextLong()}-$PREFERENCES_FILE_NAME"

    @AfterTest
    fun tearDown() {
        FileSystem.SYSTEM.delete(filePath, mustExist = false)
    }

    @Test
    fun noSyncIsUnknownAndTheLatestSyncTimeIsReadBack() =
        runTest {
            val store = DataStoreLastBreedSyncStore(createPreferencesDataStore { filePath.toString() })
            assertEquals(null, store.observeLastSync().first())

            store.recordSync(Instant.parse("2026-10-20T18:05:00Z"))
            store.recordSync(Instant.parse("2026-10-21T09:42:00Z"))

            assertEquals(Instant.parse("2026-10-21T09:42:00Z"), store.observeLastSync().first())
        }
}
