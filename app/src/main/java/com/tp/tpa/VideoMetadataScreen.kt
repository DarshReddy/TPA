package com.tp.tpa

import android.annotation.SuppressLint
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.tp.tpa.data.People
import com.tp.tpa.data.VideoMetadata
import com.tp.tpa.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun VideoMetadataScreen(
    modifier: Modifier = Modifier,
    viewModel: VideoMetaViewModel,
) {
    var videoId by rememberSaveable { mutableStateOf("") }
    val videoMetadata by viewModel.videoMetadata.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = videoId,
            onValueChange = { videoId = it },
            label = { Text("Prime Video ID") },
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
        when (videoMetadata) {
            is ApiResult.Success -> VideoDetails((videoMetadata as ApiResult.Success<VideoMetadata>).data)
            is ApiResult.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is ApiResult.Error -> Text(
                "Error: ${(videoMetadata as ApiResult.Error).message}",
                color = MaterialTheme.colorScheme.error
            )

            null -> {}
        }
    }
}

@Composable
fun VideoDetails(videoMetadata: VideoMetadata) {
    with(videoMetadata) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AsyncImage(
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth(),
                model = image,
                placeholder = painterResource(R.drawable.ic_launcher_monochrome),
                contentDescription = stringResource(R.string.video_image)
            )
            DetailRow(label = "Title", value = title)
            DetailRow(label = "Synopsis", value = synopsis)
            DetailRow(label = "Runtime", value = runTime)
            DetailRow(label = "Release Date", value = releaseData)
            DetailRow(label = "Genres", value = genres?.joinToString(", "))
            DetailRow(label = "Audio Tracks", value = audioTracks?.joinToString(", "))
            DetailRow(
                label = "Directors",
                value = people?.directors?.joinToString(", ")
            )
            DetailRow(label = "Actors", value = people?.actors?.joinToString(", "))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@SuppressLint("StateFlowValueCalledInComposition")
@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun VideoMetadataScreenPreview() {
    val viewModel = VideoMetaViewModel()
    // Simulate some state for preview
    (viewModel.videoMetadata as MutableStateFlow).value = ApiResult.Success(
        VideoMetadata(
            title = "Sample Title",
            synopsis = "Sample Synopsis",
            genres = listOf("Action", "Adventure"),
            audioTracks = listOf("English", "Spanish"),
            people = People(
                directors = listOf("Director 1", "Director 2"),
                actors = listOf("Actor 1", "Actor 2")
            ),
            runTime = "2h 41m",
            releaseData = "May 20, 2023",
            image = "https://example.com/image.jpg"
        )
    )
    VideoMetadataScreen(viewModel = viewModel)
}
