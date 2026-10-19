package com.example.dogapp.feature.wallet.data

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.map
import com.example.dogapp.feature.wallet.domain.Wallet
import com.example.dogapp.feature.wallet.domain.WalletError
import com.example.dogapp.feature.wallet.domain.WalletRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Instant

class DefaultWalletRepository(
    private val vault: WalletVault,
    private val clock: Clock = Clock.System,
) : WalletRepository {
    @Serializable
    private data class StoredWallet(
        val createdAtEpochMillis: Long,
    )

    override suspend fun unlock(): Outcome<Wallet, WalletError> {
        if (!vault.exists()) {
            val wallet = Wallet(createdAt = Instant.fromEpochMilliseconds(clock.now().toEpochMilliseconds()))
            return vault.write(encode(wallet)).map { wallet }
        }
        return vault.read().map(::decode)
    }

    override suspend fun wipe() = vault.delete()

    private fun encode(wallet: Wallet): ByteArray =
        Json.encodeToString(StoredWallet(createdAtEpochMillis = wallet.createdAt.toEpochMilliseconds())).encodeToByteArray()

    private fun decode(contents: ByteArray): Wallet =
        Wallet(
            createdAt = Instant.fromEpochMilliseconds(Json.decodeFromString<StoredWallet>(contents.decodeToString()).createdAtEpochMillis),
        )
}
