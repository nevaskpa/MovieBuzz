package com.example.moviebuzz

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

class MovieRepository(
    private val apiKey: String
) {
    private val client = OkHttpClient()

    fun getMoviesStream(genreId: Int? = null, query: String? = null): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { MoviePagingSource(apiKey, genreId, query) }
        ).flow
    }

    suspend fun fetchGenres(): List<Genre> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://api.themoviedb.org/3/genre/movie/list?api_key=$apiKey&language=en-US"

                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                val response: Response = client.newCall(request).execute()

                response.use { resp ->
                    if (!resp.isSuccessful) {
                        throw Exception("Network call failed: code=${resp.code}")
                    }

                    val bodyString = resp.body?.string() ?: ""
                    val json = JSONObject(bodyString)
                    val genresArray =
                        json.optJSONArray("genres") ?: return@withContext emptyList<Genre>()

                    val genresList = mutableListOf<Genre>()
                    for (i in 0 until genresArray.length()) {
                        val genreObj = genresArray.getJSONObject(i)
                        val id = genreObj.getInt("id")
                        val name = genreObj.getString("name")
                        genresList.add(Genre(id, name))
                    }
                    genresList
                }
            } catch (e: Exception){
                println("An exception occurred: $e")
                emptyList<Genre>()
            }
        }
    }
}
