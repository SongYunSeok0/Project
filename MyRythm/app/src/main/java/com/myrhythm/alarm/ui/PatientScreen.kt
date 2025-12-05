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
fun PatientScreen(
    username: String,
    medicineLabel: String,
    takenAtTime: String,
    mealTime: String,
    note: String,
    isOwnDevice: Boolean,
    onStop: () -> Unit,
    onSnooze: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    // ⭐ 로그 추가
    LaunchedEffect(username, medicineLabel, takenAtTime, mealTime, note) {
        Log.e("PatientScreen", "==========================================")
        Log.e("PatientScreen", "📱 PatientScreen 데이터:")
        Log.e("PatientScreen", "  - username: '$username'")
        Log.e("PatientScreen", "  - medicineLabel: '$medicineLabel'")
        Log.e("PatientScreen", "  - takenAtTime: '$takenAtTime'")
        Log.e("PatientScreen", "  - mealTime: '$mealTime'")
        Log.e("PatientScreen", "  - note: '$note'")
        Log.e("PatientScreen", "==========================================")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB5E5E1))
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

            // 사용자 이름
            Text(
                text = "$username 님",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2D3748)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "약 드실 시간이에요!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A202C)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                medicineLabel,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2D3748)
            )

            Spacer(Modifier.height(12.dp))

            // 복용 시간 (takenAt)
            if (takenAtTime.isNotBlank()) {
                Log.d("PatientScreen", "✅ takenAtTime 표시: $takenAtTime")
                Text(
                    "복용 시간: $takenAtTime",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF4A5568)
                )
                Spacer(Modifier.height(6.dp))
            } else {
                Log.w("PatientScreen", "⚠️ takenAtTime이 비어있음")
            }

            // 식사 시간 (mealTime)
            if (mealTime.isNotBlank()) {
                var mealTime = mealTime
                if (mealTime == "after") {
                    mealTime = "식후"
                }
                if (mealTime == "before") {
                    mealTime = "식전"
                }
                Log.d("PatientScreen", "✅ mealTime 표시: $mealTime")
                Text(
                    mealTime,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF4A5568)
                )
                Spacer(Modifier.height(6.dp))
            } else {
                Log.w("PatientScreen", "⚠️ mealTime이 비어있음")
            }

            // 메모 (note)
            if (note.isNotBlank()) {
                Log.d("PatientScreen", "✅ note 표시: $note")
                Text(
                    note,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF4A5568)
                )
            } else {
                Log.w("PatientScreen", "⚠️ note가 비어있음")
            }
        }

        Spacer(Modifier.weight(1f))

        // 복약 완료 버튼
        if (isOwnDevice) {
            Button(
                onClick = {
                    Log.i("PatientScreen", "복약 완료 버튼 클릭")
                    onStop()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("복약 완료", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 미루기 버튼
        Button(
            onClick = {
                Log.i("PatientScreen", "⏰ 미루기 버튼 클릭")
                onSnooze()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9CA3AF)
            )
        ) {
            Text("30분 후 다시 알림", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                Log.i("PatientScreen", "⏰ 알람 끄기")
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
fun PatientScreenPreview() {
    PatientScreen(
        username = "홍길동",
        medicineLabel = "타이레놀",
        takenAtTime = "09:00",
        mealTime = "식후 30분",
        note = "물과 함께 복용",
        onStop = {},
        onSnooze = {},
        isOwnDevice = true
    )
}