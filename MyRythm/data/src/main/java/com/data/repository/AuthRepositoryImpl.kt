package com.data.repository

import android.util.Log
import com.data.core.auth.AuthPreferencesDataSource
import com.data.core.auth.JwtUtils
import com.data.core.auth.TokenStore
import com.data.mapper.auth.asAuthTokens
import com.data.mapper.auth.toDomainTokens
import com.data.mapper.auth.toDto
import com.data.mapper.user.toDto
import com.data.network.api.UserApi
import com.data.network.dto.user.SendCodeRequest
import com.data.network.dto.user.UserLoginRequest
import com.data.network.dto.user.VerifyCodeRequest
import com.domain.model.ApiResult
import com.domain.model.AuthStatus
import com.domain.model.AuthTokens
import com.domain.model.DomainError
import com.domain.model.SignupRequest
import com.domain.model.SocialLoginParam
import com.domain.model.SocialLoginResult
import com.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val tokenStore: TokenStore,
    private val io: CoroutineDispatcher = Dispatchers.IO,
    private val prefs: AuthPreferencesDataSource
) : AuthRepository {

    override suspend fun sendEmailCode(
        email: String,
        name: String?
    ): ApiResult<Unit> = withContext(io) {
        runCatching {
            val res = api.sendEmailCode(SendCodeRequest(email, name))
            if (!res.isSuccessful) {
                return@withContext ApiResult.Failure(
                    DomainError.Server(res.code(), res.errorBody()?.string())
                )
            }
            ApiResult.Success(Unit)
        }.getOrElse {
            ApiResult.Failure(DomainError.Network(it.message))
        }
    }

    override suspend fun verifyEmailCode(
        email: String,
        code: String
    ): ApiResult<Unit> = withContext(io) {
        runCatching {
            val res = api.verifyEmailCode(VerifyCodeRequest(email, code))
            if (!res.isSuccessful) {
                return@withContext ApiResult.Failure(
                    DomainError.Server(res.code(), res.errorBody()?.string())
                )
            }
            ApiResult.Success(Unit)
        }.getOrElse {
            ApiResult.Failure(DomainError.Network(it.message))
        }
    }

    override suspend fun login(
        id: String,
        pw: String,
        autoLogin: Boolean
    ): ApiResult<AuthTokens> = withContext(io) {
        runCatching {
            val res = api.login(UserLoginRequest(id, pw))
            if (!res.isSuccessful) {
                return@withContext ApiResult.Failure(
                    DomainError.Server(res.code(), res.errorBody()?.string())
                )
            }

            val body = res.body()
                ?: return@withContext ApiResult.Failure(DomainError.Unknown("empty body"))

            val tokens = body.asAuthTokens()
            tokenStore.set(tokens.access, tokens.refresh, persist = autoLogin)
            prefs.setAutoLoginEnabled(autoLogin)

            ApiResult.Success(tokens)
        }.getOrElse {
            ApiResult.Failure(DomainError.Network(it.message))
        }
    }

    override suspend fun socialLogin(
        param: SocialLoginParam
    ): ApiResult<SocialLoginResult> = withContext(io) {
        runCatching {
            val res = api.socialLogin(param.toDto())

            if (!res.isSuccessful) {
                return@withContext when (res.code()) {
                    409, 428 -> ApiResult.Failure(DomainError.NeedAdditionalInfo)
                    else -> ApiResult.Failure(
                        DomainError.Server(res.code(), res.errorBody()?.string())
                    )
                }
            }

            val body = res.body()
                ?: return@withContext ApiResult.Failure(DomainError.Unknown("empty body"))

            if (body.needAdditionalInfo == true) {
                return@withContext ApiResult.Failure(DomainError.NeedAdditionalInfo)
            }

            val tokens = body.toDomainTokens()
            val access = tokens.access
                ?: return@withContext ApiResult.Failure(DomainError.InvalidToken("access missing"))

            val autoLoginEnabled = prefs.isAutoLoginEnabled()
            tokenStore.set(tokens.access, tokens.refresh, persist = autoLoginEnabled)

            val userId = JwtUtils.extractUserId(access)?.toLong()
                ?: return@withContext ApiResult.Failure(
                    DomainError.InvalidToken("invalid jwt")
                )

            ApiResult.Success(
                SocialLoginResult.Success(userId, tokens)
            )
        }.getOrElse {
            ApiResult.Failure(DomainError.Network(it.message))
        }
    }

    override suspend fun signup(request: SignupRequest): ApiResult<Unit> =
        withContext(io) {
            runCatching {
                val res = api.signup(request.toDto())
                if (!res.isSuccessful) {
                    ApiResult.Failure(
                        DomainError.Server(res.code(), res.errorBody()?.string())
                    )
                } else ApiResult.Success(Unit)
            }.getOrElse {
                ApiResult.Failure(DomainError.Network(it.message))
            }
        }

    override suspend fun resetPassword(
        email: String,
        newPassword: String
    ): ApiResult<Unit> = withContext(io) {
        runCatching {
            val res = api.resetPassword(mapOf("email" to email, "password" to newPassword))
            if (!res.isSuccessful) {
                ApiResult.Failure(
                    DomainError.Server(res.code(), res.errorBody()?.string())
                )
            } else ApiResult.Success(Unit)
        }.getOrElse {
            ApiResult.Failure(DomainError.Network(it.message))
        }
    }

    override suspend fun logout(): ApiResult<Unit> = withContext(io) {
        runCatching {
            val res = api.logout()

            if(!res.isSuccessful) {
                return@withContext ApiResult.Failure(
                    DomainError.Server(res.code(), res.errorBody()?.string())
                )
            }

            tokenStore.clear()
            ApiResult.Success(Unit)
        }.getOrElse {
            tokenStore.clear()
            ApiResult.Failure(DomainError.Network(it.message))
        }
    }

    override suspend fun withdrawal(): ApiResult<Unit> = withContext(io) {
        runCatching {
            val res = api.deleteAccount()
            if (!res.isSuccessful) {
                ApiResult.Failure(
                    DomainError.Server(res.code(), res.errorBody()?.string())
                )
            } else {
                tokenStore.clear()
                ApiResult.Success(Unit)
            }
        }.getOrElse {
            ApiResult.Failure(DomainError.Network(it.message))
        }
    }

    override suspend fun clearTokens(): ApiResult<Unit> =
        withContext(io) {
            tokenStore.clear()
            ApiResult.Success(Unit)
        }

    override suspend fun saveAutoLoginEnabled(enabled: Boolean): ApiResult<Unit> =
        run {
            prefs.setAutoLoginEnabled(enabled)
            ApiResult.Success(Unit)
        }

    override suspend fun checkEmailExists(email: String): ApiResult<Boolean> =
        withContext(io) {
            runCatching {
                ApiResult.Success(
                    api.checkEmailDuplicate(mapOf("email" to email)).exists
                )
            }.getOrElse {
                ApiResult.Failure(DomainError.Network(it.message))
            }
        }

    override fun getRawAccessToken(): String? =
        tokenStore.current().access

    override suspend fun tryRefreshFromLocal(): ApiResult<Boolean> {
        return ApiResult.Failure(
            DomainError.Unknown("tryRefreshFromLocal not implemented yet")
        )
    }

    override fun getUserId(): Long? {
        val access = tokenStore.current().access ?: return null
        return JwtUtils.extractUserId(access)?.toLong()
    }

    override suspend fun getAuthStatus(): AuthStatus {
        val autoLogin = prefs.isAutoLoginEnabled()
        val token = tokenStore.current().access

        if (token.isNullOrBlank()) {
            return AuthStatus(false, null, autoLogin)
        }

        val userId = JwtUtils.extractUserId(token)
        if (userId == null) {
            tokenStore.clear()
            return AuthStatus(false, null, autoLogin)
        }

        return AuthStatus(true, userId, autoLogin)
    }


}