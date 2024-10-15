package com.bpdevop.mediccontrol.data.api

import com.bpdevop.mediccontrol.data.api.responses.IcdSearchResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface IcdService {
    @GET("icd/release/11/2024-01/mms/search")
    suspend fun searchDiseases(
        @Header("Authorization") authHeader: String,
        @Query("q") query: String,
        @Header("Accept-Language") language: String = "es",
        @Query("chapterFilter") chapterFilter: String = "01",
        @Header("API-Version") apiVersion: String = "v2"
    ): IcdSearchResponse
}
