package com.example.pc02_llerena_23100548.data.remote.frankfurter

import retrofit2.http.GET
import retrofit2.http.Query

interface FrankfurterService {

    @GET("latest")
    suspend fun getLatestRates(
        @Query("from") base: String = "EUR"
    ): FrankfurterResponse
}
