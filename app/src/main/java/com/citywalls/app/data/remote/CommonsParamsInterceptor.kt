package com.citywalls.app.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class CommonsParamsInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (!original.url.encodedPath.endsWith("api.php")) {
            return chain.proceed(original)
        }
        val url = original.url.newBuilder()
            .addQueryParameter("action", "query")
            .addQueryParameter("format", "json")
            .addQueryParameter("formatversion", "2")
            .addQueryParameter("prop", "imageinfo|categories")
            .addQueryParameter("iiprop", "url|size|mime|extmetadata")
            .addQueryParameter("cllimit", "30")
            .addQueryParameter("clshow", "!hidden")
            .build()
        return chain.proceed(original.newBuilder().url(url).build())
    }
}
