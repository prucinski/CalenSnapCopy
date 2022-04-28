package com.example.ocrhotel.payment

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.ocrhotel.databinding.ActivityPaymentPopupBinding
import com.example.ocrhotel.databinding.ActivityPaymentPopupBusinessBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.wallet.*
import org.joda.time.DateTime
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class PaymentPopupBusiness : AppCompatActivity() {
    //via https://developer.android.com/codelabs/pay-android-checkout#5

    private val model: CheckoutViewModel by viewModels()
    private lateinit var paymentsClient: PaymentsClient
    private lateinit var binding: ActivityPaymentPopupBusinessBinding
    private lateinit var googlePayButton: View
    //accepting only Mastercard or Visa
    private val baseCardPaymentMethod = JSONObject().apply {
        put("type", "CARD")
        put("parameters", JSONObject().apply {
            put("allowedCardNetworks", JSONArray(listOf("VISA", "MASTERCARD")))
            put("allowedAuthMethods", JSONArray(listOf("PAN_ONLY", "CRYPTOGRAM_3DS")))
        })
    }

    //using API 2.
    private val googlePayBaseConfiguration = JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
        put("allowedPaymentMethods",  JSONArray().put(baseCardPaymentMethod))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)

        binding = ActivityPaymentPopupBusinessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        paymentsClient = createPaymentsClient(this)

        googlePayButton = binding.googlePayButton.root

        //checking if the user of the app is able to pay.
        val readyToPayRequest =
            IsReadyToPayRequest.fromJson(googlePayBaseConfiguration.toString())
        val readyToPayTask = paymentsClient.isReadyToPay(readyToPayRequest)

        readyToPayTask.addOnCompleteListener { task ->
            try {
                task.getResult(ApiException::class.java)?.let(::setGooglePayAvailable)
            } catch (exception: ApiException) {
                Log.e("PAY",exception.toString())
                // Error determining readiness to use Google Pay.
                // Inspect the logs for more details.
            }
        }

    }


    private fun createPaymentsClient(activity: Activity): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_TEST).build()
        return Wallet.getPaymentsClient(activity, walletOptions)
    }

    private fun setGooglePayAvailable(available: Boolean) {
        if (available) {
            googlePayButton.visibility = View.VISIBLE
            googlePayButton.setOnClickListener { requestPayment() }


        } else {
            Toast.makeText(this,
                "Sorry, but it doesn't seem like you have Google Pay set up!",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestPayment() {
        // Disables the button to prevent multiple clicks.
        googlePayButton.isClickable = false

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        val dummyPriceCents = 100L
        val shippingCostCents = 900L
        val task = model.getLoadPaymentDataTask(dummyPriceCents + shippingCostCents)

        task.addOnCompleteListener { completedTask ->
            if (completedTask.isSuccessful) {
                completedTask.result.let(::handlePaymentSuccess)
            } else {
                when (val exception = completedTask.exception) {
                    is ResolvableApiException -> {
                        resolvePaymentForResult.launch(
                            IntentSenderRequest.Builder(exception.resolution).build()
                        )
                    }
                    is ApiException -> {
                        handleError(exception.statusCode, exception.message)
                    }
                    else -> {
                        handleError(
                            CommonStatusCodes.INTERNAL_ERROR, "Unexpected non API" +
                                    " exception when trying to deliver the task result to an activity!"
                        )
                    }
                }
            }

            // Re-enables the Google Pay payment button.
            googlePayButton.isClickable = true
        }
    }

    /**
     * PaymentData response object contains the payment information, as well as any additional
     * requested information, such as billing and shipping address.
     *
     * @param paymentData A response object returned by Google after a payer approves payment.
     * @see [Payment
     * Data](https://developers.google.com/pay/api/android/reference/object.PaymentData)
     */
    private fun handlePaymentSuccess(paymentData: PaymentData) {
        val paymentInformation = paymentData.toJson()

        try {
            // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
            val paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            val billingName = paymentMethodData.getJSONObject("info")
                .getJSONObject("billingAddress").getString("name")
            Log.d("BillingName", billingName)

            Toast.makeText(this,
                "Payment successful!", Toast.LENGTH_LONG).show()

            // Logging token string.
            Log.d("Google Pay token", paymentMethodData
                .getJSONObject("tokenizationData")
                .getString("token"))
            // Updating the business account. Business account comes with built-in premium
            val expirationDate = DateTime.now().plusDays(30)


            val sh = getSharedPreferences("com.example.ocrhotel_preferences", MODE_PRIVATE)
            val myEdit = sh.edit()
            myEdit.putBoolean("isPremiumUser", true)
            myEdit.putBoolean("isBusinessUser", true)
            myEdit.putInt("businessExpirationMonth", expirationDate.monthOfYear)
            myEdit.putInt("businessExpirationDay", expirationDate.dayOfMonth)
            myEdit.apply()

            // Close the activity
            this.finish()

        } catch (error: JSONException) {
            Log.e("handlePaymentSuccess", "Error: $error")
        }

    }

    // Handle potential conflict from calling loadPaymentData
    private val resolvePaymentForResult = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            result: ActivityResult ->
        when (result.resultCode) {
            RESULT_OK ->
                result.data?.let { intent ->
                    PaymentData.getFromIntent(intent)?.let(::handlePaymentSuccess)
                }

            RESULT_CANCELED -> {
                // The user cancelled the payment attempt
            }
        }
    }

    /**
     * At this stage, the user has already seen a popup informing them an error occurred. Normally,
     * only logging is required.
     *
     * @param statusCode will hold the value of any constant from CommonStatusCode or one of the
     * WalletConstants.ERROR_CODE_* constants.
     * @see [
     * Wallet Constants Library](https://developers.google.com/android/reference/com/google/android/gms/wallet/WalletConstants.constant-summary)
     */
    private fun handleError(statusCode: Int, message: String?) {
        Log.e("Google Pay API error", "Error code: $statusCode, Message: $message")
    }
}