package com.homehub.app.network

import com.homehub.app.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Holds the bearer token in memory for the current process. Backed by
 * SessionStore (EncryptedSharedPreferences) as of post-Phase 7 — restored
 * into this holder at app startup by SessionStore.restore() so a killed/
 * restarted process doesn't force a re-login, but every request still
 * reads the token from here rather than from disk directly.
 */
object TokenHolder {
    @Volatile
    var token: String? = null
}

/**
 * Phase 6: every household-scoped endpoint (rooms/devices/events/rules)
 * requires an X-Household-Id header — this is the Android-side equivalent
 * of TokenHolder above. Set via bootstrapActiveHousehold() right after
 * login, and updated again by the household switcher (Step 4) when the
 * user picks a different household. Persisted the same way as TokenHolder
 * (see SessionStore) so the active household survives a process restart
 * too, not just the token.
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

/**
 * The signed-in user's own id. Needed so member-management UI can tell
 * "this row is me" apart from other members (e.g. to hide the remove
 * button on your own row) without a dedicated /whoami round trip.
 */
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
    // Render's free tier spins the backend down after inactivity; the first
    // request after a cold spell can take 30-50s to get a response while it
    // wakes up. OkHttp's default 10s read timeout was firing a
    // SocketTimeoutException on exactly that first login attempt, which
    // surfaced to the user as an opaque "timeout" message. 60s comfortably
    // covers a cold start without leaving a genuinely-dead connection
    // hanging for a full minute.
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
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