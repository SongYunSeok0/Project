package com.auth.viewmodel

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auth.BuildConfig
import com.auth.data.model.SocialLoginRequest
import com.core.auth.TokenStore
import com.data.network.api.UserApi
import com.data.network.dto.user.UserLoginRequest
import com.data.network.dto.user.UserSignupRequest
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val api: UserApi,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val isOfflineMode = false

    data class UiState(
        val loading: Boolean = false,
        val isLoggedIn: Boolean = false
    )

    private val _state = MutableStateFlow(
        UiState(isLoggedIn = tokenStore.current().access != null)
    )
    val state: StateFlow<UiState> = _state

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events

    companion object {
        const val TAG = "KakaoOauth"
    }

    fun emitInfo(msg: String) {
        _events.tryEmit(msg)
    }

    fun login(id: String, pw: String) {
        if (isOfflineMode) {
            viewModelScope.launch(Dispatchers.IO) {
                tokenStore.set("offline-access", "offline-refresh")
                _state.update { it.copy(loading = false, isLoggedIn = true) }
                _events.tryEmit("오프라인 로그인 성공")
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.update { it.copy(loading = true) }
                val res = api.login(UserLoginRequest(id, pw))
                val body = res.body()
                val ok = res.isSuccessful && body?.access != null
                if (ok && body != null) tokenStore.set(body.access, body.refresh)

                _state.update { it.copy(loading = false, isLoggedIn = ok) }
                _events.tryEmit(if (ok) "로그인 성공" else "로그인 실패: ${res.code()}")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "login error", e)
                _state.update { it.copy(loading = false) }
                _events.tryEmit("네트워크 오류: ${e.localizedMessage}")
            }
        }
    }

    fun signup(req: UserSignupRequest) {
        if (isOfflineMode) { _events.tryEmit("로컬 회원가입 성공"); return }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.update { it.copy(loading = true) }
                val ok = api.signup(req).isSuccessful
                _state.update { it.copy(loading = false) }
                _events.tryEmit(if (ok) "회원가입 성공" else "회원가입 실패")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "signup error", e)
                _state.update { it.copy(loading = false) }
                _events.tryEmit("네트워크 오류: ${e.localizedMessage}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            tokenStore.clear()
            _state.update { it.copy(loading = false, isLoggedIn = false) }
            _events.tryEmit("로그아웃 완료")
        }
    }

    fun isLoggedIn(): Boolean = tokenStore.current().access != null

    //1112 카카오&구글 연동 로그인 병합으로 추가+일부 수정
    fun kakaoOAuth(
        context: Context,
        onResult: (Boolean, String) -> Unit,
        onNeedAdditionalInfo: (String, String) -> Unit
    ) {
        // 로그인 조합 예제
        // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(AuthViewModel.Companion.TAG, "카카오계정으로 로그인 실패", error)
                onResult(false, "카카오 로그인 실패")
            } else if (token != null) {
                Log.i(AuthViewModel.Companion.TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")

                // ✅ 로그인 성공 후 최소 정보만 요청 (user.id만)
                UserApiClient.instance.me { user, error ->
                    if (error != null) {
                        Log.e(AuthViewModel.Companion.TAG, "사용자 ID 요청 실패", error)
                        onResult(false, "사용자 정보 요청 실패")
                    } else if (user != null) {
                        val socialId = user.id.toString()   // ✅ 고유 PK 역할
                        val provider = "kakao"

                        Log.i(AuthViewModel.Companion.TAG, "카카오 사용자 식별 완료: socialId=$socialId")

                        // 💡 서버로 보낼 최소 정보만 넘김
                        handleKakaoLogin(
                            accessToken = token.accessToken,
                            socialId = socialId,
                            provider = provider,
                            onResult = onResult,
                            onNeedAdditionalInfo = onNeedAdditionalInfo
                        )
                    }
                }
            }
        }
        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    Log.e(AuthViewModel.Companion.TAG, "카카오톡으로 로그인 실패", error)

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                } else if (token != null) {
                    Log.i(AuthViewModel.Companion.TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")

                    // 서버 연동 호출
                    UserApiClient.instance.me { user, error ->
                        if (error != null) {
                            Log.e(AuthViewModel.Companion.TAG, "사용자 ID 요청 실패", error)
                            onResult(false, "사용자 정보 요청 실패")
                        } else if (user != null) {
                            handleKakaoLogin(
                                accessToken = token.accessToken,
                                socialId = user.id.toString(),
                                provider = "kakao",
                                onResult = onResult,
                                onNeedAdditionalInfo = onNeedAdditionalInfo
                            )
                        }
                    }
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    // 💡 새로운 함수: 획득한 카카오 토큰을 서버 API로 전송 (Placeholder)
    private fun handleKakaoLogin(
        accessToken: String,
        socialId: String,
        provider: String,
        onResult: (Boolean, String) -> Unit,
        onNeedAdditionalInfo: ((String, String) -> Unit)? = null
    ) {
        // 이 부분에 실제 서버 API (소셜 로그인용) 호출 로직을 구현해야 합니다.
        //서버 연동 로직 (handleSocialLogin) Placeholder 실행
        Log.w(
            AuthViewModel.Companion.TAG,
            " [handleKakaoLogin 호출됨] accessToken: $accessToken, socialId: $socialId, provider: $provider"
        )


        // TODO: 1. 코루틴으로 IO 스레드 시작
        // TODO: 2. Retrofit을 사용해 서버의 소셜 로그인 엔드포인트에 accessToken 전송
        // TODO: 3. 서버 응답 (우리 서버의 JWT 토큰 등) 처리
        // TODO: 4. 성공/실패 여부를 onResult 콜백으로 Main 스레드에 전달

        // 현재는 클라이언트 단 테스트를 위해 즉시 성공 처리
        /*viewModelScope.launch(Dispatchers.Main) {
            onResult(true, "카카오 로그인 서버 연동 성공 (임시 처리)")
        }*/
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // ✅ 1. 요청 객체 생성 (SocialLoginRequest 사용)
                val request = SocialLoginRequest(
                    socialId = socialId,
                    provider = provider,
                    accessToken = accessToken
                )

                // ✅ 2. Retrofit으로 서버 전송
                //1112 수정전 val response = RetrofitClient.instance.socialLogin(request)
                //      수정후 주입받은 UserApi의 api 객체 사용
                val response = api.socialLogin(request)

                // ✅ 3. 응답 처리
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()  // LoginResponse
                        Log.d(AuthViewModel.Companion.TAG, " 카카오 로그인 서버 응답 성공: $body")

                        if (body?.access != null) {
                            onResult(true, "카카오 로그인 성공")
                        } else if (body?.needAdditionalInfo == true) {  // 산규회원여부
                            onNeedAdditionalInfo?.invoke(socialId, provider)
                        } else {
                            onResult(false, "서버 응답 데이터 오류")
                        }
                    } else {
                        Log.e(AuthViewModel.Companion.TAG, " 서버 응답 오류: ${response.code()}")
                        onResult(false, "서버 오류: ${response.code()}")
                    }
                }

            } catch (e: Exception) {
                Log.e(AuthViewModel.Companion.TAG, " 네트워크 예외 발생: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(false, "네트워크 오류: ${e.localizedMessage}")
                }
            }
        }
    }

    // 구글 프로토콜은 카카오와 다름
    fun googleOAuth(
        context: Context,
        onResult: (Boolean, String) -> Unit,
        onNeedAdditionalInfo: (String, String) -> Unit
    ) {
        val googleClientId = BuildConfig.GOOGLE_CLIENT_ID
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)

                // 가이드 request 부분
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId(googleClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // 가이드 signIn() 내부 로직
                delay(250)

                try {
                    val result = credentialManager.getCredential(context, request)
                    // Toast 대신 handleGoogleCredential 호출
                    handleGoogleCredential(result, onResult, onNeedAdditionalInfo)

                } catch (e: NoCredentialException) {
                    val googleIdOptionAll = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(googleClientId)
                        .build()

                    val requestAll = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOptionAll)
                        .build()

                    val resultAll = credentialManager.getCredential(context, requestAll)
                    handleGoogleCredential(resultAll, onResult, onNeedAdditionalInfo)
                }

            } catch (e: GetCredentialCancellationException) {
                Log.e(AuthViewModel.Companion.TAG, "구글 로그인 취소", e)
                onResult(false, "구글 로그인 취소")
            } catch (e: Exception) {
                Log.e(AuthViewModel.Companion.TAG, "구글 로그인 실패", e)
                onResult(false, "구글 로그인 실패")
            }
        }
    }

    // 여러 토큰이 있어서 토큰 필터링 과정 필요
    private fun handleGoogleCredential(
        result: GetCredentialResponse,
        onResult: (Boolean, String) -> Unit,
        onNeedAdditionalInfo: (String, String) -> Unit // 여기에 handleGoogleLogin 의 콜백 전달
    ) {
        val credential = result.credential

        // 가이드에서 필요한 부분만 (GoogleIdToken만)
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)

                // ✅ 카카오처럼 서버 전송
                handleGoogleLogin(
                    idToken = googleIdToken.idToken,
                    socialId = googleIdToken.id,
                    provider = "google",
                    onResult = onResult,
                    onNeedAdditionalInfo = onNeedAdditionalInfo
                )

            } catch (e: GoogleIdTokenParsingException) {
                Log.e(AuthViewModel.Companion.TAG, "구글 토큰 파싱 실패", e)
                onResult(false, "구글 토큰 파싱 실패")
            }
        }
    }

    // 핸들함수(레트로핏 서버 전송)
    private fun handleGoogleLogin(
        idToken: String,
        socialId: String,
        provider: String,
        onResult: (Boolean, String) -> Unit,
        onNeedAdditionalInfo: ((String, String) -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = SocialLoginRequest(
                    socialId = socialId,
                    provider = provider,
                    accessToken = null,
                    idToken = idToken
                )

                //1112 수정전 val response = RetrofitClient.instance.socialLogin(request)
                //      수정후 주입받은 UserApi의 api 객체 사용
                val response = api.socialLogin(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.access != null) {
                            onResult(true, "구글 로그인 성공")
                        } else if (body?.needAdditionalInfo == true) {
                            // 🔹 서버에서 신규 회원임을 알려주면 추가 정보 화면으로 이동 - ui에서콜백받기
                            onNeedAdditionalInfo?.invoke(socialId, provider)
                        } else {
                            onResult(false, "서버 응답 데이터 오류")
                        }
                    } else {
                        onResult(false, "서버 오류: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "네트워크 오류: ${e.localizedMessage}")
                }
            }
        }
    }
}
