package com.example.dogapp.feature.donate.domain

data class DonationAmount(
    val pence: Int,
) : Comparable<DonationAmount> {
    override fun compareTo(other: DonationAmount): Int = pence.compareTo(other.pence)

    companion object {
        val presets: List<DonationAmount> = listOf(300, 500, 1000, 2000).map(::DonationAmount)
        val minimum = DonationAmount(100)
        val maximum = DonationAmount(50_000)

        fun fromPounds(text: String): DonationAmount? {
            val parts = text.trim().removePrefix("£").split('.')
            if (parts.size > 2) return null
            val pounds = parts[0].takeIf { it.isNotEmpty() && it.all(Char::isDigit) }?.toIntOrNull() ?: return null
            val fraction = parts.getOrNull(1) ?: ""
            if (fraction.length > 2 || !fraction.all(Char::isDigit)) return null
            val pence = pounds * 100 + fraction.padEnd(2, '0').toInt()
            return DonationAmount(pence).takeIf { it in minimum..maximum }
        }
    }
}
