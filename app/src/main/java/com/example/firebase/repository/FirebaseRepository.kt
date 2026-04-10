package com.example.firebase.repository

import com.example.firebase.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getMovies(): List<Movie> {
        return try {
            db.collection("movies").get().await().toObjects(Movie::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getShowtimes(movieId: String): List<Showtime> {
        return try {
            db.collection("showtimes")
                .whereEqualTo("movieId", movieId)
                .get().await().toObjects(Showtime::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTheater(theaterId: String): Theater? {
        return try {
            db.collection("theaters").document(theaterId).get().await().toObject(Theater::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun bookTicket(ticket: Ticket): Boolean {
        return try {
            db.collection("tickets").add(ticket).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getTickets(userId: String): List<Ticket> {
        return try {
            db.collection("tickets")
                .whereEqualTo("userId", userId)
                .get().await().toObjects(Ticket::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
