package com.tp.tpa.data

data class VideoMetadata(
    val title: String?,
    val synopsis: String?,
    val runTime: String?, // seconds
    val genres: List<String>?,
    val audioTracks: List<String>?,
    val people: People?,
    val releaseData: String?,
    val image: String?
)

data class People(
    val directors: List<String>?,
    val actors: List<String>?
)