package com.chatbot.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.shared.R
import com.shared.ui.components.AppButton
import com.shared.ui.components.AppInputField

@Composable
fun ChatInputField(
    input: String,
    onQuestionChange: (String) -> Unit,
    isLoading: Boolean,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sendText = stringResource(R.string.send)
    val contentMessage = stringResource(R.string.chatbot_message_content)

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 12.dp,
                bottom = 35.dp
            )
    ) {
        AppInputField(
            value = input,
            onValueChange = onQuestionChange,
            label = contentMessage,
            modifier = Modifier.weight(1f),
            imeAction = ImeAction.Send,
            keyboardActions = KeyboardActions(
                onSend = {
                    if (input.isNotBlank() && !isLoading) {
                        onSendClick()
                    }
                }
            ),
            trailingContent = {
                AppButton(
                    isCircle = true,
                    width = 44.dp,
                    height = 44.dp,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    onClick = {
                        if (input.isNotBlank() && !isLoading) {
                            onSendClick()
                        }
                    }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.send),
                        contentDescription = sendText
                    )
                }
            }
        )
    }
}