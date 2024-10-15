package com.bpdevop.mediccontrol.data.api

import com.bpdevop.mediccontrol.data.api.responses.TokenResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthService {
    @FormUrlEncoded
    @POST("connect/token")
    suspend fun getToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("scope") scope: String = "icdapi_access",
    ): TokenResponse
}