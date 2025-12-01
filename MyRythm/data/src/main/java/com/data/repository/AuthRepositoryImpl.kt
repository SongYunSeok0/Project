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
import com.domain.model.AuthTokens
import com.domain.model.SignupRequest
import com.domain.model.SocialLoginParam
import com.domain.model.SocialLoginResult
import com.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val tokenStore: TokenStore,
    private val io: CoroutineDispatcher = Dispatchers.IO,
    private val prefs: AuthPreferencesDataSource    //1127
) : AuthRepository {

    override suspend fun sendEmailCode(email: String): Boolean {
        val res = api.sendEmailCode(SendCodeRequest(email))
        return res.isSuccessful
    }

    override suspend fun verifyEmailCode(email: String, code: String): Boolean {
        val res = api.verifyEmailCode(VerifyCodeRequest(email, code))
        return res.isSuccessful
    }

// 1127 자동로그인 적용 - 일부 수정한 코드
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

                //1127 1줄추가 - prefs.setAutoLoginEnabled(autoLogin)
                prefs.setAutoLoginEnabled(autoLogin)
                tokens
            }
        }

    // 1127 자동로그인 관련 추가
    override suspend fun saveAutoLoginEnabled(enabled: Boolean) =
        prefs.setAutoLoginEnabled(enabled)
    override suspend fun isAutoLoginEnabled(): Boolean =
        prefs.isAutoLoginEnabled()

    override suspend fun socialLogin(param: SocialLoginParam): Result<SocialLoginResult> =
        withContext(io) {
            runCatching {
                val res = api.socialLogin(param.toDto())

                if (!res.isSuccessful) {
                    // 409 / 428 → 추가정보 필요
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

                // 서버에서 "추가정보 필요" 신호를 줬을 때
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
            }
        }

    override suspend fun refresh(refreshToken: String): Result<AuthTokens> =
        withContext(io) {
            runCatching {
                throw UnsupportedOperationException("Refresh API not implemented")
            }
        }

    override suspend fun tryRefreshFromLocal(): Result<Boolean> =
        withContext(io) {
            runCatching {
                val refresh = tokenStore.current().refresh ?: return@runCatching false
                val tokens = refresh(refresh).getOrNull() ?: return@runCatching false
                tokenStore.set(tokens.access, tokens.refresh)
                true
            }
        }

    override suspend fun clearTokens(): Result<Unit> =
        withContext(io) {
            runCatching { tokenStore.clear() }
        }

    /* // 1127 11:27 merge seok into yun 주석
    override suspend fun signup(request: SignupRequest): Boolean {
        return try {
            val res = api.signup(request.toDto())
            if (!res.isSuccessful) {
                Log.e(
                    "Signup",
                    "HTTP ${res.code()} ${res.message()}\n${res.errorBody()?.string()}"
                )
                false
            } else {
                Log.d("Signup", "회원가입 성공: ${res.body()}")
                true
            }
        } catch (e: Exception) {
            Log.e("Signup", "네트워크 예외", e)
            false
        }
    }*/
    override suspend fun signup(request: SignupRequest): Boolean {
        return try {

            // 🔥 서버로 보낼 실제 JSON(DTO) 확인
            val dto = request.toDto()
            Log.e("SIGNUP_DTO", "보내는 JSON = $dto")

            val res = api.signup(dto)

            // 🔥 서버 응답 상태 확인
            Log.e(
                "SIGNUP_RESPONSE",
                "code=${res.code()}, body=${res.errorBody()?.string()}"
            )

            if (!res.isSuccessful) {
                Log.e(
                    "Signup",
                    "HTTP ${res.code()} ${res.message()}"
                )
                false
            } else {
                Log.d("Signup", "회원가입 성공: ${res.body()}")
                true
            }

        } catch (e: Exception) {
            Log.e("Signup", "네트워크 예외", e)
            false
        }
    }

    // 1201 비밀번호잊음창의 휴대폰->이메일 인증 변경중, 비번재설정 추가
    override suspend fun resetPassword(email: String, newPassword: String): Boolean {
        return try {
            val res = api.resetPassword(
                mapOf(
                    "email" to email,
                    "password" to newPassword
                )
            )
            res.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun withdrawal(): Boolean {
        return try {
            val response = api.deleteAccount()
            if (response.isSuccessful) {
                tokenStore.clear() // ✅ 내 폰의 토큰 삭제 (로그아웃 처리)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
// 1127 11:27 merge seok into yun
    override fun getUserId(): Long {
        val access = tokenStore.current().access
            ?: throw IllegalStateException("No access token stored!")

        val idStr = JwtUtils.extractUserId(access)
            ?: throw IllegalStateException("User ID not found in JWT!")

        return idStr.toLong()
    }


}

class HttpAuthException(val code: Int, message: String?) :
    IOException("HTTP $code: ${message ?: "unknown"}")
