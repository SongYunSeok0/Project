package com.auth.viewmodel

import android.content.Context
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core.push.PushManager
import com.domain.model.SocialLoginResult
import com.domain.model.SignupRequest
import com.domain.usecase.auth.LoginUseCase
import com.domain.usecase.auth.LogoutUseCase
import com.domain.usecase.auth.RefreshTokenUseCase
import com.domain.usecase.auth.SocialLoginUseCase
import com.domain.usecase.push.RegisterFcmTokenUseCase
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val refreshUseCase: RefreshTokenUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val signupUseCase: SignupUseCase,
    private val socialLoginUseCase: SocialLoginUseCase,
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val isLoggedIn: Boolean = false
    )

    data class FormState(
        val email: String="",
        val password: String=""
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events

    private fun emit(msg: String) = _events.tryEmit(msg)
    fun emitInfo(msg: String) = emit(msg)

    private val _form = MutableStateFlow(FormState())
    val form: StateFlow<FormState> = _form

    fun updateEmail(v: String) = _form.update{it.copy(email=v)}
    fun updatePW(v: String) = _form.update{it.copy(password=v)}

    // -------------------------------------------------------------------------
    // 이메일 로그인 + FCM 등록
    // -------------------------------------------------------------------------
    fun login() = viewModelScope.launch {
        val email = form.value.email
        val pw = form.value.password

        if(email.isBlank() || pw.isBlank()){
            emit("ID와 비번을 입력하세요")
            return@launch
        }
        _state.update { it.copy(loading = true) }

        val result = loginUseCase(email, pw)
        val ok = result.isSuccess

        _state.update { it.copy(loading = false, isLoggedIn = ok) }

        emit(if (ok) "로그인 성공" else "이메일 또는 비밀번호가 올바르지 않습니다.")
    }

    // -------------------------------------------------------------------------
    // 회원가입
    // -------------------------------------------------------------------------
    fun signup(req: SignupRequest) = viewModelScope.launch {
        _state.update { it.copy(loading = true) }

        val ok = runCatching { signupUseCase(req) }.getOrDefault(false)

        _state.update { it.copy(loading = false) }
        emit(if (ok) "회원가입 성공" else "회원가입 실패")
    }

    // -------------------------------------------------------------------------
    // 토큰 갱신
    // -------------------------------------------------------------------------
    fun tryRefresh() = viewModelScope.launch {
        val ok = runCatching { refreshUseCase() }.getOrDefault(false)
        if (ok) emit("토큰 갱신")
    }

    // -------------------------------------------------------------------------
    // 로그아웃
    // -------------------------------------------------------------------------
    fun logout() = viewModelScope.launch {
        runCatching { logoutUseCase() }
        _state.update { it.copy(isLoggedIn = false) }
        emit("로그아웃 완료")
    }

    // -------------------------------------------------------------------------
    // 카카오 로그인 + FCM 등록
    // -------------------------------------------------------------------------
    fun kakaoOAuth(
        context: Context,
        onResult: (Boolean, String) -> Unit,
        onNeedAdditionalInfo: (String, String) -> Unit
    ) {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                onResult(false, "카카오 로그인 실패")
            } else if (token != null) {
                UserApiClient.instance.me { user, _ ->
                    if (user != null) {
                        handleSocialLogin(
                            provider = "kakao",
                            accessToken = token.accessToken,
                            idToken = null,
                            socialId = user.id.toString(),
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
                            handleSocialLogin(
                                "kakao",
                                token.accessToken,
                                null,
                                user.id.toString(),
                                onResult,
                                onNeedAdditionalInfo
                            )
                    }
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    // -------------------------------------------------------------------------
    // 구글 로그인 + FCM 등록
    // -------------------------------------------------------------------------
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

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = try {
                    credentialManager.getCredential(context, request)
                } catch (_: NoCredentialException) {
                    val optAll = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(googleClientId)
                        .build()
                    val reqAll = GetCredentialRequest.Builder()
                        .addCredentialOption(optAll)
                        .build()
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
        if (
            credential is CustomCredential &&
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

    // -------------------------------------------------------------------------
    // 공통 소셜 로그인 처리 + FCM 등록
    // -------------------------------------------------------------------------
    private fun handleSocialLogin(
        provider: String,
        accessToken: String?,
        idToken: String?,
        socialId: String,
        onResult: (Boolean, String) -> Unit,
        onNeedAdditionalInfo: (String, String) -> Unit
    ) {
        viewModelScope.launch {
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
                        // ⭐ 소셜 로그인 성공 → FCM 등록
                        PushManager.fcmToken?.let { token ->
                            runCatching { registerFcmTokenUseCase(token) }
                                .onFailure { emit("푸시 토큰 등록 실패") }
                        }
                        onResult(true, "$provider 로그인 성공")
                    }

                    is SocialLoginResult.NeedAdditionalInfo ->
                        onNeedAdditionalInfo(socialId, provider)

                    is SocialLoginResult.Error, null ->
                        onResult(false, r?.message ?: "서버 오류")
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // 예외 메시지 파싱
    // -------------------------------------------------------------------------
    private fun parseError(t: Throwable?): String? {
        if (t == null) return null
        return when (t) {
            is HttpException -> "HTTP ${t.code()}"
            else -> t.message
        }
    }
}
