package com.example.dogapp.feature.identify.presentation

sealed interface IdentifyAction {
    class PhotoPick(
        val photo: ByteArray,
    ) : IdentifyAction
}
