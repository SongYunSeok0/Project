package com.example.scheduler

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.common.design.R

private val Mint = Color(0xFF6AE0D9)
private val CardBg = Color(0xFFF9FAFB)
private val SectionTitle = Color(0xFF3B566E)
private val Hint = Color(0x800A0A0A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegiScreen(modifier: Modifier = Modifier, onBack: () -> Unit = {}) {
    var disease by remember { mutableStateOf("") }
    val meds = remember { mutableStateListOf("") }

    Scaffold(

    ) { inner ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(top = 20.dp)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 병명
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("병명", color = SectionTitle, fontSize = 14.sp, lineHeight = 1.43.em)
                OutlinedTextField(
                    value = disease,
                    onValueChange = { disease = it },
                    placeholder = { Text("병명을 입력하세요", color = Hint, fontSize = 14.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = CardBg,
                        focusedContainerColor = CardBg,
                        focusedBorderColor = Mint,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 약 이름(+ 추가)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("약 이름", color = SectionTitle, fontSize = 14.sp, lineHeight = 1.43.em)
                    IconButton(
                        onClick = {
                            if (meds.isEmpty() || meds.last().isNotBlank()) meds.add("")
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Mint)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "약 추가", tint = Color.White)
                    }
                }

                meds.forEachIndexed { idx, value ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = value,
                            onValueChange = { meds[idx] = it },
                            placeholder = { Text("약 이름을 입력하세요", color = Hint, fontSize = 14.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = CardBg,
                                focusedContainerColor = CardBg,
                                focusedBorderColor = Mint,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (meds.size > 1) {
                            TextButton(
                                onClick = { meds.removeAt(idx) },
                                colors = ButtonDefaults.textButtonColors(contentColor = SectionTitle)
                            ) { Text("삭제", fontSize = 12.sp) }
                        }
                    }
                }
            }

            // 복용 횟수(하루)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("복용 횟수 (하루)", color = SectionTitle, fontSize = 14.sp, lineHeight = 1.43.em)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RoundIcon(R.drawable.upload)
                    Text("3회", color = Mint, style = TextStyle(fontSize = 20.sp))
                    RoundIcon(R.drawable.upload)
                }
            }

            // 복용 시간
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("복용 시간", color = SectionTitle, fontSize = 14.sp, lineHeight = 1.43.em)
                RepeatTimeRow("1회차")
                RepeatTimeRow("2회차")
                RepeatTimeRow("3회차")
            }

            // 복용 기간
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("복용 기간 *", color = SectionTitle, fontSize = 14.sp, lineHeight = 1.43.em)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DateBox("시작일", Modifier.weight(1f))
                    DateBox("종료일", Modifier.weight(1f))
                }
            }

            // 식사 관계
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("식사 관계", color = SectionTitle, fontSize = 14.sp, lineHeight = 1.43.em)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    SegChip("식전", selected = false, modifier = Modifier.weight(1f))
                    SegChip("식후", selected = true, modifier = Modifier.weight(1f))
                    SegChip("관계없음", selected = false, modifier = Modifier.weight(1f))
                }
            }

            // 메모
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("메모 / 주의사항", color = SectionTitle, fontSize = 14.sp, lineHeight = 1.43.em)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardBg)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    Text("복용 시 주의사항이나 메모를 입력하세요", color = Hint, fontSize = 14.sp, lineHeight = 1.43.em)
                }
            }

            Spacer(Modifier.height(8.dp))

            // 등록 완료 버튼
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Mint,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, RoundedCornerShape(14.dp))
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("등록 완료", color = Color.White, fontSize = 16.sp, lineHeight = 1.5.em)
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun RoundIcon(res: Int) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFF3F4F6)),
        contentAlignment = Alignment.Center
    ) {
        Image(painter = painterResource(id = res), contentDescription = null, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun RepeatTimeRow(label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, color = Color(0xFF6F8BA4), fontSize = 14.sp, modifier = Modifier.width(48.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CardBg)
        )
    }
}

@Composable
private fun DateBox(label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = Color(0xFF6F8BA4), fontSize = 12.sp, lineHeight = 1.33.em)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CardBg)
        )
    }
}

@Composable
private fun SegChip(text: String, selected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Mint else CardBg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFF6F8BA4),
            fontSize = 14.sp,
            lineHeight = 1.43.em
        )
    }
}

@Preview(widthDp = 392, heightDp = 1342, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun RegiScreenPreview() {
    RegiScreen()
}
