package com.example.dogapp.feature.wallet.domain

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.map
import com.example.dogapp.core.domain.onFailure
import com.example.dogapp.core.domain.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.uuid.Uuid

class WalletCoordinator(
    private val repository: WalletRepository,
    private val newCardId: () -> String = { Uuid.random().toString() },
) {
    private val mutex = Mutex()
    private val mutableStatus = MutableStateFlow<WalletStatus>(WalletStatus.Locked)

    val status: StateFlow<WalletStatus> = mutableStatus.asStateFlow()

    suspend fun unlock(): Outcome<Wallet, WalletError> =
        mutex.withLock {
            repository
                .unlock(WalletAccess.Unlock)
                .onSuccess { wallet -> mutableStatus.value = WalletStatus.Unlocked(wallet) }
                .onFailure { error -> handleFailure(error) }
        }

    suspend fun confirmPayment(cardId: String): Outcome<SavedCard, WalletError> =
        mutex.withLock {
            if (status.value !is WalletStatus.Unlocked) return@withLock Outcome.Failure(WalletError.Locked)
            when (val read = repository.unlock(WalletAccess.ConfirmPayment)) {
                is Outcome.Failure -> {
                    handleFailure(read.error)
                    read
                }

                is Outcome.Success -> {
                    mutableStatus.value = WalletStatus.Unlocked(read.data)
                    read.data.cards
                        .firstOrNull { it.id == cardId }
                        ?.let { Outcome.Success(it) }
                        ?: Outcome.Failure(WalletError.CardNotFound)
                }
            }
        }

    suspend fun addCard(card: CardValidation.Valid): Outcome<Wallet, WalletError> =
        change(WalletChange.CardAdded) { wallet ->
            wallet.copy(cards = wallet.cards + SavedCard(id = newCardId(), brand = card.brand, last4 = card.last4, expiry = card.expiry))
        }

    suspend fun removeCard(cardId: String): Outcome<Wallet, WalletError> =
        change(WalletChange.CardRemoved) { wallet -> wallet.copy(cards = wallet.cards.filterNot { it.id == cardId }) }

    fun lock() {
        mutableStatus.update { status -> if (status is WalletStatus.Unlocked) WalletStatus.Locked else status }
    }

    private suspend fun change(
        change: WalletChange,
        transform: (Wallet) -> Wallet,
    ): Outcome<Wallet, WalletError> =
        mutex.withLock {
            val current = (status.value as? WalletStatus.Unlocked)?.wallet ?: return@withLock Outcome.Failure(WalletError.Locked)
            val updated = transform(current)
            repository
                .save(updated, change)
                .map { updated }
                .onSuccess { wallet -> mutableStatus.value = WalletStatus.Unlocked(wallet) }
                .onFailure { error -> handleFailure(error) }
        }

    private suspend fun handleFailure(error: WalletError) {
        if (error == WalletError.KeyInvalidated) {
            repository.wipe()
            mutableStatus.value = WalletStatus.KeyInvalidated
        }
    }
}
