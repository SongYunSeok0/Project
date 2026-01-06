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
import com.data.core.auth.AuthPreferencesDataSource
import com.data.core.push.PushManager
import com.domain.exception.DomainException
import com.domain.model.SocialLoginResult
import com.domain.usecase.auth.SocialLoginUseCase
import com.domain.usecase.push.RegisterFcmTokenUseCase
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SocialLoginViewModel @Inject constructor(
    private val socialLoginUseCase: SocialLoginUseCase,
    private val registerFcmTokenUseCase: RegisterFcmTokenUseCase,
    private val authPrefs: AuthPreferencesDataSource
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val isLoggedIn: Boolean = false,
        val userId: String? = null,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

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
                _uiState.update { it.copy(loading = true) }

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
                                authPrefs.setAutoLoginEnabled(true)

                                _uiState.update {
                                    it.copy(
                                        isLoggedIn = true,
                                        loading = false,
                                        userId = result.userId.toString()
                                    )
                                }

                                PushManager.fcmToken?.let { token ->
                                    runCatching { registerFcmTokenUseCase(token) }
                                }
                                onResult(true, "$provider 로그인 성공")
                            }

                            is SocialLoginResult.NeedAdditionalInfo -> {
                                _uiState.update {
                                    it.copy(
                                        isLoggedIn = true,
                                        loading = false
                                    )
                                }
                                onNeedAdditionalInfo(result.socialId, result.provider)
                            }

                            is SocialLoginResult.Error -> {
                                _uiState.update { it.copy(loading = false) }
                                onResult(false, result.message ?: "서버 오류")
                            }
                        }
                    }
                }.onFailure { error ->
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(loading = false) }
                        val message = when (error) {
                            is DomainException.NetworkException -> "인터넷 연결을 확인해주세요"
                            is DomainException.AuthException -> "인증에 실패했습니다"
                            else -> "소셜 로그인에 실패했습니다"
                        }
                        onResult(false, message)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(loading = false) }
                    onResult(false, "소셜 로그인 오류: ${e.localizedMessage}")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}