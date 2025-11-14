package com.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.common.design.R


@Composable
fun MedicineScheduleItem(
    title: String,
    time: String,
    dose: String,
    done: Boolean
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xfff3f4f6)),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 아이콘 + 텍스트 묶음
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xff6ae0d9).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.icon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(title, fontSize = 16.sp, color = Color(0xff101828))
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.icon),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(time, fontSize = 14.sp, color = Color(0xff6a7282))
                        Spacer(Modifier.width(6.dp))
                        Text("•", fontSize = 14.sp, color = Color(0xff6a7282))
                        Spacer(Modifier.width(6.dp))
                        Text(dose, fontSize = 14.sp, color = Color(0xff6a7282))
                    }
                }
            }

            // 오른쪽 상태 버튼
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (done) Color(0xff6ae0d9).copy(alpha = 0.1f)
                        else Color(0xfff3f4f6)
                    )
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (done) "복용 완료" else "복용하기",
                    fontSize = 12.sp,
                    color = if (done) Color(0xff6ae0d9) else Color(0xff4a5565)
                )
            }
        }
    }
}