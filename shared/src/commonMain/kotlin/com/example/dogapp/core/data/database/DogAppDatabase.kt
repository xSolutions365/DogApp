package com.example.dogapp.core.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.example.dogapp.core.data.breed.local.BreedTypeConverters
import com.example.dogapp.core.data.breed.local.SourcedBreedDao
import com.example.dogapp.core.data.breed.local.SourcedBreedEntity

const val DATABASE_FILE_NAME = "dogapp.db"

@Database(entities = [SourcedBreedEntity::class], version = 1, exportSchema = true)
@TypeConverters(BreedTypeConverters::class)
@ConstructedBy(DogAppDatabaseConstructor::class)
abstract class DogAppDatabase : RoomDatabase() {
    abstract fun sourcedBreedDao(): SourcedBreedDao
}

expect object DogAppDatabaseConstructor : RoomDatabaseConstructor<DogAppDatabase> {
    override fun initialize(): DogAppDatabase
}
