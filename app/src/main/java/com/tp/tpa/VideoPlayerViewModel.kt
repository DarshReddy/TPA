package com.tp.tpa

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector

@UnstableApi
class VideoPlayerViewModel(application: Application) : AndroidViewModel(application) {
    val trackSelector = DefaultTrackSelector(getApplication())
    var isBuffering = mutableStateOf(true)
    val qualityFormats = mutableStateListOf<Format>()


    val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(getApplication())
            .setTrackSelector(trackSelector)
            .build().apply {
                repeatMode = Player.REPEAT_MODE_OFF
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        isBuffering.value =
                            (state == Player.STATE_BUFFERING || state == Player.STATE_IDLE)
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("VideoPlayerVM", "Playback error: ${error.errorCodeName}")
                    }

                    override fun onTracksChanged(tracks: Tracks) {
                        val formats = tracks.groups
                            .filter { it.type == C.TRACK_TYPE_VIDEO }
                            .flatMap { group ->
                                (0 until group.mediaTrackGroup.length).map { idx ->
                                    group.mediaTrackGroup.getFormat(idx)
                                }.filterIndexed { idx, _ -> group.isTrackSupported(idx) }
                            }
                        val distinct = formats.distinctBy { it.height }.sortedBy { it.height }
                        qualityFormats.clear()
                        qualityFormats.addAll(distinct)
                    }
                })
            }
    }

    fun initialize(mediaUrl: String, licenseUrl: String, startPosition: Long?) {

        val drmConfig = MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
            .setLicenseUri(licenseUrl)
            .build()
        val mediaItem = MediaItem.Builder()
            .setUri(mediaUrl)
            .setDrmConfiguration(drmConfig)
            .build()
        if (exoPlayer.currentMediaItem?.mediaId != mediaItem.mediaId) {
            exoPlayer.setMediaItem(mediaItem)
            startPosition?.let { exoPlayer.seekTo(it) }
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    override fun onCleared() {
        exoPlayer.release()
        super.onCleared()
    }
}