package com.example.dogapp.feature.wallet.data

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.wallet.domain.WalletError

interface WalletVault {
    suspend fun exists(): Boolean

    suspend fun read(): Outcome<ByteArray, WalletError>

    suspend fun write(contents: ByteArray): Outcome<Unit, WalletError>

    suspend fun delete()
}
