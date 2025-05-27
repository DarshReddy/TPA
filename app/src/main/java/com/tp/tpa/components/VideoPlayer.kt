package com.tp.tpa.components

import android.app.Activity
import android.content.pm.ActivityInfo
import android.media.metrics.EditingEndedEvent.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
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
    val lifecycleOwner = LocalLifecycleOwner.current

    val trackSelector = remember {
        DefaultTrackSelector(context).apply {
            setParameters(buildUponParameters())
        }
    }

    val qualityFormats = remember { mutableStateListOf<Format>() }
    val qualityLabels by remember(qualityFormats.size) {
        derivedStateOf { listOf(context.getString(R.string.video_quality_auto)) + qualityFormats.map { "${it.height}p" } }
    }

    var qualityMenuExpanded by remember { mutableStateOf(false) }
    var isFullscreen by rememberSaveable { mutableStateOf(false) }

    // Build ExoPlayer with track selector
    val exoPlayer = remember(context, mediaUrl) {
        ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build().apply {
                repeatMode = Player.REPEAT_MODE_OFF
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        isBuffering.value =
                            (state == Player.STATE_BUFFERING || state == Player.STATE_IDLE)
                        if (state == Player.STATE_ENDED) {
                            onComplete?.invoke()
                        } else if (state == Player.STATE_READY) {
                            onPlaying?.invoke()
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("VideoPlayer", "Playback failed: ${error.errorCodeName}")
                        if (error.errorCode == ERROR_CODE_IO_NETWORK_CONNECTION_FAILED) {
                            prepare()
                        }
                    }

                    override fun onTracksChanged(tracks: Tracks) {
                        val videoFormats = tracks.groups
                            .filter { it.type == C.TRACK_TYPE_VIDEO }
                            .flatMap { group ->
                                (0 until group.mediaTrackGroup.length)
                                    .map { idx -> group.mediaTrackGroup.getFormat(idx) }
                                    .filterIndexed { idx, _ -> group.isTrackSupported(idx) }
                            }
                        val distinct = videoFormats
                            .distinctBy { it.height }
                            .sortedBy { it.height }
                        qualityFormats.clear()
                        qualityFormats.addAll(distinct)
                    }
                })

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

    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Box(contentAlignment = Alignment.Center) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f),
                factory = {
                    PlayerView(context).apply { player = exoPlayer }
                }
            )

            if (isBuffering.value) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .height(36.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.quality),
                color = Color.White,
            )
            Box {
                IconButton(
                    onClick = { qualityMenuExpanded = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Quality Dropdown",
                        tint = Color.White
                    )
                }
                DropdownMenu(
                    expanded = qualityMenuExpanded,
                    onDismissRequest = { qualityMenuExpanded = false }
                ) {
                    qualityLabels.forEachIndexed { idx, label ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                qualityMenuExpanded = false
                                val params = trackSelector.buildUponParameters()
                                if (label == context.getString(R.string.video_quality_auto)) {
                                    params.clearVideoSizeConstraints()
                                } else {
                                    val fmt = qualityFormats[idx - 1]
                                    params.setMaxVideoSize(fmt.width, fmt.height)
                                }
                                trackSelector.parameters = params.build()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(onClick = {
                activity?.requestedOrientation = if (!isFullscreen)
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                else
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                isFullscreen = !isFullscreen
            }) {
                Icon(
                    painter = painterResource(
                        if (isFullscreen) {
                            R.drawable.ic_fullscreen
                        } else {
                            R.drawable.ic_fullscreen
                        }
                    ),
                    contentDescription = "Toggle Fullscreen",
                    tint = Color.White
                )
            }
        }
    }
}
