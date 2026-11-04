package com.citywalls.app.data.remote

import com.citywalls.app.data.remote.dto.CommonsSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Wikimedia Commons action API. Shared parameters (action/format/prop/iiprop)
 * are added by [CommonsParamsInterceptor] so every call stays consistent.
 */
interface CommonsApi {

    @GET("w/api.php")
    suspend fun searchImages(
        @Query("gsrsearch") search: String,
        @Query("gsrnamespace") namespace: Int,
        @Query("gsrlimit") limit: Int,
        @Query("gsroffset") offset: Int,
        @Query("gsrsort") sort: String,
        @Query("iiurlwidth") thumbWidth: Int
    ): CommonsSearchResponse
}
