package com.tp.tpa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tp.tpa.data.ApiClient
import com.tp.tpa.data.YouTubeRepository
import com.tp.tpa.data.YouTubeVideo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VideoMetaViewModel : ViewModel() {
    private val repository = YouTubeRepository(ApiClient.service)

    private val _video = MutableStateFlow<YouTubeVideo?>(null)
    val video: StateFlow<YouTubeVideo?> = _video

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchVideo(videoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _video.value = repository.fetchMetadata(videoId)
                if (_video.value == null) _error.value = "Video not found"
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun reset() {
        _video.value = null
        _isLoading.value = false
        _error.value = null
    }
}