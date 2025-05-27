package com.tp.tpa

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView

@UnstableApi
@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    videoPlayerViewModel: VideoPlayerViewModel,
    mediaUrl: String,
    licenseUrl: String,
    startPosition: Long? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleState = LocalLifecycleOwner.current.lifecycle.currentStateAsState()

    val exoPlayer = videoPlayerViewModel.exoPlayer
    val trackSelector = videoPlayerViewModel.trackSelector
    val qualityFormats = videoPlayerViewModel.qualityFormats

    val qualityLabels by remember(qualityFormats.size) {
        derivedStateOf { listOf(context.getString(R.string.video_quality_auto)) + qualityFormats.map { "${it.height}p" } }
    }
    var qualityMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isFullscreen by rememberSaveable { mutableStateOf(false) }
    var showControls by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(mediaUrl, licenseUrl) {
        videoPlayerViewModel.initialize(
            mediaUrl,
            licenseUrl,
            maxOf(startPosition ?: 0, exoPlayer.currentPosition)
        )
    }

    LaunchedEffect(lifecycleState.value) {
        exoPlayer.playWhenReady = lifecycleState.value == Lifecycle.State.RESUMED
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Box(contentAlignment = Alignment.Center) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    PlayerView(context).apply {
                        player = exoPlayer
                        showControls = isControllerFullyVisible
                        setControllerVisibilityListener(object :
                            PlayerView.ControllerVisibilityListener {
                            override fun onVisibilityChanged(visibility: Int) {
                                showControls = visibility == View.VISIBLE
                            }
                        })
                    }
                }
            )

            if (videoPlayerViewModel.isBuffering.value) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            }
        }

        if (showControls)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(end = 48.dp),
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
                            imageVector = if (qualityMenuExpanded) {
                                Icons.Filled.KeyboardArrowUp
                            } else {
                                Icons.Filled.KeyboardArrowDown
                            },
                            contentDescription = "Quality Dropdown",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = qualityMenuExpanded,
                        onDismissRequest = { qualityMenuExpanded = false }
                    ) {
                        qualityLabels.forEachIndexed { idx, label ->
                            val isSelected = videoPlayerViewModel.selectedQuality.intValue == idx
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = label,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                },
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
                                    videoPlayerViewModel.selectedQuality.intValue = idx
                                },
                                modifier = Modifier.background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.1f
                                    ) else Color.Transparent
                                )
                            )
                        }
                    }
                }

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
                                R.drawable.ic_fullscreen_exit
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
