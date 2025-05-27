package com.tp.tpa.components

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

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
    val isBuffering = remember { mutableStateOf(true) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val videoPlayer = ExoPlayer.Builder(context).build()
    DisposableEffect(lifecycleOwner) {
        onDispose {
            onFinish?.invoke(
                (videoPlayer.currentPosition.toFloat() * PERCENTAGE / videoPlayer.duration.toFloat()).toInt(),
                videoPlayer.currentPosition
            )
        }
    }

    val drmConfig = MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
        .setLicenseUri(licenseUrl)
        .build()

    val mediaItem = MediaItem.Builder().setUri(mediaUrl).setDrmConfiguration(drmConfig).build()

    val exoPlayer = remember(context, mediaItem) {
        videoPlayer.apply {
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
            repeatMode = REPEAT_MODE_OFF
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    isBuffering.value =
                        (playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_IDLE)
                    if (playbackState == Player.STATE_ENDED) {
                        onComplete?.invoke()
                    } else if (playbackState == Player.STATE_READY) {
                        onPlaying?.invoke()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    Log.e("VideoPlayer", "Playback failed: ${error.errorCodeName}")
                    if (error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED) {
                        videoPlayer.prepare()
                    }
                }
            })
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    watchedDuration?.let { positionMs -> player?.seekTo(positionMs) }
                }
            }
        )
        if (isBuffering.value) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp),
            )
        }
    }
}