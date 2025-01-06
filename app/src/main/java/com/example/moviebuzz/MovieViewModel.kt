package com.example.moviebuzz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MovieViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _selectedGenre = MutableStateFlow<Int?>(null) // null represents "All"
    val selectedGenre: StateFlow<Int?> = _selectedGenre.asStateFlow()

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    init {
        viewModelScope.launch {
            _genres.value = repository.fetchGenres()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val movies: Flow<PagingData<Movie>> = _selectedGenre
        .flatMapLatest { genreId ->
            repository.getMoviesStream(genreId)
        }
        .cachedIn(viewModelScope)

    fun selectGenre(genreId: Int?) {
        _selectedGenre.value = genreId
    }
}
