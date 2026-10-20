package com.example.dogapp.core.data.security

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH

@OptIn(ExperimentalForeignApi::class)
actual fun sha256(bytes: ByteArray): ByteArray {
    val digest = ByteArray(CC_SHA256_DIGEST_LENGTH)
    digest.usePinned { output ->
        if (bytes.isEmpty()) {
            CC_SHA256(null, 0u, output.addressOf(0).reinterpret())
        } else {
            bytes.usePinned { input -> CC_SHA256(input.addressOf(0), bytes.size.convert(), output.addressOf(0).reinterpret()) }
        }
    }
    return digest
}
