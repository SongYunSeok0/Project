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
        val path = req.url.encodedPath

        // 인증이 필요 없는 엔드포인트는 토큰 제외
        if (path.contains("/users/social-login/") ||
            path.contains("/users/signup/") ||
            path.contains("/token/") ||
            path.contains("/api/auth/")) {
            return chain.proceed(req)
        }

        // 나머지 API는 토큰 추가
        val access = tokenStore.current().access
        val newReq = if (!access.isNullOrBlank()) {
            req.newBuilder()
                .header("Authorization", "$scheme $access")
                .build()
        } else req

        return chain.proceed(newReq)
    }
}