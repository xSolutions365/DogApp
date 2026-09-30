package com.example.dogapp.core.data.datastore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PreferencesDataStoreTest {
    private val filePath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "test-${Random.nextLong()}-$PREFERENCES_FILE_NAME"

    @AfterTest
    fun tearDown() {
        FileSystem.SYSTEM.delete(filePath, mustExist = false)
    }

    @Test
    fun writtenPreferencesAreReadBack() =
        runTest {
            val dataStore = createPreferencesDataStore { filePath.toString() }
            val key = stringPreferencesKey("key")

            dataStore.edit { preferences -> preferences[key] = "value" }

            assertEquals("value", dataStore.data.first()[key])
            assertEquals(true, FileSystem.SYSTEM.exists(filePath))
        }
}
