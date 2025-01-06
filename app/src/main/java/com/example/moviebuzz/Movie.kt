package com.example.moviebuzz

data class Movie(
    val id: Int,
    val title: String,
    val releaseDate: String,
    val overview: String,
    val posterPath: String,
    val backdropPath: String,
    val voteAverage: Double,
    val voteCount: Int,
    val adult: Boolean,
    val originalLanguage: String,
    val originalTitle: String,
    val genreIds: List<Int>,
    val video: Boolean,
    val popularity: Double
)
