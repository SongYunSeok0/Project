package com.auth.viewmodel

import android.content.Context
import android.util.Log
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.core.push.PushManager
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
        val isLoggedIn: Boolean = false,
        val userId: String? = null  // 1124 추가
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
    // AuthViewModel.kt에서 login 함수만 이 버전으로 교체

    fun login() = viewModelScope.launch {
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

        Log.e("AuthViewModel", "📡 [4] loginUseCase 결과: isSuccess=${result.isSuccess}")

        val ok = result.isSuccess

        if (ok) {
            Log.e("AuthViewModel", "✅ 로컬 로그인 성공 → FCM 토큰 등록 시도")

            PushManager.fcmToken?.let { token ->
                runCatching { registerFcmTokenUseCase(token) }
                    .onSuccess { Log.e("AuthViewModel", "FCM 토큰 등록 성공") }
                    .onFailure { Log.e("AuthViewModel", "FCM 토큰 등록 실패: ${it.message}") }
            }
        }

        _state.update {
            it.copy(
                loading = false,
                isLoggedIn = ok,
                userId = if (ok) email else null
            )
        }

        Log.e("AuthViewModel", "🏁 [6] login() 종료: isLoggedIn=$ok, userId=${_state.value.userId}")

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
    /*private fun handleSocialLogin(
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
                        // 로그추가
                        Log.e("AuthViewModel", "🎉 소셜 로그인 서버 성공 → isLoggedIn = true 로 설정")

                        // 추가 - ⭐⭐ 화면 전환되도록 상태 업데이트 추가 ⭐⭐
                        _state.update { it.copy(
                            isLoggedIn = true,
                            loading = false,
                            userId = socialId
                        ) }     // 1124 // 임시- socialId 저장 추가

                        // 소셜 로그인 성공 → FCM 등록
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
        Log.e("AuthViewModel", "🔵 [0] ========== handleSocialLogin 시작 ==========")
        Log.e("AuthViewModel", "🔵 [0] provider=$provider, socialId=$socialId")

        viewModelScope.launch {
            Log.e("AuthViewModel", "🔵 [1] viewModelScope.launch 시작")

            try {
                Log.e("AuthViewModel", "🔵 [2] socialLoginUseCase 호출")
                val apiResult = socialLoginUseCase(
                    provider = provider,
                    socialId = socialId,
                    accessToken = accessToken,
                    idToken = idToken
                )

                Log.e("AuthViewModel", "🔵 [3] API 완료")
                Log.e("AuthViewModel", "🔵 [4] apiResult 타입: ${apiResult.javaClass.simpleName}")

                // ✅ Result unwrap
                apiResult.onSuccess { result ->
                    Log.e("AuthViewModel", "🔵 [5] Result.onSuccess - result 타입: ${result.javaClass.simpleName}")

                    withContext(Dispatchers.Main) {
                        when (result) {
                            is SocialLoginResult.Success -> {
                                Log.e("AuthViewModel", "🔵 [6] Success 분기 진입")
                                Log.e("AuthViewModel", "🔵 [7] 업데이트 전 state: ${_state.value}")

                                _state.update {
                                    it.copy(
                                        isLoggedIn = true,
                                        loading = false,
                                        userId = socialId
                                    )
                                }

                                Log.e("AuthViewModel", "🔵 [8] 업데이트 후 state: ${_state.value}")

                                PushManager.fcmToken?.let { token ->
                                    runCatching { registerFcmTokenUseCase(token) }
                                        .onFailure { emit("푸시 토큰 등록 실패") }
                                }

                                Log.e("AuthViewModel", "🔵 [9] onResult(true) 호출")
                                onResult(true, "$provider 로그인 성공")
                                Log.e("AuthViewModel", "🔵 [10] onResult(true) 호출 완료")
                            }

                            is SocialLoginResult.NeedAdditionalInfo -> {
                                Log.e("AuthViewModel", "🔵 [6] NeedAdditionalInfo 분기")
                                onNeedAdditionalInfo(socialId, provider)
                            }

                            is SocialLoginResult.Error -> {
                                Log.e("AuthViewModel", "🔵 [6] Error 분기: ${result.message}")
                                onResult(false, result.message ?: "서버 오류")
                            }
                        }
                    }
                }.onFailure { e ->
                    Log.e("AuthViewModel", "🔵 [5] Result.onFailure: ${e.message}")
                    withContext(Dispatchers.Main) {
                        onResult(false, parseError(e) ?: "네트워크 오류")
                    }
                }

            } catch (e: Exception) {
                Log.e("AuthViewModel", "🔵 [3-ERROR] 예외 발생: ${e.message}")
                withContext(Dispatchers.Main) {
                    onResult(false, parseError(e) ?: "네트워크 오류")
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
