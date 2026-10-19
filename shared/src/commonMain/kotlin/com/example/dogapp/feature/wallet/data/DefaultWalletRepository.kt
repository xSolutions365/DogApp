package com.example.dogapp.feature.wallet.data

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.map
import com.example.dogapp.feature.wallet.domain.CardBrand
import com.example.dogapp.feature.wallet.domain.CardExpiry
import com.example.dogapp.feature.wallet.domain.SavedCard
import com.example.dogapp.feature.wallet.domain.Wallet
import com.example.dogapp.feature.wallet.domain.WalletChange
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
        val cards: List<StoredCard> = emptyList(),
    )

    @Serializable
    private data class StoredCard(
        val id: String,
        val brand: String,
        val last4: String,
        val expiryMonth: Int,
        val expiryYear: Int,
    )

    override suspend fun unlock(): Outcome<Wallet, WalletError> {
        if (!vault.exists()) {
            val wallet = Wallet(createdAt = Instant.fromEpochMilliseconds(clock.now().toEpochMilliseconds()))
            return vault.write(encode(wallet), WalletChange.SetUp).map { wallet }
        }
        return vault.read().map(::decode)
    }

    override suspend fun save(
        wallet: Wallet,
        change: WalletChange,
    ): Outcome<Unit, WalletError> = vault.write(encode(wallet), change)

    override suspend fun wipe() = vault.delete()

    private fun encode(wallet: Wallet): ByteArray =
        Json
            .encodeToString(
                StoredWallet(
                    createdAtEpochMillis = wallet.createdAt.toEpochMilliseconds(),
                    cards =
                        wallet.cards.map { card ->
                            StoredCard(
                                id = card.id,
                                brand = card.brand.name,
                                last4 = card.last4,
                                expiryMonth = card.expiry.month,
                                expiryYear = card.expiry.year,
                            )
                        },
                ),
            ).encodeToByteArray()

    private fun decode(contents: ByteArray): Wallet {
        val stored = Json.decodeFromString<StoredWallet>(contents.decodeToString())
        return Wallet(
            createdAt = Instant.fromEpochMilliseconds(stored.createdAtEpochMillis),
            cards =
                stored.cards.map { card ->
                    SavedCard(
                        id = card.id,
                        brand = CardBrand.valueOf(card.brand),
                        last4 = card.last4,
                        expiry = CardExpiry(month = card.expiryMonth, year = card.expiryYear),
                    )
                },
        )
    }
}
