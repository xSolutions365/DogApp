package com.example.dogapp.feature.wallet.domain

import com.example.dogapp.core.domain.draw.TodayProvider

class ValidateCardUseCase(
    private val today: TodayProvider,
) {
    operator fun invoke(
        number: String,
        expiry: String,
        securityCode: String,
    ): CardValidation {
        val digits = number.filterNot { it == ' ' || it == '-' }
        val brand = CardBrand.of(digits)
        val parsedExpiry = parseExpiry(expiry)
        val errors =
            buildSet {
                if (!digits.isDigitsOnly() || digits.length !in brand.numberLengths) add(CardFieldError.InvalidNumber)
                when {
                    parsedExpiry == null -> add(CardFieldError.InvalidExpiry)
                    parsedExpiry < currentMonth() -> add(CardFieldError.ExpiredCard)
                    parsedExpiry.year > currentMonth().year + MAX_YEARS_AHEAD -> add(CardFieldError.InvalidExpiry)
                }
                if (!securityCode.isDigitsOnly() || securityCode.length != brand.securityCodeLength) add(CardFieldError.InvalidSecurityCode)
            }
        return if (errors.isEmpty() && parsedExpiry != null) {
            CardValidation.Valid(brand = brand, last4 = digits.takeLast(LAST_DIGITS), expiry = parsedExpiry)
        } else {
            CardValidation.Invalid(errors)
        }
    }

    private fun currentMonth(): CardExpiry {
        val (year, month) = today.today().isoDate.split('-')
        return CardExpiry(month = month.toInt(), year = year.toInt())
    }

    private fun parseExpiry(expiry: String): CardExpiry? {
        val digits = expiry.filterNot { it == '/' || it == ' ' }
        if (digits.length != EXPIRY_DIGITS || !digits.isDigitsOnly()) return null
        val month = digits.take(2).toInt()
        if (month !in 1..12) return null
        return CardExpiry(month = month, year = CENTURY + digits.takeLast(2).toInt())
    }

    private fun String.isDigitsOnly(): Boolean = isNotEmpty() && all { it in '0'..'9' }

    private companion object {
        const val LAST_DIGITS = 4
        const val EXPIRY_DIGITS = 4
        const val CENTURY = 2000
        const val MAX_YEARS_AHEAD = 20
    }
}
