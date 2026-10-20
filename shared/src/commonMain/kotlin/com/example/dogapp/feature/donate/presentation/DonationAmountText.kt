package com.example.dogapp.feature.donate.presentation

import com.example.dogapp.feature.donate.domain.DonationAmount

fun DonationAmount.displayText(): String {
    val pounds = pence / 100
    val remainder = pence % 100
    return if (remainder == 0) "£$pounds" else "£$pounds.${remainder.toString().padStart(2, '0')}"
}
