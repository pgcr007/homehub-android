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

/**
 * Phase 6: every household-scoped endpoint (rooms/devices/events/rules)
 * requires an X-Household-Id header — this is the Android-side equivalent
 * of TokenHolder above. Set via bootstrapActiveHousehold() right after
 * login (see HouseholdBootstrap.kt). In-memory only for now, same
 * persistence caveat as TokenHolder.
 */
object HouseholdHolder {
    @Volatile
    var activeHouseholdId: String? = null

    @Volatile
    var activeHouseholdName: String? = null

    // "owner" | "manager" | "member" for the signed-in user in the active
    // household — drives role-gated UI (e.g. hiding the add/remove-member
    // controls for a plain member). This is a UI convenience only; the
    // backend re-checks the role on every request regardless.
    @Volatile
    var activeHouseholdRole: String? = null
}

object UserHolder {
    @Volatile
    var userId: String? = null
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

private val householdInterceptor = Interceptor { chain ->
    val original = chain.request()
    val householdId = HouseholdHolder.activeHouseholdId
    val request = if (householdId != null) {
        original.newBuilder().addHeader("X-Household-Id", householdId).build()
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
    .addInterceptor(householdInterceptor)
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
    val householdService: HouseholdService by lazy { retrofit.create(HouseholdService::class.java) }
}