package com.example.dogapp.core.data.database

import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

class ClearBreedCacheMigrationSpec : AutoMigrationSpec {
    override fun onPostMigrate(connection: SQLiteConnection) {
        connection.execSQL("DELETE FROM sourced_breed")
    }
}
