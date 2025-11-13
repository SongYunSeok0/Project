package com.auth.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.SocialLoginResult
import com.domain.model.SignupRequest
import com.domain.usecase.auth.LoginUseCase
import com.domain.usecase.auth.LogoutUseCase
import com.domain.usecase.auth.RefreshTokenUseCase
import com.domain.usecase.auth.SocialLoginUseCase
import com.domain.usecase.user.SignupUseCase
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val refreshUseCase: RefreshTokenUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val signupUseCase: SignupUseCase,
    private val socialLoginUseCase: SocialLoginUseCase
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val isLoggedIn: Boolean = false
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events

    fun info(msg: String) = _events.tryEmit(msg)
    fun emitInfo(msg: String) = info(msg)

    /** 기본 로그인 */
    fun login(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
        _state.update { it.copy(loading = true) }
        val result = runCatching { loginUseCase(email, password) }
        val ok = result.isSuccess
        _state.update { it.copy(loading = false, isLoggedIn = ok) }
        _events.tryEmit(if (ok) "로그인 성공" else parseError(result.exceptionOrNull()) ?: "로그인 실패")
    }

    /** 회원가입 */
    fun signup(req: SignupRequest) = viewModelScope.launch(Dispatchers.IO) {
        _state.update { it.copy(loading = true) }
        val result = runCatching { signupUseCase(req) }
        _state.update { it.copy(loading = false) }
        _events.tryEmit(if (result.getOrDefault(false)) "회원가입 성공" else parseError(result.exceptionOrNull()) ?: "회원가입 실패")
    }

    /** 토큰 갱신 */
    fun tryRefresh() = viewModelScope.launch(Dispatchers.IO) {
        val ok = runCatching { refreshUseCase() }.getOrDefault(false)
        if (ok) _events.tryEmit("토큰 갱신")
    }

    /** 로그아웃 */
    fun logout() = viewModelScope.launch(Dispatchers.IO) {
        runCatching { logoutUseCase() }
        _state.update { it.copy(isLoggedIn = false) }
        _events.tryEmit("로그아웃 완료")
    }

    // ----------------------------
    // ✅ 카카오 로그인
    // ----------------------------
    fun kakaoOAuth(
        context: Context,
        onResult: (Boolean, String) -> Unit,
        onNeedAdditionalInfo: (String, String) -> Unit
    ) {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                onResult(false, "카카오 로그인 실패")
            } else if (token != null) {
                UserApiClient.instance.me { user, e ->
                    if (user != null) {
                        val socialId = user.id.toString()
                        handleSocialLogin(
                            provider = "kakao",
                            accessToken = token.accessToken,
                            idToken = null,
                            socialId = socialId,
                            onResult = onResult,
                            onNeedAdditionalInfo = onNeedAdditionalInfo
                        )
                    } else {
                        onResult(false, "사용자 정보 요청 실패")
                    }
                }
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) return@loginWithKakaoTalk
                    UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                } else if (token != null) {
                    UserApiClient.instance.me { user, _ ->
                        if (user != null)
                            handleSocialLogin("kakao", token.accessToken, null, user.id.toString(), onResult, onNeedAdditionalInfo)
                    }
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    // ----------------------------
    // ✅ 구글 로그인
    // ----------------------------
    fun googleOAuth(
        context: Context,
        googleClientId: String,
        onResult: (Boolean, String) -> Unit,
        onNeedAdditionalInfo: (String, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId(googleClientId)
                    .build()

                val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

                val result = try {
                    credentialManager.getCredential(context, request)
                } catch (_: NoCredentialException) {
                    val optAll = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(googleClientId)
                        .build()
                    val reqAll = GetCredentialRequest.Builder().addCredentialOption(optAll).build()
                    credentialManager.getCredential(context, reqAll)
                }

                handleGoogleCredential(result, onResult, onNeedAdditionalInfo)
            } catch (e: GetCredentialCancellationException) {
                onResult(false, "구글 로그인 취소")
            } catch (e: Exception) {
                onResult(false, "구글 로그인 실패: ${e.localizedMessage}")
            }
        }
    }

    private fun handleGoogleCredential(
        result: GetCredentialResponse,
        onResult: (Boolean, String) -> Unit,
        onNeedAdditionalInfo: (String, String) -> Unit
    ) {
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                handleSocialLogin(
                    provider = "google",
                    accessToken = null,
                    idToken = googleIdToken.idToken,
                    socialId = googleIdToken.id,
                    onResult = onResult,
                    onNeedAdditionalInfo = onNeedAdditionalInfo
                )
            } catch (e: GoogleIdTokenParsingException) {
                onResult(false, "구글 토큰 파싱 실패")
            }
        }
    }

    // ----------------------------
    // ✅ 공통 소셜 로그인 처리
    // ----------------------------
    private fun handleSocialLogin(
        provider: String,
        accessToken: String?,
        idToken: String?,
        socialId: String,
        onResult: (Boolean, String) -> Unit,
        onNeedAdditionalInfo: (String, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val call = runCatching {
                socialLoginUseCase(
                    provider = provider,
                    socialId = socialId,
                    accessToken = accessToken,
                    idToken = idToken
                )
            }

            withContext(Dispatchers.Main) {
                if (call.isFailure) {
                    onResult(false, parseError(call.exceptionOrNull()) ?: "네트워크 오류")
                    return@withContext
                }

                when (val r = call.getOrNull()) {
                    is SocialLoginResult.Success -> {
                        onResult(true, "$provider 로그인 성공")
                    }
                    SocialLoginResult.NeedAdditionalInfo -> {
                        onNeedAdditionalInfo(socialId, provider)
                    }
                    is SocialLoginResult.Error, null -> {
                        onResult(false, r?.message ?: "서버 오류")
                    }
                }
            }
        }
    }

    // ----------------------------
    // 예외 메시지 파싱
    // ----------------------------
    private fun parseError(t: Throwable?): String? {
        if (t == null) return null
        return when (t) {
            is HttpException -> {
                val code = t.code()
                val body = try { t.response()?.errorBody()?.string() } catch (_: Throwable) { null }
                body?.takeIf { it.isNotBlank() } ?: "HTTP $code"
            }
            else -> t.message
        }
    }
}
