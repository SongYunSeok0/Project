package com.auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auth.data.api.RetrofitClient
import com.auth.data.api.UserApi
import com.auth.data.model.UserSignupRequest
import kotlinx.coroutines.launch

class SignupViewModel : ViewModel() {

    fun signup(user: UserSignupRequest, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("SignupViewModel", "📤 회원가입 요청 전송: $user")

                // ✅ Retrofit에서 UserApi 생성
                val api = RetrofitClient.instance
                val response = api.signup(user)

                if (response.isSuccessful) {
                    Log.d("SignupViewModel", "✅ 서버 응답 성공: ${response.code()}")
                    onResult(true, "회원가입 성공!")
                } else {
                    Log.e(
                        "SignupViewModel",
                        "❌ 서버 응답 실패: ${response.code()} ${response.errorBody()?.string()}"
                    )
                    onResult(false, "회원가입 실패 (${response.code()})")
                }
            } catch (e: Exception) {
                Log.e("SignupViewModel", "🚨 요청 에러: ${e.message}", e)
                onResult(false, "네트워크 에러: ${e.message}")
            }
        }
    }
}
