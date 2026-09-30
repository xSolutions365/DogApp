package com.example.dogapp.core.data.datastore

import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PreferencesDataStorePathTest {

    @Test
    fun pathPointsToPreferencesFileInsideExistingDocumentsDirectory() {
        val path = preferencesDataStorePath().toPath()

        assertEquals(PREFERENCES_FILE_NAME, path.name)
        assertEquals("Documents", path.parent?.name)
        assertTrue(FileSystem.SYSTEM.exists(requireNotNull(path.parent)))
    }
}
