package com.example.dogapp.core.data.datastore

import com.example.dogapp.core.data.storage.documentsDirectoryPath

fun preferencesDataStorePath(): String = documentsDirectoryPath() + "/" + PREFERENCES_FILE_NAME
