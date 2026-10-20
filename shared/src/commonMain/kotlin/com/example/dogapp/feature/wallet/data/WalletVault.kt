package com.example.dogapp.feature.wallet.data

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.wallet.domain.WalletAccess
import com.example.dogapp.feature.wallet.domain.WalletChange
import com.example.dogapp.feature.wallet.domain.WalletError

interface WalletVault {
    suspend fun exists(): Boolean

    suspend fun read(access: WalletAccess): Outcome<ByteArray, WalletError>

    suspend fun write(
        contents: ByteArray,
        change: WalletChange,
    ): Outcome<Unit, WalletError>

    suspend fun delete()
}
