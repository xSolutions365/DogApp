package com.example.dogapp.feature.donate.presentation

import com.example.dogapp.feature.donate.domain.DonationAmount
import com.example.dogapp.feature.donate.domain.PaymentMethod
import com.google.android.gms.common.api.CommonStatusCodes
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

internal object GooglePayRequests {
    private val cardPaymentMethod =
        buildJsonObject {
            put("type", "CARD")
            putJsonObject("parameters") {
                putJsonArray("allowedAuthMethods") {
                    add(JsonPrimitive("PAN_ONLY"))
                    add(JsonPrimitive("CRYPTOGRAM_3DS"))
                }
                putJsonArray("allowedCardNetworks") {
                    add(JsonPrimitive("AMEX"))
                    add(JsonPrimitive("MASTERCARD"))
                    add(JsonPrimitive("VISA"))
                }
            }
        }

    private val tokenizedCardPaymentMethod =
        JsonObject(
            cardPaymentMethod +
                (
                    "tokenizationSpecification" to
                        buildJsonObject {
                            put("type", "PAYMENT_GATEWAY")
                            putJsonObject("parameters") {
                                put("gateway", "example")
                                put("gatewayMerchantId", "exampleGatewayMerchantId")
                            }
                        }
                ),
        )

    val allowedPaymentMethods: String = buildJsonArray { add(cardPaymentMethod) }.toString()

    fun isReadyToPay(): String =
        buildJsonObject {
            putApiVersion()
            put("allowedPaymentMethods", buildJsonArray { add(cardPaymentMethod) })
        }.toString()

    fun paymentData(amount: DonationAmount): String =
        buildJsonObject {
            putApiVersion()
            put("allowedPaymentMethods", JsonArray(listOf(tokenizedCardPaymentMethod)))
            putJsonObject("transactionInfo") {
                put("totalPriceStatus", "FINAL")
                put("totalPrice", amount.priceText())
                put("currencyCode", "GBP")
                put("countryCode", "GB")
            }
            putJsonObject("merchantInfo") {
                put("merchantName", "DogApp")
            }
        }.toString()

    fun googlePayResult(
        statusCode: Int,
        paymentDataJson: String?,
    ): PaymentMethod.GooglePay =
        paymentDataJson?.takeIf { statusCode == CommonStatusCodes.SUCCESS }?.let(::googlePayMethod)
            ?: PaymentMethod.GooglePay(token = null, cardDescription = null)

    private fun googlePayMethod(paymentDataJson: String): PaymentMethod.GooglePay? {
        val paymentMethodData =
            runCatching { Json.parseToJsonElement(paymentDataJson).jsonObject["paymentMethodData"]?.jsonObject }.getOrNull()
                ?: return null
        val token =
            paymentMethodData["tokenizationData"]
                ?.jsonObject
                ?.get("token")
                ?.jsonPrimitive
                ?.contentOrNull
                ?.takeIf { it.isNotBlank() }
        val description = paymentMethodData["description"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
        return PaymentMethod.GooglePay(token = token, cardDescription = description)
    }

    private fun JsonObjectBuilder.putApiVersion() {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
    }

    private fun DonationAmount.priceText(): String = "${pence / 100}.${(pence % 100).toString().padStart(2, '0')}"
}
