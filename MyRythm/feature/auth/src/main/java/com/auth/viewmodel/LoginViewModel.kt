//package com.auth.viewmodel
//
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.auth.data.api.RetrofitClient
//import com.auth.data.model.UserLoginRequest
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import com.auth.data.api.UserApi
//
//class LoginViewModel : ViewModel() {
//
//    private val api = RetrofitClient.instance
//
//    fun login(id: String, pw: String, onResult: (Boolean, String) -> Unit) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                Log.d("LoginViewModel", "📤 로그인 요청 전송: id=$id, pw=$pw")
//
//                val response = api.login(UserLoginRequest(id, pw))
//
//                if (response.isSuccessful) {
//                    val body = response.body()
//                    Log.d("LoginViewModel", "✅ 서버 응답 성공: $body")
//
//                    if (body?.access != null) {
//                        // ✅ UI 콜백은 메인 스레드로 전환
//                        withContext(Dispatchers.Main) {
//                            onResult(true, "로그인 성공")
//                        }
//                    } else {
//                        withContext(Dispatchers.Main) {
//                            onResult(false, "로그인 실패: 잘못된 정보입니다.")
//                        }
//                    }
//                } else {
//                    Log.e("LoginViewModel", "❌ 서버 오류: ${response.code()}")
//                    withContext(Dispatchers.Main) {
//                        onResult(false, "서버 오류: ${response.code()}")
//                    }
//                }
//
//            } catch (e: Exception) {
//                Log.e("LoginViewModel", "🚨 네트워크 예외 발생: ${e.message}", e)
//                withContext(Dispatchers.Main) {
//                    onResult(false, "네트워크 오류: ${e.localizedMessage}")
//                }
//            }
//        }-
//    }
//}

package com.auth.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auth.data.api.RetrofitClient
import com.auth.data.model.SocialLoginRequest
import com.auth.data.model.UserLoginRequest
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel : ViewModel() {
    companion object {
        const val TAG = "KakaoOauth"
    }

    private val api = RetrofitClient.instance

    // ✅ 오프라인 테스트 모드 플래그
    private val isOfflineMode = true  // 🚀 true로 두면 서버 없이도 로그인됨

    fun login(id: String, pw: String, onResult: (Boolean, String) -> Unit) {
        // 🔹 오프라인 모드면 서버 요청 안 하고 바로 통과
        if (isOfflineMode) {
            Log.w("LoginViewModel", "🧩 Offline Mode — 서버 연결 없이 로그인 통과")
            onResult(true, "로컬 로그인 성공 (서버 없음)")
            return
        }

        // 🔹 실제 서버 로그인
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("LoginViewModel", "📤 로그인 요청 전송: id=$id, pw=$pw")

                val response = api.login(UserLoginRequest(id, pw))

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("LoginViewModel", "✅ 서버 응답 성공: $body")

                    withContext(Dispatchers.Main) {
                        if (body?.access != null) {
                            onResult(true, "로그인 성공")
                        } else {
                            onResult(false, "로그인 실패: 잘못된 정보입니다.")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(false, "서버 오류: ${response.code()}")
                    }
                }

            } catch (e: Exception) {
                Log.e("LoginViewModel", "🚨 네트워크 예외 발생: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(false, "네트워크 오류: ${e.localizedMessage}")
                }
            }
        }
    }

    // 1107
    fun kakaoOauth(context: Context, onResult: (Boolean, String) -> Unit) {
        // 로그인 조합 예제
        // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오계정으로 로그인 실패", error)
                onResult(false, "카카오 로그인 실패")
            } else if (token != null) {
                Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")

                // ✅ 로그인 성공 후 최소 정보만 요청 (user.id만)
                UserApiClient.instance.me { user, error ->
                    if (error != null) {
                        Log.e(TAG, "사용자 ID 요청 실패", error)
                        onResult(false, "사용자 정보 요청 실패")
                    } else if (user != null) {
                        val socialId = user.id.toString()   // ✅ 고유 PK 역할
                        val provider = "kakao"

                        Log.i(TAG, "카카오 사용자 식별 완료: socialId=$socialId")

                        // 💡 서버로 보낼 최소 정보만 넘김
                        handleKakaoLogin(
                            accessToken = token.accessToken,
                            socialId = socialId,
                            provider = provider,
                            onResult = onResult
                        )
                    }
                }
            }
        }
        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡으로 로그인 실패", error)

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                } else if (token != null) {
                    Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
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
        onResult: (Boolean, String) -> Unit
    ) {
        // 🚨 중요: 이 부분에 실제 서버 API (소셜 로그인용) 호출 로직을 구현해야 합니다.
        //서버 연동 로직 (handleSocialLogin) Placeholder 실행
        Log.w(
            TAG,
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
                val response = RetrofitClient.instance.socialLogin(request)

                // ✅ 3. 응답 처리
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()  // LoginResponse
                        Log.d(TAG, " 카카오 로그인 서버 응답 성공: $body")

                        if (body?.access != null) {
                            onResult(true, "카카오 로그인 성공")
                        } else {
                            onResult(false, "서버 응답 데이터 오류")
                        }
                    } else {
                        Log.e(TAG, " 서버 응답 오류: ${response.code()}")
                        onResult(false, "서버 오류: ${response.code()}")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, " 네트워크 예외 발생: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(false, "네트워크 오류: ${e.localizedMessage}")
                }
            }
        }
    }
}