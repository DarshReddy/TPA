package com.tp.tpa.data

class YouTubeRepository(private val api: YouTubeApiService) {
    suspend fun fetchMetadata(videoId: String): YouTubeVideo? {
        val response = api.getVideoById(id = videoId)
        return response.items.firstOrNull()
    }
}