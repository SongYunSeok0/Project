package com.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.shared.ui.theme.componentTheme

@Composable
fun AppMessageCard(
    text: String,
    isUser: Boolean? = null,
    backgroundColor: Color? = null,
    alpha: Float = 1f,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    textColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 1f),
    trailingContent: (@Composable (() -> Unit))? = null,
    content: @Composable () -> Unit = {}
) {
    val finalBackground = when {
        backgroundColor != null -> backgroundColor.copy(alpha = alpha)
        isUser == null -> MaterialTheme.colorScheme.background.copy(alpha = alpha)
        isUser == true -> MaterialTheme.componentTheme.inquiryCardQuestion.copy(alpha = alpha)
        else -> MaterialTheme.componentTheme.inquiryCardAnswer.copy(alpha = alpha)
    }

    val horizontalArrangement = when {
        backgroundColor != null -> Arrangement.Start
        isUser == null -> Arrangement.Start
        isUser == true -> Arrangement.End   // 사용자 말풍선 (오른쪽)
        else -> Arrangement.Start           // 관리자/챗봇 말풍선 (왼쪽)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement
    ) {
        Column(
            modifier = modifier
                .clip(shape)
                .background(finalBackground)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                style = style,
                color = textColor             )

            // 필드 내부에 추가하는 UI
            content()

            // 필드 오른쪽에 추가하는 인증/아이콘 등등 UI. 왼쪽은 leadingContent
            trailingContent?.invoke()
        }
    }
}
