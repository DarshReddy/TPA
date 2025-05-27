package com.tp.tpa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import com.tp.tpa.ui.theme.TPATheme

class MainActivity : ComponentActivity() {

    val viewModel by viewModels<VideoPlayerViewModel>()

    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TPATheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VideoPlayer(
                        modifier = Modifier.padding(innerPadding),
                        videoPlayerViewModel = viewModel,
                        mediaUrl = "https://bitmovin-a.akamaihd.net/content/art-of-motion_drm/mpds/11331.mpd",
                        licenseUrl = "https://cwip-shaka-proxy.appspot.com/no_auth",
                    )
                }
            }
        }
    }
}
