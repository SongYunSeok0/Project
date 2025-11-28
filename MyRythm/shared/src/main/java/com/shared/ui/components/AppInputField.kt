package com.shared.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
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
    useFloatingLabel: Boolean = true,   // 테두리에 라벨 텍스트 띄우기 유무
    readOnly: Boolean = false           // editScreen.kt의 읽기전용모드 속성
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = if (useFloatingLabel) {
            { Text(label) }
        } else null,
        placeholder = if (!useFloatingLabel) {
            { Text(label, color = MaterialTheme.colorScheme.surfaceVariant) }
        } else null,
        modifier = modifier
            .fillMaxWidth()
            .then(if (height != null) Modifier.height(height) else Modifier),
        shape = shape,
        singleLine = singleLine,
        maxLines = maxLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface
        )
    )
}
