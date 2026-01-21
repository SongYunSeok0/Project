package com.auth.viewmodel

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.ApiResult
import com.domain.model.SocialLoginParam
import com.domain.model.SocialLoginResult
import com.domain.usecase.auth.SocialLoginUseCase
import com.domain.usecase.push.GetFcmTokenUseCase
import com.domain.usecase.push.RegisterFcmTokenUseCase
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.User
import com.shared.mapper.toUiError
import com.shared.model.UiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialLoginViewModel @Inject constructor(
    private val socialLoginUseCase: SocialLoginUseCase,
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase,
    private val getFcmTokenUseCase: GetFcmTokenUseCase
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val isLoggedIn: Boolean = false,
        val userId: String? = null,
        val error: UiError? = null,
        val needAdditionalInfo: Pair<String, String>? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun kakaoOAuth(context: Context, autoLoginChecked: Boolean) {
        val kakaoCallback: (OAuthToken?, Throwable?) -> Unit = kakaoCallback@{ token, error ->
            if (error != null) {
                Log.e("KakaoLogin", "LOGIN FAIL: ${error.message}", error)
                _uiState.update { it.copy(error = UiError.Message("카카오 로그인 실패")) }
                return@kakaoCallback
            }
            if (token == null) {
                _uiState.update { it.copy(error = UiError.Message("카카오 토큰 수신 실패")) }
                return@kakaoCallback
            }

            val meCallback: (User?, Throwable?) -> Unit = meCallback@{ user, meError ->
                if (meError != null) {
                    Log.e("KakaoLogin", "ME FAIL: ${meError.message}", meError)
                    _uiState.update { it.copy(error = UiError.Message("사용자 정보 요청 실패")) }
                    return@meCallback
                }
                if (user == null) {
                    _uiState.update { it.copy(error = UiError.Message("사용자 정보 요청 실패")) }
                    return@meCallback
                }

                handleSocialLogin(
                    provider = "kakao",
                    socialId = user.id.toString(),
                    accessToken = token.accessToken,
                    idToken = null,
                    autoLoginChecked = autoLoginChecked
                )
            }

            UserApiClient.instance.me(callback = meCallback)
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context = context, callback = kakaoCallback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context = context, callback = kakaoCallback)
        }
    }

    fun googleOAuth(context: Context, googleClientId: String, autoLoginChecked: Boolean) {
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val option = GetGoogleIdOption.Builder()
                    .setServerClientId(googleClientId)
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(option)
                    .build()

                val response = credentialManager.getCredential(context, request)
                handleGoogleCredential(response, autoLoginChecked)

            } catch (e: GetCredentialCancellationException) {
                Log.e("GoogleLogin", "CANCEL: ${e::class.java.name} ${e.message}", e)
                _uiState.update { it.copy(error = UiError.Message("구글 로그인 취소")) }
            } catch (e: NoCredentialException) {
                Log.e("GoogleLogin", "NO_CREDENTIAL: ${e::class.java.name} ${e.message}", e)
                _uiState.update { it.copy(error = UiError.Message("기기에서 구글 계정을 찾지 못했습니다")) }
            } catch (e: GetCredentialProviderConfigurationException) {
                Log.e("GoogleLogin", "PROVIDER_CONFIG: ${e::class.java.name} ${e.message}", e)
                _uiState.update { it.copy(error = UiError.Message("구글 로그인 제공자 설정 문제")) }
            } catch (e: Exception) {
                Log.e("GoogleLogin", "FAIL: ${e::class.java.name} ${e.message}", e)
                _uiState.update { it.copy(error = UiError.Message("구글 로그인 실패: ${e.message ?: "unknown"}")) }
            }
        }
    }

    private fun handleGoogleCredential(result: GetCredentialResponse, autoLoginChecked: Boolean) {
        val credential = result.credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val token = GoogleIdTokenCredential.createFrom(credential.data)
                val socialId = runCatching { token.id }.getOrNull() ?: "google"

                handleSocialLogin(
                    provider = "google",
                    socialId = socialId,
                    accessToken = null,
                    idToken = token.idToken,
                    autoLoginChecked = autoLoginChecked
                )
            } catch (e: GoogleIdTokenParsingException) {
                Log.e("GoogleLogin", "TOKEN PARSE FAIL: ${e.message}", e)
                _uiState.update { it.copy(error = UiError.Message("구글 토큰 파싱 실패")) }
            }
        } else {
            _uiState.update { it.copy(error = UiError.Message("구글 Credential 형식 오류")) }
        }
    }

    private fun handleSocialLogin(
        provider: String,
        socialId: String,
        accessToken: String?,
        idToken: String?,
        autoLoginChecked: Boolean
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }

            val result = socialLoginUseCase(
                SocialLoginParam(
                    provider = provider,
                    socialId = socialId,
                    accessToken = accessToken,
                    idToken = idToken
                ),
                autoLogin = autoLoginChecked
            )

            when (result) {
                is ApiResult.Success -> {
                    when (val data = result.data) {
                        is SocialLoginResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    loading = false,
                                    isLoggedIn = true,
                                    userId = data.userId.toString(),
                                    error = null
                                )
                            }
                            registerFcmToken()
                        }

                        is SocialLoginResult.NeedAdditionalInfo -> {
                            _uiState.update {
                                it.copy(
                                    loading = false,
                                    needAdditionalInfo = Pair(data.socialId, data.provider)
                                )
                            }
                        }

                        is SocialLoginResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    loading = false,
                                    error = UiError.Message(data.message ?: "서버 오류")
                                )
                            }
                        }
                    }
                }

                is ApiResult.Failure -> {
                    _uiState.update {
                        it.copy(loading = false, error = result.error.toUiError())
                    }
                }
            }
        }
    }

    private fun registerFcmToken() {
        viewModelScope.launch {
            runCatching {
                val fcmToken = getFcmTokenUseCase() ?: run {
                    Log.w("SocialLogin", "FCM 토큰을 가져올 수 없습니다")
                    return@launch
                }

                registerFcmTokenUseCase(fcmToken)
                Log.d("SocialLogin", "FCM 토큰 등록 완료")
            }.onFailure { e ->
                Log.e("SocialLogin", "FCM 토큰 등록 실패: ${e.message}", e)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearNeedAdditionalInfo() {
        _uiState.update { it.copy(needAdditionalInfo = null) }
    }
}