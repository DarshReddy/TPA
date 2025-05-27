package com.tp.tpa.data

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object ApiClient {
    private const val BASE_URL = "https://www.googleapis.com/youtube/v3/"
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    val service: YouTubeApiService = retrofit.create(YouTubeApiService::class.java)
}

interface YouTubeApiService {
    @GET("videos")
    suspend fun getVideoById(
        @Query("id") id: String,
        @Query("part") part: String = "snippet,contentDetails,statistics",
        @Query("key") apiKey: String = "AIzaSyBjlZa5GqTA3VlK-Jk0Slxb52KG1itPMuE"
    ): YouTubeResponse
}

data class YouTubeResponse(
    val items: List<YouTubeVideo>
)

data class YouTubeVideo(
    val id: String,
    val snippet: Snippet,
    val contentDetails: ContentDetails,
    val statistics: Statistics
)

data class Snippet(
    val title: String,
    val description: String,
    @Json(name = "publishedAt") private val publishedAt: String,
    @Json(name = "channelTitle") val channelTitle: String
) {
    val publishedDateTime: String by lazy {
        formatPublishedDate(publishedAt)
    }

    private fun formatPublishedDate(input: String): String {
        return try {
            val zonedDateTime = ZonedDateTime.parse(input)
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm a", Locale.getDefault())
            zonedDateTime.format(formatter)
        } catch (e: Exception) {
            e.localizedMessage
        }
    }
}

data class ContentDetails(
    @Json(name = "duration") private val rawDuration: String
) {
    val duration: String by lazy {
        formatDuration(rawDuration)
    }

    private fun formatDuration(input: String): String {
        return try {
            val duration = Duration.parse(input)
            val hours = duration.toHours()
            val minutes = duration.toMinutes() % 60
            val seconds = duration.seconds % 60
            String.format(Locale.getDefault(), "%02dh:%02dm:%02ds", hours, minutes, seconds)
        } catch (e: Exception) {
            e.localizedMessage
        }
    }
}

data class Statistics(
    @Json(name = "viewCount") private val _viewCount: String,
    @Json(name = "likeCount") private val _likeCount: String?
) {
    val viewCount: String by lazy {
        formatYoutubeCount(_viewCount.toLong())
    }
    val likeCount: String? by lazy {
        _likeCount?.let { formatYoutubeCount(it.toLong()) }
    }

    private fun formatYoutubeCount(count: Long): String {
        return when {
            count < 1_000 -> "$count"
            count < 1_00_000 -> String.format(Locale.getDefault(), "%.1fK", count / 1_000.0)
            count < 1_00_00_000 -> String.format(Locale.getDefault(), "%.1fL", count / 1_00_000.0)
            else -> String.format(Locale.getDefault(), "%.1fCr", count / 1_00_00_000.0)
        }.replace(".0", "")
    }
}
