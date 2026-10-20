package com.example.dogapp.feature.wallet.domain

import com.example.dogapp.testing.FakeTodayProvider
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidateCardUseCaseTest {
    private val validate = ValidateCardUseCase(FakeTodayProvider("2026-10-19"))

    private fun errorsFor(
        number: String = "4242 4242 4242 4242",
        expiry: String = "12/28",
        securityCode: String = "123",
    ): Set<CardFieldError> = (validate(number, expiry, securityCode) as? CardValidation.Invalid)?.errors.orEmpty()

    @Test
    fun validCardKeepsOnlyItsBrandLastFourAndExpiry() {
        assertEquals(
            CardValidation.Valid(brand = CardBrand.Visa, last4 = "4242", expiry = CardExpiry(month = 12, year = 2028)),
            validate("4242 4242 4242 4242", "12/28", "123"),
        )
    }

    @Test
    fun brandComesFromTheIssuerRangeAndSetsLengthAndSecurityCode() {
        mapOf(
            "4111111111111111" to CardBrand.Visa,
            "5555555555554444" to CardBrand.Mastercard,
            "2223003122003222" to CardBrand.Mastercard,
            "378282246310005" to CardBrand.Amex,
            "6011111111111117" to CardBrand.Discover,
            "6445644564456445" to CardBrand.Discover,
            "3530111333300000" to CardBrand.Other,
        ).forEach { (number, brand) ->
            val securityCode = "1".repeat(brand.securityCodeLength)
            assertEquals(brand, (validate(number, "12/28", securityCode) as CardValidation.Valid).brand, number)
        }
        assertEquals(setOf(CardFieldError.InvalidSecurityCode), errorsFor(number = "378282246310005", securityCode = "123"))
        assertEquals(setOf(CardFieldError.InvalidNumber), errorsFor(number = "4111 1111 1111 111"))
    }

    @Test
    fun anyFakeNumberIsAcceptedWhenItHasOnlyDigitsAndItsBrandsLength() {
        assertEquals(emptySet(), errorsFor(number = "4242 4242 4242 4241"))
        assertEquals(emptySet(), errorsFor(number = "1234 5678 9012 3456"))
        assertEquals(emptySet(), errorsFor(number = "3700 0000 0000 001", securityCode = "1234"))
        assertEquals(setOf(CardFieldError.InvalidNumber), errorsFor(number = "3700 0000 0000 0012", securityCode = "1234"))
        assertEquals(setOf(CardFieldError.InvalidNumber), errorsFor(number = "4242 4242 4242 424a"))
        assertEquals(setOf(CardFieldError.InvalidNumber), errorsFor(number = ""))
        assertEquals(emptySet(), errorsFor(number = "4242-4242-4242-4242"))
    }

    @Test
    fun expiryIsAMonthAndYearThatHasNotPassed() {
        assertEquals(emptySet(), errorsFor(expiry = "10/26"))
        assertEquals(emptySet(), errorsFor(expiry = "1046"))
        assertEquals(setOf(CardFieldError.ExpiredCard), errorsFor(expiry = "09/26"))
        assertEquals(setOf(CardFieldError.InvalidExpiry), errorsFor(expiry = "13/28"))
        assertEquals(setOf(CardFieldError.InvalidExpiry), errorsFor(expiry = "00/28"))
        assertEquals(setOf(CardFieldError.InvalidExpiry), errorsFor(expiry = "12/2"))
        assertEquals(setOf(CardFieldError.InvalidExpiry), errorsFor(expiry = "11/47"))
    }

    @Test
    fun everyInvalidFieldIsReportedAtOnce() {
        assertEquals(
            setOf(CardFieldError.InvalidNumber, CardFieldError.ExpiredCard, CardFieldError.InvalidSecurityCode),
            errorsFor(number = "1234", expiry = "01/20", securityCode = "1"),
        )
    }
}
