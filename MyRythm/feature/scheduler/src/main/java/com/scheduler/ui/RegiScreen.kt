package com.scheduler.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.scheduler.viewmodel.PlanViewModel
import java.text.SimpleDateFormat
import java.util.*

private val Mint = Color(0xFF6AE0D9)
private val CardBg = Color(0xFFF9FAFB)
private val SectionTitle = Color(0xFF3B566E)
private val Hint = Color(0x800A0A0A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegiScreen(
    userId: Long,
    prescriptionId: Long,
    modifier: Modifier = Modifier,
    viewModel: PlanViewModel = hiltViewModel(),
    onCompleted: () -> Unit = {}
) {
    var medName by remember { mutableStateOf("") }
    var mealTime by remember { mutableStateOf("after") }
    var note by remember { mutableStateOf("") }

    val fmtDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val fmtTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    var date by remember { mutableStateOf(fmtDate.format(Date())) }
    var time by remember { mutableStateOf(fmtTime.format(Date())) }
    var takenTime by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // 📅 날짜 선택 다이얼로그
    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        state.selectedDateMillis?.let { date = fmtDate.format(Date(it)) }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Mint)
                ) { Text("확인", color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDatePicker = false }) { Text("취소") }
            }
        ) {
            DatePicker(state = state)
        }
    }

    // ⏰ 시간 선택 다이얼로그
    if (showTimePicker) {
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val timePickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val newTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    time = newTime
                    showTimePicker = false
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("취소") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    // ✅ 본문 UI
    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { inner ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("복용 일정 등록", style = MaterialTheme.typography.headlineSmall)

            // 🔹 약 이름
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("약 이름 *", color = SectionTitle, fontSize = 14.sp)
                OutlinedTextField(
                    value = medName,
                    onValueChange = { medName = it },
                    placeholder = { Text("약 이름을 입력하세요", color = Hint, fontSize = 14.sp) },
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

            // 🔹 식사 관계
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("식사 관계", color = SectionTitle, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    SegChip("식전", selected = mealTime == "before", modifier = Modifier.weight(1f)) { mealTime = "before" }
                    SegChip("식후", selected = mealTime == "after", modifier = Modifier.weight(1f)) { mealTime = "after" }
                    SegChip("식사와 함께", selected = mealTime == "with", modifier = Modifier.weight(1f)) { mealTime = "with" }
                }
            }

            // 🔹 복용 예정일 / 시각
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("복용 예정 시각 *", color = SectionTitle, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateBox("날짜", date, Modifier.weight(1f)) { showDatePicker = true }
                    DateBox("시간", time, Modifier.weight(1f)) { showTimePicker = true }
                }
            }

            // 🔹 실제 복용 시각
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("실제 복용 시각 (선택)", color = SectionTitle, fontSize = 14.sp)
                OutlinedTextField(
                    value = takenTime,
                    onValueChange = { takenTime = it },
                    placeholder = { Text("예: 09:15", color = Hint, fontSize = 14.sp) },
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

            // 🔹 메모
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("메모 / 주의사항", color = SectionTitle, fontSize = 14.sp)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("복용 시 주의사항을 입력하세요", color = Hint, fontSize = 14.sp) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = CardBg,
                        focusedContainerColor = CardBg,
                        focusedBorderColor = Mint,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 84.dp),
                    minLines = 3, maxLines = 6
                )
            }

            // 🔹 등록 버튼
            Button(
                onClick = {
                    // 📌 String -> Long 변환
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val takenAtMillis = sdf.parse("${date}T${time}:00")?.time ?: System.currentTimeMillis()

                    val takenMillis = if (takenTime.isNotBlank()) {
                        // takenTime 예: "09:15"
                        val today = fmtDate.format(Date())
                        sdf.parse("${today}T${takenTime}:00")?.time
                    } else null

                    viewModel.createPlan(
                        userId = userId,
                        prescriptionId = prescriptionId,
                        medName = medName,
                        takenAt = takenAtMillis,
                        mealTime = mealTime,
                        note = note.takeIf { it.isNotBlank() },
                        taken = takenMillis
                    )
                    onCompleted()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Mint)
            ) {
                Text("등록 완료", color = Color.White, fontSize = 16.sp)
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun DateBox(label: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = Color(0xFF6F8BA4), fontSize = 12.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CardBg)
                .clickable { onClick() },
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = value.ifBlank { "선택" },
                color = if (value.isNotBlank()) Color(0xFF0A0A0A) else Hint,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
        }
    }
}

@Composable
private fun SegChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Mint else CardBg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) Color.White else Color(0xFF6F8BA4), fontSize = 14.sp)
    }
}

@Preview(widthDp = 392, heightDp = 1342, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun RegiScreenPreview() {
    RegiScreen(
        userId = 1L,
        prescriptionId = 101L
    )
}
