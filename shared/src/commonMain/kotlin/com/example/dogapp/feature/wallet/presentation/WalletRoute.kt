package com.example.dogapp.feature.wallet.presentation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
data object WalletRoute : NavKey

val walletNavigationSerializers =
    SerializersModule {
        polymorphic(NavKey::class) {
            subclass(WalletRoute::class, WalletRoute.serializer())
            subclass(AddCardRoute::class, AddCardRoute.serializer())
            subclass(CardDetailRoute::class, CardDetailRoute.serializer())
        }
    }
