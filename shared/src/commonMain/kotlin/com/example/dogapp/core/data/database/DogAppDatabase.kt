package com.example.dogapp.core.data.database

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.example.dogapp.core.data.breed.local.BreedTypeConverters
import com.example.dogapp.core.data.breed.local.SourcedBreedDao
import com.example.dogapp.core.data.breed.local.SourcedBreedEntity
import com.example.dogapp.core.data.donation.local.DonationDao
import com.example.dogapp.core.data.donation.local.DonationEntity
import com.example.dogapp.core.data.draw.local.DailyDrawDao
import com.example.dogapp.core.data.draw.local.DailyDrawEntity
import com.example.dogapp.core.data.favourite.local.FavouriteBreedDao
import com.example.dogapp.core.data.favourite.local.FavouriteBreedEntity

const val DATABASE_FILE_NAME = "dogapp.db"

@Database(
    entities = [SourcedBreedEntity::class, FavouriteBreedEntity::class, DailyDrawEntity::class, DonationEntity::class],
    version = 5,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = ClearBreedCacheMigrationSpec::class),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
    ],
)
@TypeConverters(BreedTypeConverters::class)
@ConstructedBy(DogAppDatabaseConstructor::class)
abstract class DogAppDatabase : RoomDatabase() {
    abstract fun sourcedBreedDao(): SourcedBreedDao

    abstract fun favouriteBreedDao(): FavouriteBreedDao

    abstract fun dailyDrawDao(): DailyDrawDao

    abstract fun donationDao(): DonationDao
}

expect object DogAppDatabaseConstructor : RoomDatabaseConstructor<DogAppDatabase> {
    override fun initialize(): DogAppDatabase
}
