package com.example.moviebuzz

import androidx.paging.PagingSource
import androidx.paging.PagingState
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class MoviePagingSource(
    private val apiKey: String,
    private val genreId: Int? = null // Optional genre filter
) : PagingSource<Int, Movie>() {

    private val client = OkHttpClient()
    private val tag = "MoviePagingSource"

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        val genreQuery = genreId?.let { "&with_genres=$it" } ?: ""

        val url = "https://api.themoviedb.org/3/discover/movie?api_key=$apiKey&page=$page$genreQuery"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return try {
            // Perform network call on IO dispatcher
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            Log.d(tag, "Response Code: ${response.code}")

            if (!response.isSuccessful) {
                Log.e(tag, "Unsuccessful network call: ${response.code}")
                return LoadResult.Error(IOException("Unsuccessful network call: ${response.code}"))
            }

            val bodyString = response.body?.string().orEmpty()
            Log.d(tag, "Response Body: $bodyString")

            val json = JSONObject(bodyString)
            val resultsArray = json.optJSONArray("results") ?: return LoadResult.Error(IOException("Invalid response"))

            val movieList = mutableListOf<Movie>()
            for (i in 0 until resultsArray.length()) {
                val item = resultsArray.getJSONObject(i)

                // Parse each field safely
                val id = item.optInt("id", 0)
                val title = item.optString("title", "")
                val releaseDate = item.optString("release_date", "")
                val overview = item.optString("overview", "")
                val posterPath = item.optString("poster_path", null)
                val backdropPath = item.optString("backdrop_path", null)
                val voteAverage = item.optDouble("vote_average", 0.0)
                val voteCount = item.optInt("vote_count", 0)
                val adult = item.optBoolean("adult", false)
                val originalLanguage = item.optString("original_language", "")
                val originalTitle = item.optString("original_title", "")
                val video = item.optBoolean("video", false)
                val popularity = item.optDouble("popularity", 0.0)

                val genreIdsJson = item.optJSONArray("genre_ids")
                val genreIds = mutableListOf<Int>()
                if (genreIdsJson != null) {
                    for (j in 0 until genreIdsJson.length()) {
                        genreIds.add(genreIdsJson.optInt(j))
                    }
                }

                val movie = Movie(
                    id = id,
                    title = title,
                    releaseDate = releaseDate,
                    overview = overview,
                    posterPath = posterPath,
                    backdropPath = backdropPath,
                    voteAverage = voteAverage,
                    voteCount = voteCount,
                    adult = adult,
                    originalLanguage = originalLanguage,
                    originalTitle = originalTitle,
                    genreIds = genreIds,
                    video = video,
                    popularity = popularity
                )
                movieList.add(movie)
            }

            val totalPages = json.optInt("total_pages", 1)
            val nextKey = if (page < totalPages) page + 1 else null

            LoadResult.Page(
                data = movieList,
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextKey
            )

        } catch (e: Exception) {
            Log.e(tag, "Error loading page $page: ${e.message}")
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { position ->
            state.closestPageToPosition(position)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(position)?.nextKey?.minus(1)
        }
    }
}
