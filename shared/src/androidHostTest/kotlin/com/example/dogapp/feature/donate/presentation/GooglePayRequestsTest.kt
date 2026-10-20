package com.example.dogapp.feature.donate.presentation

import com.example.dogapp.feature.donate.domain.DonationAmount
import com.example.dogapp.feature.donate.domain.PaymentMethod
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GooglePayRequestsTest {
    @Test
    fun paymentRequestAsksForTheExactAmountInPoundsThroughTheTestGateway() {
        val request = Json.parseToJsonElement(GooglePayRequests.paymentData(DonationAmount(750))).jsonObject

        val transaction = request.getValue("transactionInfo").jsonObject
        assertEquals("7.50", transaction.getValue("totalPrice").jsonPrimitive.content)
        assertEquals("FINAL", transaction.getValue("totalPriceStatus").jsonPrimitive.content)
        assertEquals("GBP", transaction.getValue("currencyCode").jsonPrimitive.content)
        val tokenization =
            request
                .getValue("allowedPaymentMethods")
                .jsonArray
                .single()
                .jsonObject
                .getValue("tokenizationSpecification")
                .jsonObject
        assertEquals(
            "example",
            tokenization
                .getValue("parameters")
                .jsonObject
                .getValue("gateway")
                .jsonPrimitive.content,
        )
        assertEquals(
            "20.00",
            Json
                .parseToJsonElement(GooglePayRequests.paymentData(DonationAmount(2000)))
                .jsonObject
                .getValue("transactionInfo")
                .jsonObject
                .getValue("totalPrice")
                .jsonPrimitive.content,
        )
    }

    @Test
    fun paymentDataGivesTheTokenAndCardDescriptionAndIsRejectedWithoutAToken() {
        assertEquals(
            PaymentMethod.GooglePay(token = "examplePaymentMethodToken", cardDescription = "Visa •••• 1111"),
            GooglePayRequests.googlePayMethod(paymentData(token = "\"examplePaymentMethodToken\"", description = "\"Visa •••• 1111\"")),
        )
        assertEquals(
            PaymentMethod.GooglePay(token = "examplePaymentMethodToken", cardDescription = null),
            GooglePayRequests.googlePayMethod(paymentData(token = "\"examplePaymentMethodToken\"", description = "\"\"")),
        )
        assertNull(GooglePayRequests.googlePayMethod(paymentData(token = "\"\"", description = "\"Visa •••• 1111\"")))
        assertNull(GooglePayRequests.googlePayMethod("not json"))
    }

    private fun paymentData(
        token: String,
        description: String,
    ): String =
        """{"apiVersion":2,"apiVersionMinor":0,"paymentMethodData":{"type":"CARD","description":$description,""" +
            """"tokenizationData":{"type":"PAYMENT_GATEWAY","token":$token}}}"""
}
