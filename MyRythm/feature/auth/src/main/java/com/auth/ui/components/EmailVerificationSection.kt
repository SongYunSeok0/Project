package com.auth.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shared.R
import com.shared.ui.components.AuthActionButton
import com.shared.ui.components.AuthInputField
import com.shared.ui.components.AuthSectionTitle
import com.shared.ui.theme.componentTheme
import com.shared.ui.theme.loginTheme

@Composable
fun EmailVerificationSection(
    email: String,
    code: String,
    isVerificationCompleted: Boolean,
    isEmailCodeSent: Boolean,
    isTimerRunning: Boolean,
    remainingSeconds: Int,
    canSend: Boolean,
    onEmailChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onCodeChange: (String) -> Unit,
    onVerifyClick: () -> Unit
) {
    val emailText = stringResource(R.string.email)
    val emailVerification = stringResource(R.string.email_verification)
    val sendText = stringResource(R.string.send)
    val resendText = stringResource(R.string.resend)
    val verificationText = stringResource(R.string.verification)
    val verificationCodeText = stringResource(R.string.verification_code)

    val emailCodeSentText = stringResource(R.string.email_code_sent)
    val verificationCompletedText = stringResource(R.string.verification_completed)
    val timerFormat = stringResource(R.string.timer_format)

    AuthSectionTitle(emailVerification)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        AuthInputField(
            value = email,
            onValueChange = onEmailChange,
            hint = emailText,
            modifier = Modifier.weight(1f),
            keyboardType = KeyboardType.Email,
            enabled = !isVerificationCompleted
        )

        Spacer(Modifier.width(8.dp))

        AuthActionButton(
            text = if (isEmailCodeSent) resendText else sendText,
            onClick = onSendClick,
            enabled = canSend,
            modifier = Modifier.widthIn(min = 90.dp)
        )
    }

    // 타이머 표시
    if (isTimerRunning && remainingSeconds > 0) {
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emailCodeSentText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.loginTheme.loginTertiary,
                modifier = Modifier.padding(start = 8.dp)
            )
            Text(
                text = String.format(
                    timerFormat,
                    remainingSeconds / 60,
                    remainingSeconds % 60
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,    // 1216 기존 FF6B6BFF 변경
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }

    if (isEmailCodeSent) {
        Spacer(Modifier.height(20.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AuthInputField(
                value = code,
                onValueChange = onCodeChange,
                hint = verificationCodeText,
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number,
                enabled = !isVerificationCompleted
            )

            Spacer(Modifier.width(8.dp))

            AuthActionButton(
                text = verificationText,
                onClick = onVerifyClick,
                enabled = !isVerificationCompleted && code.isNotBlank(),
                modifier = Modifier.widthIn(min = 90.dp)
            )
        }

        // 인증 완료 메시지
        if (isVerificationCompleted) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = verificationCompletedText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.componentTheme.successGreen,  // 1216 초록색컬러추가
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            )
        }
    }

}