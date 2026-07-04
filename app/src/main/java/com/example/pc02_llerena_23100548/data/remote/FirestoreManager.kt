package com.example.pc02_llerena_23100548.data.remote

import com.example.pc02_llerena_23100548.data.model.ConversionHistory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

object FirestoreManager {

    private val firestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    suspend fun saveConversion(conversion: ConversionHistory): Result<Unit> {
        return try {
            val docRef = firestore.collection("conversions").document()
            conversion.id = docRef.id
            docRef.set(conversion).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getConversionHistory(userId: String): List<ConversionHistory> {
        return try {
            val snapshot = firestore.collection("conversions")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(ConversionHistory::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveRates(rates: Map<String, Double>): Result<Unit> {
        return try {
            // Save rates to a collection named "rates"
            // We save each currency as a document
            val batch = firestore.batch()
            val collectionRef = firestore.collection("rates")
            for ((currency, rate) in rates) {
                val docRef = collectionRef.document(currency)
                val data = hashMapOf(
                    "code" to currency,
                    "rate" to rate,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(docRef, data)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRates(): Map<String, Double> {
        return try {
            val snapshot = firestore.collection("rates").get().await()
            val ratesMap = mutableMapOf<String, Double>()
            for (doc in snapshot.documents) {
                val code = doc.id
                val rate = doc.getDouble("rate")
                if (rate != null) {
                    ratesMap[code] = rate
                }
            }
            ratesMap
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
