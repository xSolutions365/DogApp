package com.example.dogapp.feature.wallet.domain

enum class CardBrand(
    val numberLengths: Set<Int>,
    val securityCodeLength: Int,
) {
    Visa(numberLengths = setOf(13, 16, 19), securityCodeLength = 3),
    Mastercard(numberLengths = setOf(16), securityCodeLength = 3),
    Amex(numberLengths = setOf(15), securityCodeLength = 4),
    Discover(numberLengths = setOf(16, 19), securityCodeLength = 3),
    Other(numberLengths = (12..19).toSet(), securityCodeLength = 3),
    ;

    companion object {
        fun of(digits: String): CardBrand =
            when {
                digits.startsWith("4") -> Visa
                digits.startsWith("34") || digits.startsWith("37") -> Amex
                digits.prefixIn(51..55, length = 2) || digits.prefixIn(2221..2720, length = 4) -> Mastercard
                digits.startsWith("6011") || digits.startsWith("65") || digits.prefixIn(644..649, length = 3) -> Discover
                else -> Other
            }

        private fun String.prefixIn(
            range: IntRange,
            length: Int,
        ): Boolean = this.length >= length && take(length).toInt() in range
    }
}
