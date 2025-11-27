package com.auth.viewmodel

import android.content.Context
import android.util.Log
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.core.auth.JwtUtils
import com.data.core.auth.AuthPreferencesDataSource
import com.data.core.auth.TokenStore
import com.data.core.push.PushManager
import com.domain.model.SocialLoginResult
import com.domain.model.SignupRequest
import com.domain.repository.AuthRepository
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
import com.domain.usecase.auth.SendEmailCodeUseCase
import com.domain.usecase.auth.VerifyEmailCodeUseCase

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val refreshUseCase: RefreshTokenUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val signupUseCase: SignupUseCase,
    private val socialLoginUseCase: SocialLoginUseCase,
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase,
    private val tokenStore: TokenStore,
    private val sendEmailCodeUseCase: SendEmailCodeUseCase,
    private val verifyEmailCodeUseCase: VerifyEmailCodeUseCase,
    private val repo: AuthRepository,
    private val authPrefs: AuthPreferencesDataSource
) : ViewModel() {

    // 1127 자동로그인
    private val _autoLoginEnabled = MutableStateFlow(false)
    val autoLoginEnabled: StateFlow<Boolean> = _autoLoginEnabled
    fun setAutoLogin(enabled: Boolean) {
        _autoLoginEnabled.value = enabled
        Log.d("AuthViewModel", "자동 로그인 설정 변경: $enabled (로그인 시 저장됨)")
    }

    // -----------------------------------------------------------
    // 1) SignupForm (UI 입력 상태)
    // -----------------------------------------------------------
    data class SignupForm(
        val email: String = "",
        val code: String = "",
        val username: String = "",
        val phone: String = "",
        val birthDate: String = "",
        val gender: String = "",
        val height: Double = 0.0,
        val weight: Double = 0.0,
        val password: String = ""
    )

    private val _signupForm = MutableStateFlow(SignupForm())
    val signupForm: StateFlow<SignupForm> = _signupForm

    fun updateSignupEmail(v: String) = _signupForm.update { it.copy(email = v) }
    fun updateCode(v: String) = _signupForm.update { it.copy(code = v) }
//    fun updateSignupPassword(v: String) = _signupForm.update { it.copy(password = v) }
//    fun updateUsername(v: String) = _signupForm.update { it.copy(username = v) }
//    fun updatePhone(v: String) = _signupForm.update { it.copy(phone = v) }
//    fun updateBirth(v: String) = _signupForm.update { it.copy(birthDate = v) }
//    fun updateGender(v: String) = _signupForm.update { it.copy(gender = v) }
//    fun updateHeight(v: Double) = _signupForm.update { it.copy(height = v) }
//    fun updateWeight(v: Double) = _signupForm.update { it.copy(weight = v) }
//    fun updatePassword(v: String) = _signupForm.update { it.copy(password = v) }

    // -----------------------------------------------------------
    // 로그인 / 상태 관리
    // -----------------------------------------------------------
    data class UiState(
        val loading: Boolean = false,
        val isLoggedIn: Boolean = false,
        val userId: String? = null
    )

    data class FormState(
        val email: String = "",
        val password: String = ""
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events

    private fun emit(msg: String) = _events.tryEmit(msg)
    fun emitInfo(msg: String) = emit(msg)

    private val _form = MutableStateFlow(FormState())
    val form: StateFlow<FormState> = _form

    fun updateLoginEmail(v: String) = _form.update { it.copy(email = v) }
    fun updateLoginPW(v: String) = _form.update { it.copy(password = v) }

    // 4) 이메일 인증
    fun sendCode() = viewModelScope.launch {
        val ok = runCatching {
            sendEmailCodeUseCase(signupForm.value.email)
        }.getOrDefault(false)
        emit(if (ok) "인증코드 전송" else "전송 실패")
    }

    fun verifyCode() = viewModelScope.launch {
        val f = signupForm.value
        val ok = runCatching {
            verifyEmailCodeUseCase(f.email, f.code)
        }.getOrDefault(false)
        emit(if (ok) "인증 성공" else "인증 실패")
    }

    // 6) 기존 signup(req) (소셜로그인용)
    fun signup(req: SignupRequest) = viewModelScope.launch {
        _state.update { it.copy(loading = true) }
        val ok = runCatching { signupUseCase(req) }.getOrDefault(false)
        _state.update { it.copy(loading = false) }
        emit(if (ok) "회원가입 성공" else "회원가입 실패")
    }

    // -----------------------------------------------------------
    // 로그인
    // -----------------------------------------------------------
    fun login() = viewModelScope.launch {
        val email = form.value.email
        val pw = form.value.password

        if (email.isBlank() || pw.isBlank()) {
            emit("ID와 비번을 입력하세요")
            return@launch
        }

        _state.update { it.copy(loading = true) }

        // 자동로그인 적용
        val result = loginUseCase(email, pw, _autoLoginEnabled.value)
        val tokens = result.getOrNull() // 결과에서 토큰 추출
        val ok = result.isSuccess

        if (ok && tokens != null) {
            Log.d("AuthViewModel", "✅ 로그인 성공 - 자동로그인: ${_autoLoginEnabled.value}")
            authPrefs.setAutoLoginEnabled(_autoLoginEnabled.value)

            PushManager.fcmToken?.let { token ->
                runCatching { registerFcmTokenUseCase(token) }
            }

            // 토큰에서 userId 추출
            val uid = tokens.access?.let { JwtUtils.extractUserId(it) }

            _state.update {
                it.copy(
                    loading = false,
                    isLoggedIn = true,
                    userId = uid
                )
            }
            emit("로그인 성공")
        } else {
            _state.update { it.copy(loading = false, isLoggedIn = false) }
            emit("이메일 또는 비밀번호가 올바르지 않습니다.")
        }
    } // ⭐ login 함수 종료

    // 👇 이제 login 함수 밖으로 나왔으므로 LoginScreen에서 참조 가능합니다.

    fun tryRefresh() = viewModelScope.launch {
        val ok = runCatching { refreshUseCase() }.getOrDefault(false)
        if (ok) emit("토큰 갱신")
    }

    fun logout() = viewModelScope.launch {
        runCatching { logoutUseCase() }
        _state.update { it.copy(isLoggedIn = false) }
        _autoLoginEnabled.value = false // 로그아웃 시 로컬/소셜 모두 자동로그인 해제
        emit("로그아웃 완료")
    }

    // 카카오 로그인
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

    // 구글 로그인
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

    // -----------------------------------------------------------
    // 소셜 로그인
    // -----------------------------------------------------------
    private fun handleSocialLogin(
        provider: String,
        accessToken: String?,
        idToken: String?,
        socialId: String,
        onResult: (Boolean, String) -> Unit,
        onNeedAdditionalInfo: (String, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val apiResult = socialLoginUseCase(
                    provider = provider,
                    socialId = socialId,
                    accessToken = accessToken,
                    idToken = idToken
                )
                apiResult.onSuccess { result ->
                    withContext(Dispatchers.Main) {
                        when (result) {
                            is SocialLoginResult.Success -> {
                                // 소셜로그인은 로그인 성공 시 언제나 자동 로그인 저장
                                authPrefs.setAutoLoginEnabled(true)

                                _state.update {
                                    it.copy(
                                        isLoggedIn = true,
                                        loading = false,
                                        userId = socialId
                                    )
                                }
                                PushManager.fcmToken?.let { token ->
                                    runCatching { registerFcmTokenUseCase(token) }
                                        .onFailure { emit("푸시 토큰 등록 실패") }
                                }
                                onResult(true, "$provider 로그인 성공")
                            }

                            is SocialLoginResult.NeedAdditionalInfo -> {
                                _state.update {
                                    it.copy(
                                        isLoggedIn = true,
                                        loading = false,
                                        userId = socialId
                                    )
                                }
                                PushManager.fcmToken?.let { token ->
                                    runCatching { registerFcmTokenUseCase(token) }
                                }
                                onResult(true, "$provider 신규 회원 등록 성공")
                            }

                            is SocialLoginResult.Error -> {
                                onResult(false, result.message ?: "서버 오류")
                            }
                        }
                    }
                }.onFailure { e ->
                    withContext(Dispatchers.Main) {
                        onResult(false, parseError(e))
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, parseError(e))
                }
            }
        }
    }

    private fun parseError(t: Throwable?): String {
        if (t == null) return "알 수 없는 오류"
        return when (t) {
            is HttpException -> "HTTP ${t.code()}"
            else -> t.message ?: "알 수 없는 오류"
        }
    }
}