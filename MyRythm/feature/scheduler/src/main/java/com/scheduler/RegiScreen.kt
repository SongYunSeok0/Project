package com.scheduler

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

private val Mint = Color(0xFF6AE0D9)
private val CardBg = Color(0xFFF9FAFB)
private val SectionTitle = Color(0xFF3B566E)
private val Hint = Color(0x800A0A0A)

enum class MealRelation { BEFORE, AFTER, NONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegiScreen(
    modifier: Modifier = Modifier,
    onSubmit: () -> Unit = {},
) {
    var disease by remember { mutableStateOf("") }
    val meds = remember { mutableStateListOf("") }
    var dosePerDay by remember { mutableIntStateOf(3) }
    var meal by remember { mutableStateOf(MealRelation.AFTER) }
    var memo by remember { mutableStateOf("") }
    val times = remember { mutableStateListOf("08:00", "12:00", "20:00") }

    LaunchedEffect(dosePerDay) {
        while (times.size < dosePerDay) times.add("")
        while (times.size > dosePerDay) if (times.isNotEmpty()) times.removeAt(times.lastIndex)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0)   // 상단 인셋 제거
    ) { inner ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 24.dp)         // top 패딩 제거
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 병명
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

            // 약 이름
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("약 이름", color = SectionTitle, fontSize = 14.sp, lineHeight = 1.43.em)
                meds.forEachIndexed { idx, value ->
                    val isLast = idx == meds.lastIndex
                    OutlinedTextField(
                        value = value,
                        onValueChange = { meds[idx] = it },
                        placeholder = { Text("약 이름을 입력하세요", color = Hint, fontSize = 14.sp) },
                        singleLine = true,
                        trailingIcon = {
                            if (isLast) {
                                IconButton(onClick = { meds.add("") }) {   // 항상 추가
                                    Icon(Icons.Filled.Add, contentDescription = "add", tint = Mint)
                                }
                            } else if (meds.size > 1) {
                                IconButton(onClick = { meds.removeAt(idx) }) {
                                    Icon(Icons.Filled.Close, contentDescription = "remove")
                                }
                            }
                        },
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
            }

            // 복용 횟수
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("복용 횟수 (하루)", color = SectionTitle, fontSize = 14.sp, lineHeight = 1.43.em)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { dosePerDay = (dosePerDay - 1).coerceAtLeast(1) }) {
                        Icon(Icons.Filled.Remove, contentDescription = "minus", tint = Mint)
                    }
                    Spacer(Modifier.weight(1f))
                    Text("${dosePerDay}회", color = Mint, style = TextStyle(fontSize = 20.sp))
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { dosePerDay = (dosePerDay + 1).coerceAtMost(6) }) {
                        Icon(Icons.Filled.Add, contentDescription = "plus", tint = Mint)
                    }
                }
            }

            // 복용 시간
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("복용 시간", color = SectionTitle, fontSize = 14.sp, lineHeight = 1.43.em)
                times.forEachIndexed { i, t ->
                    RepeatTimeRow("${i + 1}회차", t) { new -> times[i] = new }
                }
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
                    SegChip("식전", selected = meal == MealRelation.BEFORE, modifier = Modifier.weight(1f)) { meal = MealRelation.BEFORE }
                    SegChip("식후", selected = meal == MealRelation.AFTER, modifier = Modifier.weight(1f)) { meal = MealRelation.AFTER }
                    SegChip("관계없음", selected = meal == MealRelation.NONE, modifier = Modifier.weight(1f)) { meal = MealRelation.NONE }
                }
            }

            // 메모 / 주의사항
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("메모 / 주의사항", color = SectionTitle, fontSize = 14.sp, lineHeight = 1.43.em)
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    placeholder = { Text("복용 시 주의사항이나 메모를 입력하세요", color = Hint, fontSize = 14.sp) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = CardBg,
                        focusedContainerColor = CardBg,
                        focusedBorderColor = Mint,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 84.dp),
                    minLines = 3, maxLines = 6
                )
            }

            // 등록 완료
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Mint)
            ) { Text("등록 완료", color = Color.White, fontSize = 16.sp, lineHeight = 1.5.em) }

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun RepeatTimeRow(label: String, value: String, onChange: (String) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, color = Color(0xFF6F8BA4), fontSize = 14.sp, modifier = Modifier.width(48.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text("예: 08:00", color = Hint, fontSize = 14.sp) },
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
private fun SegChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Mint else CardBg)
            .clickable { onClick() },
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
private fun RegiScreenPreview() { RegiScreen() }
