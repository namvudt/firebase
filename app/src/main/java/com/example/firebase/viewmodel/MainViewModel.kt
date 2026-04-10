package com.example.firebase.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.firebase.model.*
import com.example.firebase.repository.FirebaseRepository
import com.example.firebase.service.NotificationWorker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val repository = FirebaseRepository()
    private val workManager = WorkManager.getInstance(application)

    private val _user = MutableStateFlow(auth.currentUser)
    val user: StateFlow<com.google.firebase.auth.FirebaseUser?> = _user

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies

    private val _tickets = MutableStateFlow<List<Ticket>>(emptyList())
    val tickets: StateFlow<List<Ticket>> = _tickets

    init {
        loadMovies()
        loadUserTickets()
    }

    fun loadMovies() {
        viewModelScope.launch {
            _movies.value = repository.getMovies()
        }
    }

    fun loadUserTickets() {
        auth.currentUser?.uid?.let { uid ->
            viewModelScope.launch {
                _tickets.value = repository.getTickets(uid)
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _user.value = null
    }

    suspend fun bookTicket(showtime: Showtime, movie: Movie, theater: Theater, seat: String): Boolean {
        val currentUser = auth.currentUser ?: return false
        val ticket = Ticket(
            userId = currentUser.uid,
            showtimeId = showtime.id,
            movieTitle = movie.title,
            theaterName = theater.name,
            startTime = showtime.startTime,
            seatNumber = seat
        )
        val success = repository.bookTicket(ticket)
        if (success) {
            loadUserTickets()
            scheduleNotification(movie.title, showtime.startTime.toDate().time)
        }
        return success
    }

    private fun scheduleNotification(movieTitle: String, startTimeMillis: Long) {
        val currentTime = System.currentTimeMillis()
        // Nhắc trước 15 phút. Nếu đã qua giờ nhắc thì nhắc sau 5 giây để test.
        var delay = (startTimeMillis - 15 * 60 * 1000) - currentTime
        if (delay < 0) delay = 5000 

        val data = Data.Builder()
            .putString("movieTitle", movieTitle)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        workManager.enqueue(workRequest)
    }
}
