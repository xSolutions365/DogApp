package com.example.dogapp.testing

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.donate.domain.PaymentCard
import com.example.dogapp.feature.donate.domain.PaymentCards
import com.example.dogapp.feature.donate.domain.PaymentError
import kotlinx.coroutines.flow.MutableStateFlow

class FakePaymentCards(
    private val savedCards: List<PaymentCard>,
    unlocked: Boolean = true,
) : PaymentCards {
    override val cards = MutableStateFlow(if (unlocked) savedCards else null)
    var nextFailure: PaymentError? = null
    val confirmed = mutableListOf<String>()

    override suspend fun unlock(): Outcome<Unit, PaymentError> = takeFailure() ?: Outcome.Success(Unit).also { cards.value = savedCards }

    override suspend fun confirm(cardId: String): Outcome<PaymentCard, PaymentError> {
        takeFailure()?.let { return it }
        val card = cards.value?.firstOrNull { it.id == cardId } ?: return Outcome.Failure(PaymentError.CardNotFound)
        confirmed += cardId
        return Outcome.Success(card)
    }

    private fun takeFailure(): Outcome.Failure<PaymentError>? = nextFailure?.let { Outcome.Failure(it) }.also { nextFailure = null }
}

fun testPaymentCard(
    last4: String,
    brandName: String = "Visa",
) = PaymentCard(id = "card-$last4", brandName = brandName, last4 = last4)
