package com.auth.viewmodel

import android.content.Context
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.core.auth.AuthPreferencesDataSource
import com.data.core.auth.JwtUtils
import com.data.core.auth.TokenStore
import com.data.core.push.PushManager
import com.domain.model.ApiResult
import com.domain.model.SocialLoginParam
import com.domain.model.SocialLoginResult
import com.domain.model.SignupRequest
import com.domain.usecase.auth.LoginUseCase
import com.domain.usecase.auth.LogoutUseCase
import com.domain.usecase.auth.ResetPasswordUseCase
import com.domain.usecase.auth.SendEmailCodeUseCase
import com.domain.usecase.auth.SocialLoginUseCase
import com.domain.usecase.auth.VerifyEmailCodeUseCase
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
import retrofit2.HttpException

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val signupUseCase: SignupUseCase,
    private val socialLoginUseCase: SocialLoginUseCase,
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase,
    private val sendEmailCodeUseCase: SendEmailCodeUseCase,
    private val verifyEmailCodeUseCase: VerifyEmailCodeUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val tokenStore: TokenStore,
    private val authPrefs: AuthPreferencesDataSource
) : ViewModel() {

    /* ---------- UI STATE ---------- */

    data class UiState(
        val loading: Boolean = false,
        val isLoggedIn: Boolean = false,
        val userId: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events
    private fun emit(msg: String) = _events.tryEmit(msg)

    /* ---------- LOGIN FORM ---------- */

    data class FormState(
        val email: String = "",
        val password: String = ""
    )

    private val _form = MutableStateFlow(FormState())
    val form: StateFlow<FormState> = _form

    fun updateLoginEmail(v: String) = _form.update { it.copy(email = v) }
    fun updateLoginPW(v: String) = _form.update { it.copy(password = v) }

    fun sendResetCode(email: String) = viewModelScope.launch {
        when (val result = sendEmailCodeUseCase(email)) {
            is ApiResult.Success -> emit("비밀번호 재설정 인증코드 전송")
            is ApiResult.Failure -> emit("전송 실패")
        }
    }

    fun verifyResetCode(email: String, code: String) = viewModelScope.launch {
        when (val result = verifyEmailCodeUseCase(email, code)) {
            is ApiResult.Success -> emit("재설정 인증 성공")
            is ApiResult.Failure -> emit("인증 실패")
        }
    }

    fun resetPassword(email: String, newPassword: String) = viewModelScope.launch {
        when (val result = resetPasswordUseCase(email, newPassword)) {
            is ApiResult.Success -> emit("비밀번호 재설정 성공")
            is ApiResult.Failure -> emit("비밀번호 재설정 실패")
        }
    }



    /* ---------- SIGNUP ---------- */

    private val _signupEmail = MutableStateFlow("")
    private val _signupCode = MutableStateFlow("")

    fun signup(req: SignupRequest) = viewModelScope.launch {
        _state.update { it.copy(loading = true) }

        when (signupUseCase(req)) {
            is ApiResult.Success -> emit("회원가입 성공")
            is ApiResult.Failure -> emit("회원가입 실패")
        }

        _state.update { it.copy(loading = false) }
    }

    fun updateSignupEmail(email: String) {
        _signupEmail.value = email
    }

    fun updateCode(code: String) {
        _signupCode.value = code
    }

    fun sendCode() = viewModelScope.launch {
        when (
            sendEmailCodeUseCase(
                email = _signupEmail.value,
                name = null
            )
        ) {
            is ApiResult.Success -> emit("인증코드 전송")
            is ApiResult.Failure -> emit("전송 실패")
        }
    }

    fun verifyCode() = viewModelScope.launch {
        when (
            verifyEmailCodeUseCase(
                email = _signupEmail.value,
                code = _signupCode.value
            )
        ) {
            is ApiResult.Success -> emit("인증 성공")
            is ApiResult.Failure -> emit("인증 실패")
        }
    }

    /* ---------- LOGIN ---------- */

    fun login(autoLogin: Boolean) = viewModelScope.launch {
        val (email, pw) = form.value

        if (email.isBlank() || pw.isBlank()) {
            emit("ID와 비번을 입력하세요")
            return@launch
        }

        _state.update { it.copy(loading = true) }

        when (val result = loginUseCase(email, pw, autoLogin)) {
            is ApiResult.Success -> {
                authPrefs.setAutoLoginEnabled(autoLogin)

                val uid = JwtUtils.extractUserId(tokenStore.current().access)

                _state.update {
                    it.copy(
                        loading = false,
                        isLoggedIn = true,
                        userId = uid
                    )
                }
                emit("로그인 성공")
            }

            is ApiResult.Failure -> {
                _state.update { it.copy(loading = false, isLoggedIn = false) }
                emit("이메일 또는 비밀번호가 올바르지 않습니다.")
            }
        }
    }

    /* ---------- LOGOUT ---------- */

    fun logout() = viewModelScope.launch {
        logoutUseCase()
        _state.value = UiState()
        authPrefs.setAutoLoginEnabled(false)
        emit("로그아웃 완료")
    }

    /* ---------- KAKAO LOGIN ---------- */

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
                            socialId = user.id.toString(),
                            accessToken = token.accessToken,
                            idToken = null,
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
            UserApiClient.instance.loginWithKakaoTalk(
                context = context,
                callback = callback
            )
        } else {
            UserApiClient.instance.loginWithKakaoAccount(
                context = context,
                callback = callback
            )
        }

    }

    /* ---------- GOOGLE LOGIN ---------- */

    fun googleOAuth(
        context: Context,
        googleClientId: String,
        onResult: (Boolean, String) -> Unit,
        onNeedAdditionalInfo: (String, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val option = GetGoogleIdOption.Builder()
                    .setServerClientId(googleClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(option)
                    .build()

                val response = credentialManager.getCredential(context, request)
                handleGoogleCredential(response, onResult, onNeedAdditionalInfo)
            } catch (e: GetCredentialCancellationException) {
                onResult(false, "구글 로그인 취소")
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
                val token = GoogleIdTokenCredential.createFrom(credential.data)
                handleSocialLogin(
                    provider = "google",
                    socialId = token.id,
                    accessToken = null,
                    idToken = token.idToken,
                    onResult = onResult,
                    onNeedAdditionalInfo = onNeedAdditionalInfo
                )
            } catch (_: GoogleIdTokenParsingException) {
                onResult(false, "구글 토큰 파싱 실패")
            }
        }
    }

    /* ---------- SOCIAL LOGIN CORE ---------- */

    private fun handleSocialLogin(
        provider: String,
        socialId: String,
        accessToken: String?,
        idToken: String?,
        onResult: (Boolean, String) -> Unit,
        onNeedAdditionalInfo: (String, String) -> Unit
    ) {
        viewModelScope.launch {
            when (
                val result = socialLoginUseCase(
                    SocialLoginParam(
                        provider = provider,
                        socialId = socialId,
                        accessToken = accessToken,
                        idToken = idToken
                    )
                )
            ) {
                is ApiResult.Success -> {
                    when (val data = result.data) {
                        is SocialLoginResult.Success -> {
                            _state.update {
                                it.copy(
                                    isLoggedIn = true,
                                    loading = false,
                                    userId = data.userId.toString()
                                )
                            }

                            PushManager.fcmToken?.let {
                                viewModelScope.launch {
                                    registerFcmTokenUseCase(it)
                                }
                            }

                            onResult(true, "$provider 로그인 성공")
                        }

                        is SocialLoginResult.NeedAdditionalInfo -> {
                            onNeedAdditionalInfo(data.socialId, data.provider)
                        }

                        is SocialLoginResult.Error -> {
                            onResult(false, data.message ?: "서버 오류")
                        }
                    }
                }

                is ApiResult.Failure -> {
                    onResult(false, "네트워크 오류")
                }
            }
        }
    }
}
