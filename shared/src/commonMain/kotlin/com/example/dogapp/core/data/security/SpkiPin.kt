package com.example.dogapp.core.data.security

import kotlin.io.encoding.Base64

fun spkiPin(certificateDer: ByteArray): String? {
    val certificate = DerReader(certificateDer).readSequence() ?: return null
    val tbsCertificate = certificate.readSequence() ?: return null
    if (tbsCertificate.peekTag() == EXPLICIT_VERSION_TAG) tbsCertificate.readElement() ?: return null
    repeat(TBS_FIELDS_BEFORE_SPKI) { tbsCertificate.readElement() ?: return null }
    val subjectPublicKeyInfo = tbsCertificate.readElement(SEQUENCE_TAG) ?: return null
    return Base64.encode(sha256(certificateDer.copyOfRange(subjectPublicKeyInfo.start, subjectPublicKeyInfo.end)))
}

private const val SEQUENCE_TAG = 0x30
private const val EXPLICIT_VERSION_TAG = 0xA0
private const val TBS_FIELDS_BEFORE_SPKI = 5
private const val MAX_LENGTH_BYTES = 4

private class DerElement(
    val start: Int,
    val contentStart: Int,
    val end: Int,
)

private class DerReader(
    private val bytes: ByteArray,
    private var position: Int = 0,
    private val end: Int = bytes.size,
) {
    fun peekTag(): Int? = if (position < end) bytes[position].toInt() and 0xFF else null

    fun readSequence(): DerReader? = readElement(SEQUENCE_TAG)?.let { DerReader(bytes, it.contentStart, it.end) }

    fun readElement(tag: Int? = null): DerElement? {
        val start = position
        val actualTag = peekTag() ?: return null
        if (tag != null && actualTag != tag) return null
        var cursor = start + 1
        if (cursor >= end) return null
        val first = bytes[cursor++].toInt() and 0xFF
        var length = first
        if (first >= 0x80) {
            val count = first and 0x7F
            if (count == 0 || count > MAX_LENGTH_BYTES || count > end - cursor) return null
            length = 0
            repeat(count) { length = (length shl 8) or (bytes[cursor++].toInt() and 0xFF) }
            if (length < 0) return null
        }
        if (length > end - cursor) return null
        position = cursor + length
        return DerElement(start = start, contentStart = cursor, end = position)
    }
}
