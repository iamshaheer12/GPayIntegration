package com.example.googlepayintegration

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.googlepayintegration.ui.theme.GooglePayIntegrationTheme
import com.example.googlepayintegration.ui.theme.ShopScreen
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.wallet.contract.TaskResultContracts.GetPaymentDataResult
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: GPayViewMModel by viewModels()
    val paymentDataLauncher = registerForActivityResult(GetPaymentDataResult()) { taskResult ->
        when (taskResult.status.statusCode) {
            CommonStatusCodes.SUCCESS -> {
                taskResult.result!!.let {
                    Log.i("Google Pay result:", it.toJson())
                }
            }
            //CommonStatusCodes.CANCELED -> The user canceled
            //CommonStatusCodes.DEVELOPER_ERROR -> The API returned an error (it.status: Status)
            //else -> Handle internal and other unexpected errors
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()
        setContent {
            val payState: GPayUiState by viewModel.gPayUiState.collectAsStateWithLifecycle()

            ShopScreen(
                title = "Men's Tech Shell Full-Zip",
                description = "A versatile full-zip that you can wear all day long and even...",
                price = "$50.20",
                image = R.drawable.image,
                payUiState = payState,
                onGooglePayButtonClick = this::requestPayment,
            )

        }
    }

    private fun requestPayment() {
        lifecycleScope.launch {
            val task =  viewModel.startPaymentProcess("30").awaitTask()
            paymentDataLauncher.launch(task)
        }


    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GooglePayIntegrationTheme {
        Greeting("Android")
    }
}