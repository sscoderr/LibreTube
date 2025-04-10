package com.bimilyoncu.sscoderr.libretubess.api

import com.bimilyoncu.sscoderr.libretubess.BuildConfig
import com.bimilyoncu.sscoderr.libretubess.constants.PreferenceKeys
import com.bimilyoncu.sscoderr.libretubess.helpers.PreferenceHelper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

object RetrofitInstance {
    const val PIPED_API_URL = "https://pipedapi.kavin.rocks"

    val authUrl
        get() = if (
            PreferenceHelper.getBoolean(
                PreferenceKeys.AUTH_INSTANCE_TOGGLE,
                false
            )
        ) {
           PreferenceHelper.getString(
                PreferenceKeys.AUTH_INSTANCE,
                PIPED_API_URL
            )
        } else {
            PipedMediaServiceRepository.apiUrl
        }

    val apiLazyMgr = resettableManager()
    val kotlinxConverterFactory = JsonHelper.json
        .asConverterFactory("application/json".toMediaType())

    val httpClient by lazy { buildClient() }

    val authApi = buildRetrofitInstance<PipedAuthApi>(authUrl)

    // the url provided here isn't actually used anywhere in the external api
    val externalApi = buildRetrofitInstance<ExternalApi>(PIPED_API_URL)

    private fun buildClient(): OkHttpClient {
        val httpClient = OkHttpClient().newBuilder()

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            httpClient.addInterceptor(loggingInterceptor)
        }

        return httpClient.build()
    }

    inline fun <reified T: Any> buildRetrofitInstance(apiUrl: String): T = Retrofit.Builder()
        .baseUrl(apiUrl)
        .client(httpClient)
        .addConverterFactory(kotlinxConverterFactory)
        .build()
        .create<T>()
}
