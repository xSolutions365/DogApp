package com.example.dogapp.testing

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.wallet.data.WalletVault
import com.example.dogapp.feature.wallet.domain.WalletAccess
import com.example.dogapp.feature.wallet.domain.WalletChange
import com.example.dogapp.feature.wallet.domain.WalletError

class FakeWalletVault : WalletVault {
    var contents: ByteArray? = null
    var nextFailure: WalletError? = null
    var authentications = 0
        private set
    val writes = mutableListOf<WalletChange>()
    val reads = mutableListOf<WalletAccess>()

    override suspend fun exists(): Boolean = contents != null

    override suspend fun read(access: WalletAccess): Outcome<ByteArray, WalletError> =
        authenticate {
            reads += access
            Outcome.Success(checkNotNull(contents))
        }

    override suspend fun write(
        contents: ByteArray,
        change: WalletChange,
    ): Outcome<Unit, WalletError> =
        authenticate {
            this.contents = contents
            writes += change
            Outcome.Success(Unit)
        }

    override suspend fun delete() {
        contents = null
    }

    private fun <D> authenticate(onSuccess: () -> Outcome<D, WalletError>): Outcome<D, WalletError> {
        authentications++
        val failure = nextFailure ?: return onSuccess()
        nextFailure = null
        return Outcome.Failure(failure)
    }
}
