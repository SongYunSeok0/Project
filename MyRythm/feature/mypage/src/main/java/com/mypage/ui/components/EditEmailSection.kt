package com.mypage.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shared.R
import com.shared.ui.components.AppButton
import com.shared.ui.components.AppInputField
import com.shared.ui.theme.AppFieldHeight
import kotlinx.coroutines.delay

@Composable
fun EditEmailSection(
    label: String,
    email: String,
    onEmailChange: (String) -> Unit,
    isVerified: Boolean,
    onVerifiedChange: (Boolean) -> Unit,
    isReadOnly: Boolean,
    protName: String? = null,
    onProtNameChange: ((String) -> Unit)? = null,
    protNameLabel: String = "",
    sendCode: (String) -> Unit,
    verifyCode: (String, String, (Boolean) -> Unit) -> Unit,
    checkEmailDuplicate: (String, (Boolean) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val sendText = stringResource(R.string.send)
    val resendText = "재전송"
    val verificationText = stringResource(R.string.verification)
    val verificationCodeText = stringResource(R.string.verification_code)
    val enterEmailMessage = stringResource(R.string.mypage_message_enter_email)
    val emailDuplicateMessage = stringResource(R.string.mypage_message_email_duplicate)
    val codeSentMessage = stringResource(R.string.mypage_message_code_sent)
    val verificationSuccessText = stringResource(R.string.verification_success)
    val verificationFailedText = stringResource(R.string.verification_failed)

    var isSent by rememberSaveable { mutableStateOf(false) }
    var code by rememberSaveable { mutableStateOf("") }
    var remainingSeconds by rememberSaveable { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var sendCount by rememberSaveable { mutableIntStateOf(0) }

    // ⏱️ 이메일 타이머
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning && remainingSeconds > 0) {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
            }
            isTimerRunning = false
        }
    }

    Column {
        if (onProtNameChange != null) {
            AppInputField(
                value = protName ?: "",
                onValueChange = onProtNameChange,
                label = protNameLabel,
                outlined = true,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        AppInputField(
            value = email,
            onValueChange = onEmailChange,
            label = label,
            outlined = true,
            singleLine = true,
            readOnly = isReadOnly || isVerified,
            trailingContent = {
                if (!isVerified && !isReadOnly) {
                    AppButton(
                        text = if (isSent) resendText else sendText,
                        height = AppFieldHeight,
                        width = 80.dp,
                        enabled = email.isNotBlank() && sendCount < 5,
                        onClick = {
                            if (email.isBlank()) {
                                Toast.makeText(context, enterEmailMessage, Toast.LENGTH_SHORT).show()
                                return@AppButton
                            }
                            checkEmailDuplicate(email) { isDuplicate ->
                                if (isDuplicate) {
                                    Toast.makeText(context, emailDuplicateMessage, Toast.LENGTH_LONG).show()
                                } else {
                                    sendCode(email)
                                    isSent = true
                                    onVerifiedChange(false)
                                    code = ""
                                    sendCount++
                                    remainingSeconds = 180
                                    isTimerRunning = true
                                    Toast.makeText(context, codeSentMessage, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        )

        // ⏱️ 타이머 표시
        if (isTimerRunning && remainingSeconds > 0) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "인증 번호 발송 완료",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier.padding(start = 8.dp)
                )
                Text(
                    text = "%02d:%02d".format(remainingSeconds / 60, remainingSeconds % 60),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF6B6B),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        if (isSent && !isVerified) {
            Spacer(Modifier.height(8.dp))
            AppInputField(
                value = code,
                onValueChange = { code = it },
                label = verificationCodeText,
                outlined = true,
                singleLine = true,
                trailingContent = {
                    AppButton(
                        text = verificationText,
                        height = AppFieldHeight,
                        width = 80.dp,
                        enabled = code.isNotBlank(),
                        onClick = {
                            verifyCode(email, code) { ok ->
                                if (ok) {
                                    onVerifiedChange(true)
                                    isSent = false
                                    isTimerRunning = false
                                    Toast.makeText(context, verificationSuccessText, Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, verificationFailedText, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            )
        }

        // ✅ 인증 완료 메시지
        if (isVerified) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "인증 완료",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9E9E9E),
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp)
            )
        }
    }
}