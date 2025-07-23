package com.example.googlepayintegration.util

import android.content.Context
import com.google.android.gms.common.internal.Constants
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object PaymentUtil {
    private const val TEST_GIT =  "https://github.com/google-pay/android-quickstart/blob/f1caab23fdaadd8d133d31aeb612ecbdbdeda5ed/kotlin/app/src/androidTest/java/com/google/android/gms/samples/pay/GooglePayCheckoutTest/SampleGooglePayCheckoutTest.kt"

    // GPay API version
    private val baseRequest = JSONObject()
        .put("apiVersion", 2)
        .put("apiVersionMinor", 0)



    // Request a payment token for your payment provider
    private fun gatewayTokenizationSpecification(): JSONObject {
        return JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put("parameters", JSONObject(mapOf(
                "gateway" to "example",
                "gatewayMerchantId" to "exampleGatewayMerchantId")))
        }
    }

    // card networks that your application accepts\
    private val allowedCardNetworks = JSONArray(listOf(
        "AMEX",
        "DISCOVER",
        "INTERAC",
        "JCB",
        "MASTERCARD",
        "VISA"))


    private val allowedCardAuthMethods = JSONArray(listOf(
        "PAN_ONLY",
        "CRYPTOGRAM_3DS"))


    private val cardPaymentMethod: JSONObject = baseCardPaymentMethod()
        .put("tokenizationSpecification", gatewayTokenizationSpecification())

    val allowedPaymentMethods: JSONArray = JSONArray().put(cardPaymentMethod)




    // allowed payment methods
    // Only card  allowed for now
    private fun baseCardPaymentMethod(): JSONObject =
        JSONObject()
            .put("type", "CARD")
            .put("parameters", JSONObject()
                .put("allowedAuthMethods", allowedCardAuthMethods)
                .put("allowedCardNetworks", allowedCardNetworks)
                .put("billingAddressRequired", true)
                .put("billingAddressParameters", JSONObject()
                    .put("format", "FULL")
                )
            )

    //  PaymentsClient instance
    fun createPaymentsClient(context: Context): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
            .build()

        return Wallet.getPaymentsClient(context, walletOptions)
    }

    fun isReadyToPayRequest(): JSONObject? =
        try {
            baseRequest
                .put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod()))
        } catch (e: JSONException) {
            null
        }

    private fun getTransactionInfo(price: String): JSONObject =
        JSONObject()
            .put("totalPrice", price)
            .put("totalPriceStatus", "FINAL")
            .put("countryCode", "US")
            .put("currencyCode", "USD")

    private val merchantInfo: JSONObject =
        JSONObject().put("merchantName", "Hearthstone")



    fun getPaymentDataRequest(priceLabel: String): JSONObject =
        baseRequest
            .put("allowedPaymentMethods", JSONArray().put(cardPaymentMethod))
            .put("transactionInfo", getTransactionInfo(priceLabel))
            .put("merchantInfo", merchantInfo)
            .put("shippingAddressRequired", true)
            .put("shippingAddressParameters", JSONObject()
                .put("phoneNumberRequired", false)
                .put("allowedCountryCodes", JSONArray(listOf("US", "GB")))
            )

}