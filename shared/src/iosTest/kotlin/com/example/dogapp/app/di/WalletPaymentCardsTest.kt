package com.example.dogapp.app.di

import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.donate.domain.PaymentCard
import com.example.dogapp.feature.donate.domain.PaymentError
import com.example.dogapp.feature.wallet.data.DefaultWalletRepository
import com.example.dogapp.feature.wallet.domain.CardBrand
import com.example.dogapp.feature.wallet.domain.CardExpiry
import com.example.dogapp.feature.wallet.domain.CardValidation
import com.example.dogapp.feature.wallet.domain.WalletCoordinator
import com.example.dogapp.feature.wallet.domain.WalletError
import com.example.dogapp.testing.FakeWalletVault
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSDate
import platform.Foundation.NSRunLoop
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.Foundation.runUntilDate
import platform.Foundation.timeIntervalSinceNow
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WalletPaymentCardsTest {
    private val vault = FakeWalletVault()
    private val coordinator = WalletCoordinator(DefaultWalletRepository(vault)) { "card-1" }
    private val scope = CoroutineScope(Dispatchers.Default)
    private val paymentCards = WalletPaymentCards(coordinator, scope)

    @AfterTest
    fun tearDown() {
        scope.cancel()
    }

    private fun <T> call(block: suspend () -> T): T = runBlocking(Dispatchers.Default) { block() }

    private fun spinMainRunLoopUntil(condition: () -> Boolean) {
        val deadline = NSDate.dateWithTimeIntervalSinceNow(15.0)
        while (!condition()) {
            check(deadline.timeIntervalSinceNow > 0) { "Timed out waiting on the main run loop" }
            NSRunLoop.mainRunLoop.runUntilDate(NSDate.dateWithTimeIntervalSinceNow(0.01))
        }
    }

    @Test
    fun walletCardsReachDonateOnlyWhileUnlockedWithReadableBrands() {
        assertNull(paymentCards.cards.value)

        assertEquals(Outcome.Success(Unit), call { paymentCards.unlock() })
        call {
            coordinator.addCard(
                CardValidation.Valid(brand = CardBrand.Amex, last4 = "0005", expiry = CardExpiry(month = 3, year = 2031)),
            )
        }
        spinMainRunLoopUntil { paymentCards.cards.value?.isNotEmpty() == true }
        val amex = PaymentCard(id = "card-1", brandName = "American Express", last4 = "0005")
        assertEquals(listOf(amex), paymentCards.cards.value)

        assertEquals(Outcome.Success(amex), call { paymentCards.confirm("card-1") })
        assertEquals(Outcome.Failure(PaymentError.CardNotFound), call { paymentCards.confirm("card-9") })

        vault.nextFailure = WalletError.AuthenticationCancelled
        assertEquals(Outcome.Failure(PaymentError.Cancelled), call { paymentCards.confirm("card-1") })

        coordinator.lock()
        spinMainRunLoopUntil { paymentCards.cards.value == null }
        assertEquals(Outcome.Failure(PaymentError.WalletUnavailable), call { paymentCards.confirm("card-1") })
    }
}
