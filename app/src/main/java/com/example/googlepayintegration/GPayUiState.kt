package com.example.googlepayintegration

sealed class GPayUiState {
    object NotStarted : GPayUiState()
    object Available : GPayUiState()
    data class PaymentCompleted(val payerName: String) : GPayUiState()
    data class Error(val code: Int, val message: String? = null) : GPayUiState()
}