package com.tp.tpa.components

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import com.tp.tpa.R

private const val PERCENTAGE = 100f

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    mediaUrl: String,
    licenseUrl: String,
    watchedDuration: Long? = null,
    onComplete: (() -> Unit)? = null,
    onFinish: ((Int, Long) -> Unit)? = null,
    onPlaying: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isBuffering = remember { mutableStateOf(true) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val trackSelector = remember {
        DefaultTrackSelector(context).apply {
            setParameters(buildUponParameters().clearVideoSizeConstraints())
        }
    }

    val exoPlayer = remember(context, mediaUrl) {
        ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build().apply {
                repeatMode = Player.REPEAT_MODE_OFF
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        isBuffering.value =
                            (state == Player.STATE_BUFFERING || state == Player.STATE_IDLE)
                        if (state == Player.STATE_ENDED) onComplete?.invoke()
                        else if (state == Player.STATE_READY) onPlaying?.invoke()
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("VideoPlayer", "Playback failed: ${error.errorCodeName}")
                        if (error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED) prepare()
                    }
                })

                // DRM config
                val drmConfig = MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                    .setLicenseUri(licenseUrl)
                    .build()
                val mediaItem = MediaItem.Builder()
                    .setUri(mediaUrl)
                    .setDrmConfiguration(drmConfig)
                    .build()

                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
                watchedDuration?.let { seekTo(it) }
            }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            onFinish?.invoke(
                (exoPlayer.currentPosition.toFloat() * PERCENTAGE / exoPlayer.duration.toFloat()).toInt(),
                exoPlayer.currentPosition
            )
            exoPlayer.release()
        }
    }

    // UI state
    var qualityMenuExpanded by remember { mutableStateOf(false) }
    val qualityOptions = listOf("Auto", "720p", "1080p")
    var isFullscreen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isBuffering.value) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color(0x66000000))
                .height(36.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Text(
                    text = "Quality",
                    color = Color.White,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clickable { qualityMenuExpanded = true }
                )
                DropdownMenu(
                    expanded = qualityMenuExpanded,
                    onDismissRequest = { qualityMenuExpanded = false }
                ) {
                    qualityOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                qualityMenuExpanded = false
                                val paramsBuilder = trackSelector.buildUponParameters()
                                when (option) {
                                    "Auto" -> paramsBuilder.clearVideoSizeConstraints()
                                    "720p" -> paramsBuilder.setMaxVideoSize(1280, 720)
                                    "1080p" -> paramsBuilder.setMaxVideoSize(1920, 1080)
                                }
                                trackSelector.parameters = paramsBuilder.build()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(onClick = {
                isFullscreen = !isFullscreen
                activity?.requestedOrientation = if (isFullscreen)
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                else
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }) {
                Icon(
                    painter = painterResource(R.drawable.ic_fullscreen),
                    contentDescription = "Toggle Fullscreen",
                    tint = Color.White
                )
            }
        }
    }
}
