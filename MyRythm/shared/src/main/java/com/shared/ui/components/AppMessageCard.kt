package com.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.shared.ui.theme.componentTheme

@Composable
fun AppMessageCard(
    text: String,
    isUser: Boolean,
    backgroundColor: Color? = null,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    ) {
    val finalBackground = backgroundColor ?: (
            if (isUser) MaterialTheme.componentTheme.inquiryCardQuestion
            else MaterialTheme.componentTheme.inquiryCardAnswer
            )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(finalBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = text
        )
    }
}
