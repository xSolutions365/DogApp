package com.example.dogapp.testing

import com.example.dogapp.feature.donate.domain.Charity
import com.example.dogapp.feature.donate.domain.CharityRepository

class FakeCharityRepository(
    private val charities: List<Charity>,
) : CharityRepository {
    override fun charities(): List<Charity> = charities

    override fun charity(id: String): Charity? = charities.firstOrNull { it.id == id }
}

fun testCharity(
    id: String,
    name: String = id,
) = Charity(id = id, name = name, tagline = "Tagline of $name", location = "Kennelbury", description = "About $name")
