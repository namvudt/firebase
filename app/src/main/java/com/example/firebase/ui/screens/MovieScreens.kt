package com.example.firebase.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.firebase.model.Movie
import com.example.firebase.model.Showtime
import com.example.firebase.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(viewModel: MainViewModel, onMovieClick: (Movie) -> Unit, onViewTickets: () -> Unit) {
    val movies by viewModel.movies.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movies") },
                actions = {
                    IconButton(onClick = onViewTickets) {
                        Icon(Icons.Default.List, contentDescription = "My Tickets")
                    }
                }
            )
        }
    ) { padding ->
        if (movies.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No movies available. Please add some to Firestore 'movies' collection.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(movies) { movie ->
                    MovieCard(movie = movie, onClick = { onMovieClick(movie) })
                }
            }
        }
    }
}

@Composable
fun MovieCard(movie: Movie, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() }
            .fillMaxWidth()
    ) {
        Column {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.title,
                modifier = Modifier.height(200.dp).fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Text(
                text = movie.title,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(movie: Movie, viewModel: MainViewModel, onBack: () -> Unit) {
    var showtimes by remember { mutableStateOf<List<Showtime>>(emptyList()) }
    val repository = remember { com.example.firebase.repository.FirebaseRepository() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(movie.id) {
        showtimes = repository.getShowtimes(movie.id)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(movie.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = null,
                modifier = Modifier.height(300.dp).fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(movie.description, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Select Showtime:", style = MaterialTheme.typography.titleMedium)
            
            if (showtimes.isEmpty()) {
                Text("No showtimes available for this movie.", modifier = Modifier.padding(top = 8.dp))
            }

            LazyColumn {
                items(showtimes) { showtime ->
                    ListItem(
                        headlineContent = { Text("${showtime.startTime.toDate()}") },
                        supportingContent = { Text("Price: $${showtime.price}") },
                        trailingContent = {
                            Button(onClick = {
                                scope.launch {
                                    val theater = repository.getTheater(showtime.theaterId)
                                    if (theater != null) {
                                        val success = viewModel.bookTicket(showtime, movie, theater, "A1")
                                        if (success) {
                                            snackbarHostState.showSnackbar("Ticket booked successfully!")
                                        } else {
                                            snackbarHostState.showSnackbar("Failed to book ticket")
                                        }
                                    } else {
                                        snackbarHostState.showSnackbar("Theater not found")
                                    }
                                }
                            }) {
                                Text("Book")
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val tickets by viewModel.tickets.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tickets") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (tickets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You haven't booked any tickets yet.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(tickets) { ticket ->
                    Card(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(ticket.movieTitle, style = MaterialTheme.typography.titleLarge)
                            Text("Theater: ${ticket.theaterName}")
                            Text("Seat: ${ticket.seatNumber}")
                            Text("Time: ${ticket.startTime.toDate()}")
                        }
                    }
                }
            }
        }
    }
}
