package com.bpdevop.mediccontrol.di

import com.bpdevop.mediccontrol.core.network.qualifiers.RetrofitAuth
import com.bpdevop.mediccontrol.core.network.qualifiers.RetrofitICD
import com.bpdevop.mediccontrol.data.api.AuthService
import com.bpdevop.mediccontrol.data.api.IcdService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val AUTH_BASE_URL = "https://icdaccessmanagement.who.int/"
    private const val ICD_BASE_URL = "https://id.who.int/"

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @RetrofitAuth
    fun provideRetrofitAuth(client: OkHttpClient, gson: Gson): Retrofit = Retrofit.Builder()
        .baseUrl(AUTH_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun provideAuthService(@RetrofitAuth retrofit: Retrofit): AuthService = retrofit.create(AuthService::class.java)

    @Provides
    @Singleton
    @RetrofitICD
    fun provideRetrofitICD(client: OkHttpClient, gson: Gson): Retrofit = Retrofit.Builder()
        .baseUrl(ICD_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun provideIcdService(@RetrofitICD retrofit: Retrofit): IcdService = retrofit.create(IcdService::class.java)
}
