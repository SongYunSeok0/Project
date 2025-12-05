package com.myrhythm.alarm.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shared.R

@Composable
fun GuardianScreen(
    username: String,
    medicineLabel: String,
    takenAtTime: String,
    mealTime: String,
    note: String,
    onStop: () -> Unit,
    onDismiss: () -> Unit
) {
    // ⭐ 로그 추가 (PatientScreen과 동일한 로직)
    LaunchedEffect(username, medicineLabel, takenAtTime, mealTime, note) {
        Log.e("GuardianScreen", "==========================================")
        Log.e("GuardianScreen", "🛡️ GuardianScreen 데이터:")
        Log.e("GuardianScreen", "  - username: '$username'")
        Log.e("GuardianScreen", "  - medicineLabel: '$medicineLabel'")
        Log.e("GuardianScreen", "  - takenAtTime: '$takenAtTime'")
        Log.e("GuardianScreen", "  - mealTime: '$mealTime'")
        Log.e("GuardianScreen", "  - note: '$note'")
        Log.e("GuardianScreen", "==========================================")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB5E5E1)) // PatientScreen과 동일한 배경색
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.pill),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )

            Spacer(Modifier.height(20.dp))

            // 사용자 이름 (PatientScreen 스타일 적용)
            Text(
                text = "$username 님의",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2D3748)
            )

            Spacer(Modifier.height(12.dp))

            // 메인 타이틀
            Text(
                "약 드실 시간이에요!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A202C)
            )

            Spacer(Modifier.height(16.dp))

            // 약 이름
            Text(
                medicineLabel,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2D3748)
            )

            Spacer(Modifier.height(12.dp))

            // 복용 시간 (takenAt)
            if (takenAtTime.isNotBlank()) {
                Log.d("GuardianScreen", "✅ takenAtTime 표시: $takenAtTime")
                Text(
                    "복용 시간: $takenAtTime",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF4A5568)
                )
                Spacer(Modifier.height(6.dp))
            } else {
                Log.w("GuardianScreen", "⚠️ takenAtTime이 비어있음")
            }

            // 식사 시간 (mealTime) - 한글 변환 로직 포함
            if (mealTime.isNotBlank()) {
                var displayMealTime = mealTime
                if (displayMealTime == "after") {
                    displayMealTime = "식후"
                }
                if (displayMealTime == "before") {
                    displayMealTime = "식전"
                }
                Log.d("GuardianScreen", "✅ mealTime 표시: $displayMealTime")
                Text(
                    displayMealTime,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF4A5568)
                )
                Spacer(Modifier.height(6.dp))
            } else {
                Log.w("GuardianScreen", "⚠️ mealTime이 비어있음")
            }

            // 메모 (note)
            if (note.isNotBlank()) {
                Log.d("GuardianScreen", "✅ note 표시: $note")
                Text(
                    note,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF4A5568)
                )
            } else {
                Log.w("GuardianScreen", "⚠️ note가 비어있음")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 확인 버튼 (PatientScreen의 '복약 완료' 버튼 스타일 - 초록색)
        Button(
            onClick = {
                Log.i("GuardianScreen", "확인 버튼 클릭")
                onStop()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Text("확인", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 알람 끄기 버튼 (PatientScreen의 '알람 끄기' 버튼 스타일 - 회색)
        Button(
            onClick = {
                Log.i("GuardianScreen", "알람 끄기 버튼 클릭")
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9CA3AF)
            )
        ) {
            Text("알람 끄기", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GuardianScreenPreview() {
    GuardianScreen(
        username = "보호자",
        medicineLabel = "어머니 혈압약",
        takenAtTime = "12:30",
        mealTime = "after",
        note = "식사 꼭 챙겨드리기",
        onStop = {},
        onDismiss = {}
    )
}