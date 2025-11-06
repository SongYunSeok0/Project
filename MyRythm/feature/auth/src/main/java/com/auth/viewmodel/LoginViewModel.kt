//package com.auth.viewmodel
//
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.auth.data.api.RetrofitClient
//import com.auth.data.api.UserApi
//import com.auth.data.model.UserLoginRequest
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//class LoginViewModel : ViewModel() {
//
//    // ✅ Retrofit 객체 초기화
//    private val api: UserApi = RetrofitClient.instance
//
//    // ✅ 필요 시 오프라인 테스트 모드 (서버 없을 때 사용)
//    private val isOfflineMode = false  // true면 서버 없이 로컬 성공 처리
//
//    fun login(id: String, pw: String, onResult: (Boolean, String) -> Unit) {
//        // 🔹 오프라인 모드일 경우 서버 없이 바로 성공
//        if (isOfflineMode) {
//            Log.w("LoginViewModel", "🧩 Offline Mode — 서버 연결 없이 로그인 통과")
//            onResult(true, "로컬 로그인 성공 (서버 없음)")
//            return
//        }
//
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                // ✅ Retrofit 및 API 객체 null 여부 확인 로그
//                Log.d("RetrofitCheck", "RetrofitClient.instance = ${RetrofitClient.instance}")
//                Log.d("RetrofitCheck", "API instance = $api")
//                Log.d("LoginViewModel", "📤 로그인 요청 전송: id=$id, pw=$pw")
//
//                // ✅ 실제 로그인 요청
//                val response = api.login(UserLoginRequest(id, pw))
//
//                if (response.isSuccessful) {
//                    val body = response.body()
//                    Log.d("LoginViewModel", "✅ 서버 응답 성공: $body")
//
//                    withContext(Dispatchers.Main) {
//                        if (body?.access != null) {
//                            onResult(true, "로그인 성공")
//                        } else {
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
//        }
//    }
//}

//
package com.auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auth.data.api.RetrofitClient
import com.auth.data.model.UserLoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel : ViewModel() {

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
}
