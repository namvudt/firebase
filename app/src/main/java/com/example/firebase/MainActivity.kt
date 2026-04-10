package com.example.firebase

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebase.ui.screens.LoginScreen
import com.example.firebase.ui.screens.MovieDetailScreen
import com.example.firebase.ui.screens.MovieListScreen
import com.example.firebase.ui.screens.MyTicketsScreen
import com.example.firebase.ui.theme.FireBaseTheme
import com.example.firebase.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FireBaseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MainViewModel = viewModel()
                    val navController = rememberNavController()
                    val auth = remember { FirebaseAuth.getInstance() }
                    var currentUser by remember { mutableStateOf(auth.currentUser) }

                    // Permission request for Notifications on Android 13+
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = { isGranted -> }
                    )

                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }

                    val startDestination = if (currentUser != null) "movie_list" else "login"

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("login") {
                            LoginScreen(onLoginSuccess = {
                                currentUser = auth.currentUser
                                navController.navigate("movie_list") {
                                    popUpTo("login") { inclusive = true }
                                }
                            })
                        }
                        composable("movie_list") {
                            MovieListScreen(
                                viewModel = viewModel,
                                onMovieClick = { movie ->
                                    navController.currentBackStackEntry?.savedStateHandle?.set("movie_id", movie.id)
                                    navController.navigate("movie_detail")
                                },
                                onViewTickets = {
                                    navController.navigate("my_tickets")
                                }
                            )
                        }
                        composable("movie_detail") {
                            val movieId = navController.previousBackStackEntry?.savedStateHandle?.get<String>("movie_id")
                            val movies by viewModel.movies.collectAsState()
                            val movie = movies.find { it.id == movieId }

                            movie?.let {
                                MovieDetailScreen(
                                    movie = it,
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable("my_tickets") {
                            MyTicketsScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
