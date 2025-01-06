package com.example.moviebuzz

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieScreenPagination(
    viewModel: MovieViewModel
) {
    val movies = viewModel.movies.collectAsLazyPagingItems()
    val genres by viewModel.genres.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text(text = "MovieBuzz") }
                )
                if(genres.isNotEmpty()) {
                    GenreTabs(
                        genres = genres,
                        selectedGenreId = selectedGenre,
                        onGenreSelected = { genreId ->
                            viewModel.selectGenre(genreId)
                            movies.refresh() // Refresh PagingData when genre changes
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                count = movies.itemCount,
                key = movies.itemKey {movie -> movie.id}
            ) { index ->
                val movie = movies[index]
                movie?.let {
                    MovieGridItem(movie = it)
                }
            }

            // Handle Load States...
            movies.apply {
                when {
                    loadState.refresh is androidx.paging.LoadState.Loading -> {
                        item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                    }
                    loadState.append is androidx.paging.LoadState.Loading -> {
                        item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                    }
                    loadState.append is androidx.paging.LoadState.Error -> {
                        val e = movies.loadState.append as androidx.paging.LoadState.Error
                        item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                            ErrorItem(
                                message = e.error.localizedMessage ?: "Unknown Error",
                                onRetry = { movies.retry() }
                            )
                        }
                    }
                    loadState.refresh is androidx.paging.LoadState.Error -> {
                        val e = movies.loadState.refresh as androidx.paging.LoadState.Error
                        item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                            ErrorItem(
                                message = e.error.localizedMessage ?: "Unknown Error",
                                onRetry = { movies.retry() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GenreTabs(
    genres: List<Genre>,
    selectedGenreId: Int?,
    onGenreSelected: (Int?) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = if (selectedGenreId == null) 0 else genres.indexOfFirst { it.id == selectedGenreId } + 1,
        modifier = Modifier.fillMaxWidth(),
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {

        // "All" Tab
        Tab(
            selected = selectedGenreId == null,
            onClick = { onGenreSelected(null) },
            text = { Text("All") }
        )
        // Genre Tabs
        genres.forEach { genre ->
            Tab(
                selected = genre.id == selectedGenreId,
                onClick = { onGenreSelected(genre.id) },
                text = { Text(genre.name) }
            )
        }
    }
}

@Composable
fun MovieGridItem(movie: Movie) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = movie.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" },
                    error = painterResource(id = R.drawable.image_placeholder),
                    placeholder = painterResource(id = R.drawable.image_placeholder)
                ),
                contentDescription = "Poster for ${movie.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = movie.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Release: ${movie.releaseDate.take(4)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Text(
                text = "Rating: ${movie.voteAverage}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun ErrorItem(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
