package com.example.dogapp.app.di

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.donate.domain.PaymentCard
import com.example.dogapp.feature.donate.domain.PaymentCards
import com.example.dogapp.feature.donate.domain.PaymentError
import com.example.dogapp.feature.wallet.domain.SavedCard
import com.example.dogapp.feature.wallet.domain.WalletCoordinator
import com.example.dogapp.feature.wallet.domain.WalletError
import com.example.dogapp.feature.wallet.domain.WalletStatus
import com.example.dogapp.feature.wallet.presentation.displayName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.compose.resources.getString

class WalletPaymentCards(
    private val coordinator: WalletCoordinator,
    scope: CoroutineScope,
) : PaymentCards {
    override val cards: StateFlow<List<PaymentCard>?> =
        coordinator.status
            .map { status -> (status as? WalletStatus.Unlocked)?.wallet?.cards?.map { it.toPaymentCard() } }
            .stateIn(scope, SharingStarted.Eagerly, null)

    override suspend fun unlock(): Outcome<Unit, PaymentError> =
        when (val outcome = coordinator.unlock()) {
            is Outcome.Success -> Outcome.Success(Unit)
            is Outcome.Failure -> Outcome.Failure(outcome.error.toPaymentError())
        }

    override suspend fun confirm(cardId: String): Outcome<PaymentCard, PaymentError> =
        when (val outcome = coordinator.confirmPayment(cardId)) {
            is Outcome.Success -> Outcome.Success(outcome.data.toPaymentCard())
            is Outcome.Failure -> Outcome.Failure(outcome.error.toPaymentError())
        }

    private suspend fun SavedCard.toPaymentCard() = PaymentCard(id = id, brandName = getString(brand.displayName), last4 = last4)

    private fun WalletError.toPaymentError(): PaymentError =
        when (this) {
            WalletError.AuthenticationCancelled -> PaymentError.Cancelled
            WalletError.AuthenticationFailed -> PaymentError.AuthenticationFailed
            WalletError.CardNotFound -> PaymentError.CardNotFound
            WalletError.BiometricUnavailable, WalletError.KeyInvalidated, WalletError.Locked -> PaymentError.WalletUnavailable
        }
}
