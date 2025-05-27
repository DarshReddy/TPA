package com.tp.tpa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tp.tpa.data.YouTubeVideo

@Composable
fun VideoMetadataScreen(
    modifier: Modifier = Modifier,
    viewModel: VideoMetaViewModel,
) {
    var videoId by rememberSaveable { mutableStateOf("") }
    val video by viewModel.video.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = videoId,
            onValueChange = { videoId = it },
            label = { Text("YouTube Video ID") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { viewModel.fetchVideo(videoId) },
            enabled = videoId.isNotBlank(),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Fetch Metadata")
        }
        Spacer(modifier = Modifier.height(16.dp))
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            error != null -> Text("Error: $error", color = MaterialTheme.colorScheme.error)
            video != null -> video?.let { VideoDetails(it) }
        }
    }
}

@Composable
fun VideoDetails(video: YouTubeVideo) {
    Text("Title: ${video.snippet.title}", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(4.dp))
    Text("Channel: ${video.snippet.channelTitle}")
    Spacer(modifier = Modifier.height(4.dp))
    Text("Published: ${video.snippet.publishedDateTime}")
    Spacer(modifier = Modifier.height(4.dp))
    Text("Duration: ${video.contentDetails.duration}")
    Spacer(modifier = Modifier.height(4.dp))
    Text("Views: ${video.statistics.viewCount}")
    Spacer(modifier = Modifier.height(4.dp))
    video.statistics.likeCount?.let { Text("Likes: $it") }
}
