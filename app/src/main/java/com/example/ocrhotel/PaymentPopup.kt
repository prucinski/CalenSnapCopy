package com.example.ocrhotel

import android.app.Activity
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.example.ocrhotel.databinding.ActivityMainBinding
import com.example.ocrhotel.databinding.ActivityPaymentPopupBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONArray
import org.json.JSONObject

class PaymentPopup : AppCompatActivity() {
    //via https://developer.android.com/codelabs/pay-android-checkout#5
    private lateinit var paymentsClient: PaymentsClient
    private lateinit var binding: ActivityPaymentPopupBinding
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
        setContentView(R.layout.activity_payment_popup)
        Log.d(" Activity 2 Created", " ")
        binding = ActivityPaymentPopupBinding.inflate(layoutInflater)
        paymentsClient = createPaymentsClient(this)

        //checking if the user of the app is able to pay.
        val readyToPayRequest =
            IsReadyToPayRequest.fromJson(googlePayBaseConfiguration.toString())
        val readyToPayTask = paymentsClient.isReadyToPay(readyToPayRequest)

        readyToPayTask.addOnCompleteListener { task ->
            try {
                task.getResult(ApiException::class.java)?.let(::setGooglePayAvailable)
            } catch (exception: ApiException) {
                // Error determining readiness to use Google Pay.
                // Inspect the logs for more details.
            }
        }


    }

    fun createPaymentsClient(activity: Activity): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_TEST).build()
        return Wallet.getPaymentsClient(activity, walletOptions)
    }
    private fun setGooglePayAvailable(available: Boolean) {
        if (available) {
            Log.d("INFO", "Google pay initiated")
            googlePayButton = binding.googlePayButton.root
            //TODO: WHY IS THIS NOT WORKING??
            //THIS IS PRECISELY WHAT THEY HAVE IN THEIR REPO TOO
            //https://github.com/google-pay/android-quickstart/blob/master/kotlin/app/src/main/java/com/google/android/gms/samples/wallet/activity/CheckoutActivity.kt
            googlePayButton.visibility = View.VISIBLE
            googlePayButton.setOnClickListener { requestPayment() }
        } else {
            Toast.makeText(this, "Sorry, but it doesn't seem like you have google pay set up!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestPayment() {
        Log.d("INFO", "Payment requested")
        // TODO: Perform transaction
    }
}