package com.shared.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shared.ui.theme.AppFieldHeight
import com.shared.ui.theme.componentTheme

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
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    focusedContainerColor: Color? = null,
    unfocusedContainerColor: Color? = null,
    colors: TextFieldColors? = null
    ) {
    val bgColor = MaterialTheme.colorScheme.background
    val outlineColor = MaterialTheme.colorScheme.outline
    val borderColorFocused = MaterialTheme.colorScheme.primary
    val borderColorUnfocused = MaterialTheme.colorScheme.surfaceVariant

    val defaultColors = if (outlined) {
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
            focusedContainerColor = focusedContainerColor ?: bgColor,
            unfocusedContainerColor = unfocusedContainerColor ?: bgColor,
            focusedBorderColor = outlineColor,
            unfocusedBorderColor = outlineColor,
            focusedLabelColor = MaterialTheme.componentTheme.appTransparent,
            unfocusedLabelColor = MaterialTheme.componentTheme.appTransparent
        )
    }

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
            .then(
                if (!outlined) Modifier.border(1.dp, outlineColor, shape)
                else Modifier
            )
            .then(if (height != null) Modifier.height(AppFieldHeight) else Modifier),

        shape = shape,
        singleLine = singleLine,
        maxLines = maxLines,
        readOnly = readOnly,
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction,
            keyboardType = keyboardType
        ),
        keyboardActions = keyboardActions,
        // 필드 옆에 추가하는 인증/아이콘 등등 UI
        leadingIcon = {
            if (leadingContent != null) leadingContent()

        },
        trailingIcon = {
            if (trailingContent != null) trailingContent()
        },

        colors = if (outlined) {
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColorFocused,
                unfocusedBorderColor = borderColorUnfocused,
                focusedLabelColor = borderColorFocused,
                unfocusedLabelColor = borderColorUnfocused,
                focusedContainerColor = bgColor,
                unfocusedContainerColor = bgColor,
            )
        } else {
            TextFieldDefaults.colors(
                focusedContainerColor = defaultColors.focusedContainerColor,
                unfocusedContainerColor = defaultColors.unfocusedContainerColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedLabelColor = defaultColors.focusedLabelColor,
                unfocusedLabelColor = defaultColors.unfocusedLabelColor
            )
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface
        )
    )
}
