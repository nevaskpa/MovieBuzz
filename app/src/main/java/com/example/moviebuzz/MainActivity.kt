package com.example.moviebuzz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = MovieRepository(apiKey = BuildConfig.TMDB_API_KEY)

        val viewModelFactory = MovieViewModelFactory(repository)
        val viewModel: MovieViewModel = ViewModelProvider(this, viewModelFactory)[MovieViewModel::class.java]
        setContent {
            MaterialTheme {
                  MovieScreenPagination(viewModel = viewModel)
            }
        }
    }
}
