package com.example.dogapp.feature.donate.data

import com.example.dogapp.core.data.donation.local.DonationDao
import com.example.dogapp.core.data.donation.local.DonationEntity
import com.example.dogapp.feature.donate.domain.Donation
import com.example.dogapp.feature.donate.domain.DonationAmount
import com.example.dogapp.feature.donate.domain.DonationRepository
import com.example.dogapp.feature.donate.domain.PaidWith
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

class DefaultDonationRepository(
    private val dao: DonationDao,
) : DonationRepository {
    override fun observeDonations(): Flow<List<Donation>> = dao.observeAll().map { entities -> entities.map { it.toDonation() } }

    override suspend fun record(donation: Donation) = dao.insert(donation.toEntity())

    private fun Donation.toEntity(): DonationEntity {
        val paid = paidWith
        return DonationEntity(
            id = id,
            charityId = charityId,
            charityName = charityName,
            amountPence = amount.pence,
            paidWith =
                when (paid) {
                    is PaidWith.Card -> PAID_WITH_CARD
                    is PaidWith.GooglePay -> PAID_WITH_GOOGLE_PAY
                },
            cardBrand = (paid as? PaidWith.Card)?.brandName,
            cardLast4 = (paid as? PaidWith.Card)?.last4,
            cardDescription = (paid as? PaidWith.GooglePay)?.cardDescription,
            donatedAtEpochMillis = donatedAt.toEpochMilliseconds(),
        )
    }

    private fun DonationEntity.toDonation(): Donation =
        Donation(
            id = id,
            charityId = charityId,
            charityName = charityName,
            amount = DonationAmount(amountPence),
            paidWith =
                when (paidWith) {
                    PAID_WITH_CARD -> PaidWith.Card(brandName = checkNotNull(cardBrand), last4 = checkNotNull(cardLast4))
                    PAID_WITH_GOOGLE_PAY -> PaidWith.GooglePay(cardDescription)
                    else -> error("Unknown paid_with value $paidWith")
                },
            donatedAt = Instant.fromEpochMilliseconds(donatedAtEpochMillis),
        )

    private companion object {
        const val PAID_WITH_CARD = "card"
        const val PAID_WITH_GOOGLE_PAY = "google_pay"
    }
}
