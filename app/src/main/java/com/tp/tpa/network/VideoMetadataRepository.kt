package com.tp.tpa.network

import com.tp.tpa.data.People
import com.tp.tpa.data.VideoMetadata
import kotlinx.coroutines.flow.flow

class VideoMetadataRepository(private val api: VideMetaDataApiService) {
    fun fetchMetadata(videoId: String) =
        flow {
            emit(ApiResult.Loading)
            val response = api.getVideoById(id = videoId)
            if (response.isSuccessful) {
                val root = response.body()
                val detail =
                    root?.page[0]?.assembly?.body[0]?.props?.atf?.state?.detail?.headerDetail?.values?.firstOrNull()

                emit(
                    ApiResult.Success(
                        VideoMetadata(
                            title = detail?.title,
                            synopsis = detail?.synopsis,
                            genres = detail?.genres?.map { it.text },
                            audioTracks = detail?.audioTracks,
                            runTime = detail?.runtime,
                            releaseData = detail?.releaseDate,
                            image = detail?.images?.entries?.firstOrNull()?.value,
                            people = People(
                                directors = detail?.contributors?.get("directors")?.map { it.name },
                                actors = detail?.contributors?.get("cast")?.map { it.name }
                            )
                        )
                    )
                )
            } else {
                emit(ApiResult.Error(response.message()))
            }
        }
}