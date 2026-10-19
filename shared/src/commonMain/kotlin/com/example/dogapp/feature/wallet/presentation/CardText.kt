package com.example.dogapp.feature.wallet.presentation

import androidx.compose.runtime.Composable
import com.example.dogapp.feature.wallet.domain.CardBrand
import com.example.dogapp.feature.wallet.domain.CardExpiry
import com.example.dogapp.feature.wallet.domain.SavedCard
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.wallet_brand_amex
import dogapp.shared.generated.resources.wallet_brand_discover
import dogapp.shared.generated.resources.wallet_brand_mastercard
import dogapp.shared.generated.resources.wallet_brand_other
import dogapp.shared.generated.resources.wallet_brand_visa
import dogapp.shared.generated.resources.wallet_card_expires
import dogapp.shared.generated.resources.wallet_card_label
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

val CardBrand.displayName: StringResource
    get() =
        when (this) {
            CardBrand.Visa -> Res.string.wallet_brand_visa
            CardBrand.Mastercard -> Res.string.wallet_brand_mastercard
            CardBrand.Amex -> Res.string.wallet_brand_amex
            CardBrand.Discover -> Res.string.wallet_brand_discover
            CardBrand.Other -> Res.string.wallet_brand_other
        }

@Composable
fun SavedCard.label(): String = stringResource(Res.string.wallet_card_label, stringResource(brand.displayName), last4)

@Composable
fun CardExpiry.expiresText(): String =
    stringResource(Res.string.wallet_card_expires, "${month.toString().padStart(2, '0')}/${(year % 100).toString().padStart(2, '0')}")
