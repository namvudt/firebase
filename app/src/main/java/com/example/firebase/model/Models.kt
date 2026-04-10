package com.example.firebase.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = ""
)

data class Movie(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val posterUrl: String = "",
    val duration: Int = 0, // in minutes
    val genre: String = ""
)

data class Theater(
    val id: String = "",
    val name: String = "",
    val location: String = ""
)

data class Showtime(
    val id: String = "",
    val movieId: String = "",
    val theaterId: String = "",
    val startTime: Timestamp = Timestamp.now(),
    val price: Double = 0.0
)

data class Ticket(
    val id: String = "",
    val userId: String = "",
    val showtimeId: String = "",
    val movieTitle: String = "",
    val theaterName: String = "",
    val startTime: Timestamp = Timestamp.now(),
    val seatNumber: String = "",
    val bookingTime: Timestamp = Timestamp.now()
)
