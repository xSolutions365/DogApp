package com.example.dogapp.feature.donate.domain

interface CharityRepository {
    fun charities(): List<Charity>

    fun charity(id: String): Charity?
}
