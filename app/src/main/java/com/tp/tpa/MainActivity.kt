package com.tp.tpa

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import com.tp.tpa.ui.theme.TPATheme

class MainActivity : ComponentActivity() {

    val videoPlayerViewModel by viewModels<VideoPlayerViewModel>()
    val videoMetaViewModel by viewModels<VideoMetaViewModel>()

    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var selectedScreen by rememberSaveable { mutableIntStateOf(0) }
            TPATheme {
                BackHandler {
                    if (selectedScreen == 0) {
                        finish()
                    } else {
                        selectedScreen = 0
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        videoPlayerViewModel.exoPlayer.pause()
                        videoMetaViewModel.reset()
                    }
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (selectedScreen) {
                        0 -> Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Button(onClick = { selectedScreen = 1 }) { Text("Video Player") }
                            Button(onClick = { selectedScreen = 2 }) { Text("Video Metadata") }
                        }

                        1 -> VideoPlayer(
                            modifier = Modifier.padding(innerPadding),
                            videoPlayerViewModel = videoPlayerViewModel,
                            mediaUrl = "https://bitmovin-a.akamaihd.net/content/art-of-motion_drm/mpds/11331.mpd",
                            licenseUrl = "https://cwip-shaka-proxy.appspot.com/no_auth",
                        )

                        2 -> VideoMetadataScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = videoMetaViewModel
                        )
                    }
                }
            }
        }
    }
}
