package com.example.dogapp.feature.wallet.data

import com.example.dogapp.feature.wallet.domain.WalletChange
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.wallet_prompt_remove_card_title
import dogapp.shared.generated.resources.wallet_prompt_save_card_title
import dogapp.shared.generated.resources.wallet_prompt_set_up_title
import org.jetbrains.compose.resources.StringResource

internal val WalletChange.promptTitle: StringResource
    get() =
        when (this) {
            WalletChange.SetUp -> Res.string.wallet_prompt_set_up_title
            WalletChange.CardAdded -> Res.string.wallet_prompt_save_card_title
            WalletChange.CardRemoved -> Res.string.wallet_prompt_remove_card_title
        }
