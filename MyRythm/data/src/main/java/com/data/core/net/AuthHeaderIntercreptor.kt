package com.data.core.net

import com.data.core.auth.TokenStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthHeaderInterceptor(
    private val tokenStore: TokenStore,
    private val scheme: String = "Bearer"
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val access = tokenStore.current().access
        val newReq = if (!access.isNullOrBlank()) {
            req.newBuilder()
                .header("Authorization", "$scheme $access")
                .build()
        } else req
        return chain.proceed(newReq)
    }
}
