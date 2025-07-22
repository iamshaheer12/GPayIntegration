package com.example.googlepayintegration

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.googlepayintegration.util.PaymentUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.Executor
import kotlin.coroutines.resume

class GPayViewMModel(
    application: Application
): AndroidViewModel(application)  {

    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
    private val paymentsClient = PaymentUtil.createPaymentsClient(application)

    private val _gPayUiState: MutableStateFlow<GPayUiState> = MutableStateFlow<GPayUiState>(GPayUiState.NotStarted)
    val gPayUiState: MutableStateFlow<GPayUiState> = _gPayUiState



    init {
        viewModelScope.launch(dispatcher) {
            isReadyToPay()
        }
    }

    /*
    Determine if Google Pay is available and ready to pay
     */
    private suspend fun isReadyToPay() {
        val request = IsReadyToPayRequest.fromJson(PaymentUtil.isReadyToPayRequest().toString())
        val state = try {
            val result = paymentsClient.isReadyToPay(request).await()
            if (result) {
                GPayUiState.Available
            } else {
                GPayUiState.Error(code = 0, message = "Google Pay is not available")
            }
        } catch (ae: ApiException) {
            GPayUiState.Error(code = ae.statusCode, message = ae.message)
        }


        _gPayUiState.update { state }
    }

    suspend fun startPaymentProcess(priceLabel: String):Task<PaymentData>{
        val pdRequest = PaymentDataRequest.fromJson(PaymentUtil.getPaymentDataRequest(priceLabel).toString())
        return paymentsClient.loadPaymentData(pdRequest)
    }


}

suspend fun <T> Task<T>.awaitTask(cancellationTokenSource: CancellationTokenSource? = null): Task<T> {
    return if (isComplete) this else suspendCancellableCoroutine { cont ->

        addOnCompleteListener(DirectExecutor, cont::resume)

        cancellationTokenSource?.let { cancellationSource ->
            cont.invokeOnCancellation { cancellationSource.cancel() }
        }
    }
}

/**
 * An [Executor] that just directly executes the [Runnable].
 */
private object DirectExecutor : Executor {
    override fun execute(r: Runnable) {
        r.run()
    }
}




