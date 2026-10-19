package com.example.dogapp.feature.wallet.domain

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.onFailure
import com.example.dogapp.core.domain.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class WalletCoordinator(
    private val repository: WalletRepository,
) {
    private val mutex = Mutex()
    private val mutableStatus = MutableStateFlow<WalletStatus>(WalletStatus.Locked)

    val status: StateFlow<WalletStatus> = mutableStatus.asStateFlow()

    suspend fun unlock(): Outcome<Wallet, WalletError> =
        mutex.withLock {
            repository
                .unlock()
                .onSuccess { wallet -> mutableStatus.value = WalletStatus.Unlocked(wallet) }
                .onFailure { error ->
                    if (error == WalletError.KeyInvalidated) {
                        repository.wipe()
                        mutableStatus.value = WalletStatus.KeyInvalidated
                    }
                }
        }

    fun lock() {
        mutableStatus.update { status -> if (status is WalletStatus.Unlocked) WalletStatus.Locked else status }
    }
}
