package com.myrhythm.alarm.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.shared.R

@Composable
fun GuardianScreen(
    username: String,
    medicineLabel: String,
    patientPhone: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current

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

            // 안내 문구
            Text(
                text = "복약 여부 미확인",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A202C)
            )

            Spacer(Modifier.height(40.dp))

            Text(
                text = buildAnnotatedString {
                    // username 강조
                    pushStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp   // 강조 글씨 크기
                        )
                    )
                    append(username)
                    pop()

                    append(" 님의 ")

                    // medicineLabel 강조
                    pushStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp   // 강조 글씨 크기
                        )
                    )
                    append(medicineLabel)
                    pop()
                },
                fontSize = 20.sp,  // 기본 글씨 크기
                color = Color(0xFF2D3748)
            )
            Text(
                text = "복용 여부가 아직 확인되지 않았습니다.",
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF2D3748)
            )
            Text(
                text = "확인이 필요합니다.",
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF2D3748)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$patientPhone")
                }
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("$username 에게 전화하기", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6B6B)
            )
        ) {
            Text("닫기", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GuardianScreenPreview() {
    GuardianScreen(
        username = "홍길동",
        medicineLabel = "타이레놀 500mg",
        patientPhone = "01066232352",
        onClose = {}
    )
}
