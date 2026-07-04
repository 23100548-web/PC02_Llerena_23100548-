package com.example.pc02_llerena_23100548.data.model

data class ConversionHistory(
    var id: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val amount: Double = 0.0,
    val fromCurrency: String = "",
    val toCurrency: String = "",
    val result: Double = 0.0
)
