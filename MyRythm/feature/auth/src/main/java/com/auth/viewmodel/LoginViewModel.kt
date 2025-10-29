package com.auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auth.data.api.RetrofitClient
import com.auth.data.model.UserLoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.auth.data.api.UserApi

class LoginViewModel : ViewModel() {

    private val api = RetrofitClient.instance

    fun login(id: String, pw: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("LoginViewModel", "📤 로그인 요청 전송: id=$id, pw=$pw")

                val response = api.login(UserLoginRequest(id, pw))

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("LoginViewModel", "✅ 서버 응답 성공: $body")

                    if (body?.access != null) {
                        // ✅ UI 콜백은 메인 스레드로 전환
                        withContext(Dispatchers.Main) {
                            onResult(true, "로그인 성공")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onResult(false, "로그인 실패: 잘못된 정보입니다.")
                        }
                    }
                } else {
                    Log.e("LoginViewModel", "❌ 서버 오류: ${response.code()}")
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

