package com.tp.tpa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tp.tpa.data.VideoMetadata
import com.tp.tpa.network.ApiClient
import com.tp.tpa.network.ApiResult
import com.tp.tpa.network.VideoMetadataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VideoMetaViewModel : ViewModel() {
    private val repository = VideoMetadataRepository(ApiClient.service)
    private val _videoMetadata = MutableStateFlow<ApiResult<VideoMetadata>?>(null)
    val videoMetadata: StateFlow<ApiResult<VideoMetadata>?> = _videoMetadata

    fun fetchVideo(videoId: String) {
        viewModelScope.launch {
            repository.fetchMetadata(videoId).collect {
                _videoMetadata.value = it
            }
        }
    }
}