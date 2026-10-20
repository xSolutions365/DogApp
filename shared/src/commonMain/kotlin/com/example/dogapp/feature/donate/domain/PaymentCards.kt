package com.example.dogapp.feature.donate.domain

import com.example.dogapp.core.domain.Outcome
import kotlinx.coroutines.flow.StateFlow

interface PaymentCards {
    val cards: StateFlow<List<PaymentCard>?>

    suspend fun unlock(): Outcome<Unit, PaymentError>

    suspend fun confirm(cardId: String): Outcome<PaymentCard, PaymentError>
}
