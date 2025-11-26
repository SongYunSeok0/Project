package com.auth.viewmodel

import android.content.Context
import android.util.Log
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val verifyEmailCodeUseCase: VerifyEmailCodeUseCase
) : ViewModel() {

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


    // -----------------------------------------------------------
    // 2) 기존 로그인 State
    // -----------------------------------------------------------
    data class UiState(
        val loading: Boolean = false,
        val isLoggedIn: Boolean = false,
        val userId: String? = null  // 1124 추가
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
    //1124
    fun emitInfo(msg: String) = emit(msg)

    private val _form = MutableStateFlow(FormState())
    val form: StateFlow<FormState> = _form

    // 기존 로그인 입력 필드
    fun updateLoginEmail(v: String) = _form.update { it.copy(email = v) }
    fun updateLoginPW(v: String) = _form.update { it.copy(password = v) }


    // -----------------------------------------------------------
    // 3) 회원가입 입력 업데이트
    // -----------------------------------------------------------
    fun updateSignupEmail(v: String) = _signupForm.update { it.copy(email = v) }
    fun updateCode(v: String) = _signupForm.update { it.copy(code = v) }
    fun updateUsername(v: String) = _signupForm.update { it.copy(username = v) }
    fun updatePhone(v: String) = _signupForm.update { it.copy(phone = v) }
    fun updateBirth(v: String) = _signupForm.update { it.copy(birthDate = v) }
    fun updateGender(v: String) = _signupForm.update { it.copy(gender = v) }
    fun updateHeight(v: Double) = _signupForm.update { it.copy(height = v) }
    fun updateWeight(v: Double) = _signupForm.update { it.copy(weight = v) }
    fun updatePassword(v: String) = _signupForm.update { it.copy(password = v) }


    // -----------------------------------------------------------
    // 4) 이메일 인증
    // -----------------------------------------------------------
    fun sendCode() = viewModelScope.launch {
        // runCatching을 사용하여 예외 발생 시 false 처리 (안전하게 호출)
        val ok = runCatching {
            sendEmailCodeUseCase(signupForm.value.email)
        }.getOrDefault(false)

        emit(if (ok) "인증코드 전송" else "전송 실패")
    }

    fun verifyCode() = viewModelScope.launch {
        val f = signupForm.value
        // runCatching을 사용하여 예외 발생 시 false 처리 (안전하게 호출)
        val ok = runCatching {
            verifyEmailCodeUseCase(f.email, f.code)
        }.getOrDefault(false)

        emit(if (ok) "인증 성공" else "인증 실패")
    }


    // -----------------------------------------------------------
    // 5) 이메일 회원가입 (SignupForm → SignupRequest)
    // -----------------------------------------------------------
    fun signup() = viewModelScope.launch {
        val f = signupForm.value

        val body = SignupRequest(
            email = f.email,
            username = f.username,
            phone = f.phone,
            birthDate = f.birthDate,
            gender = f.gender,
            height = f.height,
            weight = f.weight,
            password = f.password
        )

        _state.update { it.copy(loading = true) }

        val ok = runCatching { signupUseCase(body) }.getOrDefault(false)

        _state.update { it.copy(loading = false) }
        emit(if (ok) "회원가입 성공" else "회원가입 실패")
    }


    // -----------------------------------------------------------
    // 6) 기존 signup(req) (소셜로그인용)
    // -----------------------------------------------------------
    fun signup(req: SignupRequest) = viewModelScope.launch {
        _state.update { it.copy(loading = true) }

        val ok = runCatching { signupUseCase(req) }.getOrDefault(false)

        _state.update { it.copy(loading = false) }
        emit(if (ok) "회원가입 성공" else "회원가입 실패")
    }


    /*fun login() = viewModelScope.launch {
        val email = form.value.email
        val pw = form.value.password

        Log.e("AuthViewModel", "⏳ [1] login() 호출됨")
        Log.e("AuthViewModel", "📩 입력값 email=$email, pw=${"*".repeat(pw.length)}")

        if (email.isBlank() || pw.isBlank()) {
            Log.e("AuthViewModel", "❌ [2] email 또는 pw 비어있음")
            emit("ID와 비번을 입력하세요")
            return@launch
        }

        _state.update { it.copy(loading = true) }
        Log.e("AuthViewModel", "⏳ [3] loginUseCase 실행 시작")

        val result = loginUseCase(email, pw)

        Log.e("AuthViewModel", "📡 [4] loginUseCase 결과: isSuccess=${result.isSuccess}, exception=${result.exceptionOrNull()}")

        val ok = result.isSuccess

        if (ok) {
            Log.e("AuthViewModel", "로그인 성공 → FCM 토큰 등록 시도")

            PushManager.fcmToken?.let { token ->
                Log.e("AuthViewModel", "FCM token = $token")
                runCatching { registerFcmTokenUseCase(token) }
                    .onSuccess { Log.e("AuthViewModel", "FCM 토큰 등록 성공") }
                    .onFailure { Log.e("AuthViewModel", "FCM 토큰 등록 실패: ${it.message}") }
            }//1124추가 } 부터
        }

        // userId 추가
        _state.update {
            it.copy(
                loading = false,
                isLoggedIn = ok,
                userId = if (ok) email else null  // 이메일을 userId로 사용
            )
        }

        Log.e("AuthViewModel", "🏁 login() 종료: isLoggedIn=$ok, userId=${_state.value.userId}")

        emit(if (ok) "로그인 성공" else "이메일 또는 비밀번호가 올바르지 않습니다.")
    }
            *//*1124주석} ?: Log.e("AuthViewModel", "FCM token 없음")
        } else {
            Log.e("AuthViewModel", "로그인 실패")
        }

        _state.update { it.copy(loading = false, isLoggedIn = ok) }

        Log.e("AuthViewModel", "🏁 [6] login() 종료 isLoggedIn=$ok")

        emit(if (ok) "로그인 성공" else "이메일 또는 비밀번호가 올바르지 않습니다.")
    }*/

    // -----------------------------------------------------------
    // 이하 모든 기존 로그인, 카카오, 구글, FCM 유지
    // -----------------------------------------------------------

    fun login() = viewModelScope.launch {
        val email = form.value.email
        val pw = form.value.password

        if (email.isBlank() || pw.isBlank()) {
            emit("ID와 비번을 입력하세요")
            return@launch
        }

        _state.update { it.copy(loading = true) }
        val result = loginUseCase(email, pw)
        val ok = result.isSuccess

        if (ok) {
            PushManager.fcmToken?.let { token ->
                runCatching { registerFcmTokenUseCase(token) }
            }
        }

        /*
         _state.update {
            it.copy(
                loading = false,
                isLoggedIn = ok,
                userId = if (ok) email else null
            )
        }
         */
        _state.update { it.copy(loading = false, isLoggedIn = ok) }
        emit(if (ok) "로그인 성공" else "이메일 또는 비밀번호가 올바르지 않습니다.")
    }

    fun tryRefresh() = viewModelScope.launch {
        val ok = runCatching { refreshUseCase() }.getOrDefault(false)
        if (ok) emit("토큰 갱신")
    }

    fun logout() = viewModelScope.launch {
        runCatching { logoutUseCase() }
        _state.update { it.copy(isLoggedIn = false) }
        emit("로그아웃 완료")
    }


    // -----------------------------------------------------------
    // 카카오 로그인 이하 전부 기존 코드 유지
    // -----------------------------------------------------------

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

    /*// -------------------------------------------------------------------------
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
                    // ⭐ [수정] parseError가 이제 String을 반환하므로 문제 해결
                    onResult(false, parseError(call.exceptionOrNull()))
                    return@withContext
                }

                when (val r = call.getOrNull()) {
                    is SocialLoginResult.Success -> {
                        PushManager.fcmToken?.let { token ->
                            runCatching { registerFcmTokenUseCase(token) }
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
    }*/
    // AuthViewModel.kt에서 handleSocialLogin 함수만 이 버전으로 교체

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
                                // 기존 소셜로그인 회원 - 바로 로그인(메인홈 이동)
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
                                //onNeedAdditionalInfo(socialId, provider)
                                // 신규 소셜로그인 회원 - 바로 로그인(메인홈 이동, 추가정보는 팝업->에딧스크린)
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
                        onResult(false, parseError(e) ?: "네트워크 오류")
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, parseError(e) ?: "네트워크 오류")
                }
            }
        }
    }


    // ⭐ [수정] 반환 타입을 String? -> String으로 변경
    private fun parseError(t: Throwable?): String {
        if (t == null) return "알 수 없는 오류"
        return when (t) {
            is HttpException -> "HTTP ${t.code()}"
            // 메시지가 null이면 대체 텍스트 반환
            else -> t.message ?: "알 수 없는 오류"
        }
    }
}
