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
import com.data.util.mapError
import com.data.util.toDomainException
import com.domain.model.AuthTokens
import com.domain.model.SignupRequest
import com.domain.model.SocialLoginParam
import com.domain.model.SocialLoginResult
import com.domain.repository.AuthRepository
import com.domain.repository.ProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val tokenStore: TokenStore,
    private val io: CoroutineDispatcher = Dispatchers.IO,
    private val prefs: AuthPreferencesDataSource,
    private val profileRepository: ProfileRepository
) : AuthRepository {

    override suspend fun sendEmailCode(email: String, name: String?): Result<Unit> =
        withContext(io) {
            runCatching {
                val res = api.sendEmailCode(SendCodeRequest(email, name))

                if (!res.isSuccessful) {
                    throw HttpAuthException(res.code(), res.errorBody()?.string())
                }

                Unit
            }.mapError { it.toDomainException() }
        }

    override suspend fun verifyEmailCode(email: String, code: String): Result<Unit> =
        withContext(io) {
            runCatching {
                val res = api.verifyEmailCode(VerifyCodeRequest(email, code))

                if (!res.isSuccessful) {
                    throw HttpAuthException(res.code(), res.errorBody()?.string())
                }

                Unit
            }.mapError { it.toDomainException() }
        }

    override suspend fun login(id: String, pw: String, autoLogin: Boolean): Result<AuthTokens> =
        withContext(io) {
            runCatching {
                val res = api.login(UserLoginRequest(id, pw))

                if (!res.isSuccessful) {
                    throw HttpAuthException(res.code(), res.errorBody()?.string())
                }

                val body = res.body() ?: throw IOException("Empty login body")
                val tokens = body.asAuthTokens()
                tokenStore.set(tokens.access, tokens.refresh)

                prefs.setAutoLoginEnabled(autoLogin)
                tokens
            }.mapError { it.toDomainException() }
        }

    override suspend fun saveAutoLoginEnabled(enabled: Boolean) =
        prefs.setAutoLoginEnabled(enabled)

    override suspend fun isAutoLoginEnabled(): Boolean =
        prefs.isAutoLoginEnabled()

    override suspend fun socialLogin(param: SocialLoginParam): Result<SocialLoginResult> =
        withContext(io) {
            runCatching {
                Log.d("SocialLogin", "Before clear: ${tokenStore.current().access}")
                val res = api.socialLogin(param.toDto())

                if (!res.isSuccessful) {
                    return@runCatching when (res.code()) {
                        409, 428 -> SocialLoginResult.NeedAdditionalInfo(
                            socialId = param.socialId,
                            provider = param.provider
                        )
                        else -> SocialLoginResult.Error(res.errorBody()?.string())
                    }
                }

                val body = res.body()
                    ?: return@runCatching SocialLoginResult.Error("empty body")

                if (body.needAdditionalInfo == true) {
                    return@runCatching SocialLoginResult.NeedAdditionalInfo(
                        socialId = param.socialId,
                        provider = param.provider
                    )
                }

                val hasTokens = !body.access.isNullOrBlank() && !body.refresh.isNullOrBlank()
                if (!hasTokens) {
                    return@runCatching SocialLoginResult.Error("invalid token data")
                }

                val tokens = body.toDomainTokens()
                tokenStore.set(tokens.access, tokens.refresh)

                val access = tokens.access
                    ?: throw IllegalStateException("No access token stored!")

                val idStr = JwtUtils.extractUserId(access)
                    ?: throw IllegalStateException("User ID not found in JWT!")

                val userId = idStr.toLong()

                SocialLoginResult.Success(
                    userId = userId,
                    tokens = tokens
                )
            }.mapError { it.toDomainException() }
        }

    override suspend fun refresh(refreshToken: String): Result<AuthTokens> =
        withContext(io) {
            runCatching {
                throw UnsupportedOperationException("Refresh API not implemented")
            }.mapError { it.toDomainException() }
        }

    override suspend fun tryRefreshFromLocal(): Result<Boolean> =
        withContext(io) {
            runCatching {
                val refresh = tokenStore.current().refresh ?: return@runCatching false
                val tokens = refresh(refresh).getOrNull() ?: return@runCatching false
                tokenStore.set(tokens.access, tokens.refresh)
                true
            }.mapError { it.toDomainException() }
        }

    override suspend fun clearTokens(): Result<Unit> =
        withContext(io) {
            runCatching {
                Log.d("AuthRepo", "clearTokens 시작")
                tokenStore.clear()
                Log.d("AuthRepo", "토큰 삭제 완료")
                profileRepository.clearProfile()
                Log.d("AuthRepo", "프로필 삭제 완료")
                Unit
            }.mapError { it.toDomainException() }
        }

    override suspend fun logout(): Result<Unit> =
        withContext(io) {
            runCatching {
                Log.d("AuthRepo", "logout 시작")

                Log.d("AuthRepo", "토큰 삭제 시작")
                tokenStore.clear()
                Log.d("AuthRepo", "토큰 삭제 완료")

                Log.d("AuthRepo", "프로필 삭제 시작")
                profileRepository.clearProfile()
                Log.d("AuthRepo", "프로필 삭제 완료")

                Log.d("AuthRepo", "자동로그인 해제 시작")
                prefs.setAutoLoginEnabled(false)
                Log.d("AuthRepo", "자동로그인 해제 완료")

                Log.d("AuthRepo", "logout 완료")
                Unit
            }.mapError { it.toDomainException() }
        }

    override suspend fun signup(request: SignupRequest): Result<Unit> =
        withContext(io) {
            runCatching {
                val dto = request.toDto()
                Log.d("SIGNUP_DTO", "보내는 JSON = $dto")

                val res = api.signup(dto)

                Log.d("SIGNUP_RESPONSE", "code=${res.code()}")

                if (!res.isSuccessful) {
                    val errorBody = res.errorBody()?.string()
                    Log.e("Signup", "HTTP ${res.code()} ${res.message()}")
                    throw HttpAuthException(res.code(), errorBody)
                }

                Log.d("Signup", "회원가입 성공: ${res.body()}")
                Unit
            }.mapError { it.toDomainException() }
        }

    override suspend fun resetPassword(email: String, newPassword: String): Result<Unit> =
        withContext(io) {
            runCatching {
                val res = api.resetPassword(
                    mapOf(
                        "email" to email,
                        "password" to newPassword
                    )
                )

                if (!res.isSuccessful) {
                    throw HttpAuthException(res.code(), res.errorBody()?.string())
                }

                Unit
            }.mapError { it.toDomainException() }
        }

    override suspend fun withdrawal(): Result<Unit> =
        withContext(io) {
            runCatching {
                val response = api.deleteAccount()

                if (!response.isSuccessful) {
                    throw HttpAuthException(response.code(), response.errorBody()?.string())
                }

                tokenStore.clear()
                profileRepository.clearProfile()
                Unit
            }.mapError { it.toDomainException() }
        }

    override fun getUserId(): Long {
        val access = tokenStore.current().access
            ?: throw IllegalStateException("No access token stored!")

        val idStr = JwtUtils.extractUserId(access)
            ?: throw IllegalStateException("User ID not found in JWT!")

        return idStr.toLong()
    }

    override suspend fun checkEmailExists(email: String): Result<Boolean> =
        withContext(io) {
            runCatching {
                val response = api.checkEmailDuplicate(mapOf("email" to email))
                response.exists
            }.mapError {
                Log.e("AuthRepository", "이메일 중복 체크 실패: ${it.message}", it)
                it.toDomainException()
            }
        }
}

class HttpAuthException(val code: Int, message: String?) :
    IOException("HTTP $code: ${message ?: "unknown"}")