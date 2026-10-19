package com.example.dogapp.feature.wallet.domain

data class CardExpiry(
    val month: Int,
    val year: Int,
) : Comparable<CardExpiry> {
    override fun compareTo(other: CardExpiry): Int = compareValuesBy(this, other, CardExpiry::year, CardExpiry::month)
}
