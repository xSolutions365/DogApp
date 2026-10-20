package com.example.dogapp.core.data.donation.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "donation")
data class DonationEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "charity_id") val charityId: String,
    @ColumnInfo(name = "charity_name") val charityName: String,
    @ColumnInfo(name = "amount_pence") val amountPence: Int,
    @ColumnInfo(name = "paid_with") val paidWith: String,
    @ColumnInfo(name = "card_brand") val cardBrand: String?,
    @ColumnInfo(name = "card_last4") val cardLast4: String?,
    @ColumnInfo(name = "card_description") val cardDescription: String?,
    @ColumnInfo(name = "donated_at") val donatedAtEpochMillis: Long,
)
