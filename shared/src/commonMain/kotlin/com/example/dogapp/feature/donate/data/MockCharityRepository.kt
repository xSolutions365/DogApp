package com.example.dogapp.feature.donate.data

import com.example.dogapp.feature.donate.domain.Charity
import com.example.dogapp.feature.donate.domain.CharityRepository

class MockCharityRepository : CharityRepository {
    override fun charities(): List<Charity> = mockCharities

    override fun charity(id: String): Charity? = mockCharities.firstOrNull { it.id == id }
}

private val mockCharities =
    listOf(
        Charity(
            id = "second-sniff",
            name = "Second Sniff Shelter",
            tagline = "New homes for senior dogs",
            location = "Little Woofington",
            description =
                "Second Sniff takes in older dogs whose owners can no longer care for them, " +
                    "gives them a vet check and a comfy bed, and matches them with calm, patient families.",
        ),
        Charity(
            id = "muddy-paws",
            name = "The Muddy Paws Trust",
            tagline = "Rescue and rehab for working breeds",
            location = "Barkshire Downs",
            description =
                "The Muddy Paws Trust rescues collies, spaniels and other high-energy working dogs, " +
                    "and trains them for life in a family home with plenty of walks and puzzle toys.",
        ),
        Charity(
            id = "wag-street",
            name = "Wag Street Rescue",
            tagline = "Getting street dogs off the street",
            location = "Port Pawdington",
            description =
                "Wag Street Rescue runs a night van that feeds and treats stray dogs, " +
                    "then fosters them until they are ready for adoption.",
        ),
        Charity(
            id = "good-boy-vets",
            name = "Good Boy Vet Fund",
            tagline = "Vet bills for owners on a low income",
            location = "Kennelbury",
            description =
                "The Good Boy Vet Fund pays for emergency treatment when an owner can't, " +
                    "so no dog has to be given up because of a single vet bill.",
        ),
        Charity(
            id = "fetch-club",
            name = "Fetch Club",
            tagline = "Therapy dogs for hospitals and schools",
            location = "Houndsley",
            description =
                "Fetch Club trains gentle dogs and their owners as therapy teams " +
                    "that visit hospital wards, care homes and school reading groups.",
        ),
    )
