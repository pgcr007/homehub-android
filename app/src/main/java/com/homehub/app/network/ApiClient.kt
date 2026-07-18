package com.homehub.app.network

import com.homehub.app.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Holds the bearer token in memory for the session.
 * Phase 2+ will back this with EncryptedSharedPreferences for persistence across app restarts.
 */
object TokenHolder {
    @Volatile
    var token: String? = null
}

private val authInterceptor = Interceptor { chain ->
    val original = chain.request()
    val token = TokenHolder.token
    val request = if (token != null) {
        original.newBuilder().addHeader("Authorization", "Bearer $token").build()
    } else {
        original
    }
    chain.proceed(request)
}

private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.BODY
    } else {
        HttpLoggingInterceptor.Level.NONE
    }
}

private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(authInterceptor)
    .addInterceptor(loggingInterceptor)
    .build()

object ApiClient {
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthService by lazy { retrofit.create(AuthService::class.java) }
    val deviceService: DeviceService by lazy { retrofit.create(DeviceService::class.java) }

}