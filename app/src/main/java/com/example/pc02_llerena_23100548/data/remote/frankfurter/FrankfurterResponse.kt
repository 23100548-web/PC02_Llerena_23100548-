package com.example.pc02_llerena_23100548.data.remote.frankfurter

data class FrankfurterResponse(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)
