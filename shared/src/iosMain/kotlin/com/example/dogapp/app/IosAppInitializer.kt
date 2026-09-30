package com.example.dogapp.app

import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
fun initAppIos() {
    initApp(debug = Platform.isDebugBinary)
}
