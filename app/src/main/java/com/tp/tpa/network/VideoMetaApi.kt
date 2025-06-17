package com.tp.tpa.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tp.tpa.data.PrimeVideoMetadataResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

object ApiClient {
    private const val BASE_URL = "https://www.primevideo.com/"
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    val service: VideMetaDataApiService = retrofit.create(VideMetaDataApiService::class.java)
}

interface VideMetaDataApiService {
    @GET("detail/{videoId}")
    @Headers("Accept: application/json", "x-requested-with: WebSPA")
    suspend fun getVideoById(
        @Path("videoId") id: String,
        @Query("dvWebSPAClientVersion") part: String = "1.0.107238.0",
    ): Response<PrimeVideoMetadataResponse>
}