package com.example.pc02_llerena_23100548.presentation.converter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pc02_llerena_23100548.data.model.ConversionHistory
import com.example.pc02_llerena_23100548.data.remote.FirebaseAuthManager
import com.example.pc02_llerena_23100548.data.remote.FirestoreManager
import com.example.pc02_llerena_23100548.data.remote.frankfurter.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ConverterViewModel : ViewModel() {

    private val _rates = MutableStateFlow<Map<String, Double>>(emptyMap())
    val rates: StateFlow<Map<String, Double>> = _rates

    private val _conversionResult = MutableStateFlow<String?>(null)
    val conversionResult: StateFlow<String?> = _conversionResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _historyList = MutableStateFlow<List<ConversionHistory>>(emptyList())
    val historyList: StateFlow<List<ConversionHistory>> = _historyList

    // Hardcoded fallback rates relative to 1 EUR
    private val defaultRates = mapOf(
        "EUR" to 1.0,
        "USD" to 1.08,
        "PEN" to 4.05,
        "GBP" to 0.85,
        "JPY" to 174.0
    )

    init {
        loadRatesAndHistory()
    }

    fun loadRatesAndHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // 1. Load rates
            try {
                val response = RetrofitInstance.api.getLatestRates("EUR")
                val apiRates = response.rates.toMutableMap()
                
                // Add EUR (base) and PEN (manually, not supported by Frankfurter API)
                apiRates["EUR"] = 1.0
                apiRates["PEN"] = 4.05 // 1 EUR = 4.05 PEN approx
                
                _rates.value = apiRates
                
                // Sync with Firestore "rates" collection
                FirestoreManager.saveRates(apiRates)
            } catch (e: Exception) {
                // If API fails, try to get rates from Firestore
                val firestoreRates = FirestoreManager.getRates()
                if (firestoreRates.isNotEmpty()) {
                    _rates.value = firestoreRates
                } else {
                    // Fallback to local hardcoded rates
                    _rates.value = defaultRates
                }
            }

            // 2. Load conversion history for current user
            loadHistory()
            _isLoading.value = false
        }
    }

    fun loadHistory() {
        val uid = FirebaseAuthManager.getCurrentUserUid()
        if (uid != null) {
            viewModelScope.launch {
                val history = FirestoreManager.getConversionHistory(uid)
                _historyList.value = history
            }
        }
    }

    fun convert(amountStr: String, fromCurrency: String, toCurrency: String) {
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _error.value = "Por favor, ingresa un monto válido mayor a 0."
            return
        }

        val ratesMap = _rates.value
        val fromRate = ratesMap[fromCurrency]
        val toRate = ratesMap[toCurrency]

        if (fromRate == null || toRate == null) {
            _error.value = "Tasas de cambio no disponibles para las monedas seleccionadas."
            return
        }

        _error.value = null
        _isLoading.value = true

        viewModelScope.launch {
            // Formula to convert using base EUR:
            // amount_in_EUR = amount / fromRate
            // result = amount_in_EUR * toRate
            val result = (amount / fromRate) * toRate
            val formattedResult = String.format("%.2f", result)
            
            val resultMessage = "$amount $fromCurrency equivalen a $formattedResult $toCurrency"
            _conversionResult.value = resultMessage

            // Save to Firestore conversions collection
            val uid = FirebaseAuthManager.getCurrentUserUid()
            if (uid != null) {
                val conversionRecord = ConversionHistory(
                    userId = uid,
                    amount = amount,
                    fromCurrency = fromCurrency,
                    toCurrency = toCurrency,
                    result = result
                )
                
                val saveResult = FirestoreManager.saveConversion(conversionRecord)
                if (saveResult.isSuccess) {
                    loadHistory()
                } else {
                    _error.value = "Error al guardar en el historial: " + saveResult.exceptionOrNull()?.localizedMessage
                }
            }
            _isLoading.value = false
        }
    }

    fun clearResult() {
        _conversionResult.value = null
    }
}
