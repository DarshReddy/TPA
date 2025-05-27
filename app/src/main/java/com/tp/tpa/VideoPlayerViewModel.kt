package com.tp.tpa

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector

@UnstableApi
class VideoPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val trackSelector = DefaultTrackSelector(getApplication())

    val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(getApplication())
            .setTrackSelector(trackSelector)
            .build().apply {
                repeatMode = Player.REPEAT_MODE_OFF
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("VideoPlayerVM", "Playback error: ${error.errorCodeName}")
                    }
                })
            }
    }

    fun initialize(mediaUrl: String, licenseUrl: String, startPosition: Long?) {
        exoPlayer.stop()
        val drmConfig = MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
            .setLicenseUri(licenseUrl)
            .build()
        val mediaItem = MediaItem.Builder()
            .setUri(mediaUrl)
            .setDrmConfiguration(drmConfig)
            .build()

        exoPlayer.setMediaItem(mediaItem)
        startPosition?.let { exoPlayer.seekTo(it) }
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    override fun onCleared() {
        exoPlayer.release()
        super.onCleared()
    }
}