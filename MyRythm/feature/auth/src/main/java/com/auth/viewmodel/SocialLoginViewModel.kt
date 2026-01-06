package com.auth.viewmodel

import android.content.Context
import androidx.credentials.*
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.data.core.push.PushManager
import com.domain.model.ApiResult
import com.domain.model.DomainError
import com.domain.model.SocialLoginParam
import com.domain.model.SocialLoginResult
import com.domain.usecase.auth.SocialLoginUseCase
import com.domain.usecase.push.RegisterFcmTokenUseCase
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
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
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val isLoggedIn: Boolean = false,
        val userId: String? = null,
        val errorMessage: String? = null,
        val needAdditionalInfo: Pair<String, String>? = null // (socialId, provider)
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun kakaoOAuth(context: Context) {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                _uiState.update { it.copy(errorMessage = "카카오 로그인 실패") }
            } else if (token != null) {
                UserApiClient.instance.me { user, _ ->
                    if (user != null) {
                        handleSocialLogin(
                            provider = "kakao",
                            socialId = user.id.toString(),
                            accessToken = token.accessToken,
                            idToken = null
                        )
                    } else {
                        _uiState.update { it.copy(errorMessage = "사용자 정보 요청 실패") }
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

    fun googleOAuth(context: Context, googleClientId: String) {
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
                handleGoogleCredential(response)
            } catch (e: GetCredentialCancellationException) {
                _uiState.update { it.copy(errorMessage = "구글 로그인 취소") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "구글 로그인 실패") }
            }
        }
    }

    private fun handleGoogleCredential(result: GetCredentialResponse) {
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
                    idToken = token.idToken
                )
            } catch (e: GoogleIdTokenParsingException) {
                _uiState.update { it.copy(errorMessage = "구글 토큰 파싱 실패") }
            }
        }
    }

    private fun handleSocialLogin(
        provider: String,
        socialId: String,
        accessToken: String?,
        idToken: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }

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
                            _uiState.update {
                                it.copy(
                                    loading = false,
                                    isLoggedIn = true,
                                    userId = data.userId.toString(),
                                    errorMessage = null
                                )
                            }

                            // FCM 토큰 등록
                            PushManager.fcmToken?.let { fcmToken ->
                                viewModelScope.launch {
                                    registerFcmTokenUseCase(fcmToken)
                                }
                            }
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
                                    errorMessage = data.message ?: "서버 오류"
                                )
                            }
                        }
                    }
                }

                is ApiResult.Failure -> {
                    val message = mapErrorToMessage(result.error)
                    _uiState.update {
                        it.copy(loading = false, errorMessage = message)
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearNeedAdditionalInfo() {
        _uiState.update { it.copy(needAdditionalInfo = null) }
    }

    private fun mapErrorToMessage(error: DomainError): String {
        return when (error) {
            is DomainError.Auth -> "소셜 로그인 인증 실패"
            is DomainError.Network -> "인터넷 연결을 확인해주세요"
            is DomainError.Server -> "서버 오류가 발생했습니다"
            is DomainError.Conflict -> "이미 다른 방법으로 가입된 계정입니다"
            is DomainError.NotFound -> "사용자를 찾을 수 없습니다"
            is DomainError.Validation -> error.message
            is DomainError.InvalidToken -> "토큰이 유효하지 않습니다"
            is DomainError.NeedAdditionalInfo -> "추가 정보가 필요합니다"
            is DomainError.Unknown -> "소셜 로그인에 실패했습니다"
        }
    }
}