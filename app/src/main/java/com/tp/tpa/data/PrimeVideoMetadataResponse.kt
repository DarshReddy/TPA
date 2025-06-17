package com.tp.tpa.data

data class PrimeVideoMetadataResponse(
    val page: List<Page>
)

data class Page(
    val assembly: Assembly
)

data class Assembly(
    val body: List<Body>
)

data class Body(
    val props: Props
)

data class Props(
    val atf: Atf
)

data class Atf(
    val state: State
)

data class State(
    val detail: Detail
)

data class Detail(
    val headerDetail: Map<String, MovieDetail>
)

data class MovieDetail(
    val title: String?,
    val synopsis: String?,
    val duration: Int?, // Might need parsing from PT format
    val genres: List<Genre>?,
    val contributors: Map<String, List<Contributor>>?,
    val audioTracks: List<String>?,
    val releaseDate: String?,
    val runtime: String?,
    val images: Map<String, String>?,
)

data class Genre(
    val text: String
)

data class Contributor(
    val name: String
)