package com.shared.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp

@Composable
fun AppInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    maxLines: Int = 10,
    height: Dp? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    outlined: Boolean = false,
    useFloatingLabel: Boolean = true,
    readOnly: Boolean = false,

    // ⭐ ChatbotScreen 적용용 확장 기능 추가
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: androidx.compose.foundation.text.KeyboardActions =
        androidx.compose.foundation.text.KeyboardActions.Default,
    keyboardType: KeyboardType = KeyboardType.Text,

    // ⭐ trailingContent 추가 (AuthInputField와 동일하게 사용 가능)
    trailingContent: @Composable (() -> Unit)? = null
) {
    val bgColor = MaterialTheme.colorScheme.surface
    val borderColorFocused = MaterialTheme.colorScheme.primary
    val borderColorUnfocused = MaterialTheme.colorScheme.surfaceVariant

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,

        label = if (outlined && useFloatingLabel) {
            { Text(label) }
        } else null,

        placeholder = if (!outlined) {
            { Text(label, color = MaterialTheme.colorScheme.surfaceVariant) }
        } else null,

        modifier = modifier
            .fillMaxWidth()
            .then(if (height != null) Modifier.height(height) else Modifier),

        shape = shape,
        singleLine = singleLine,
        maxLines = maxLines,
        readOnly = readOnly,

        // ⭐ 키보드 동작
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction,
            keyboardType = keyboardType
        ),
        keyboardActions = keyboardActions,

        // ⭐ 트레일링 지원
        trailingIcon = {
            if (trailingContent != null) trailingContent()
        },

        colors = if (outlined) {
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = bgColor,
                unfocusedContainerColor = bgColor,
                focusedBorderColor = borderColorFocused,
                unfocusedBorderColor = borderColorUnfocused,
                focusedLabelColor = borderColorFocused,
                unfocusedLabelColor = borderColorUnfocused
            )
        } else {
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = bgColor,
                unfocusedContainerColor = bgColor,
                focusedBorderColor = bgColor,
                unfocusedBorderColor = bgColor,
                focusedLabelColor = Color.Transparent,
                unfocusedLabelColor = Color.Transparent
            )
        },

        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface
        )
    )
}
