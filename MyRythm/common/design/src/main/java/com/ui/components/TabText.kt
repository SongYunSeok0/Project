package com.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TabText(text: String, selected: Boolean) {
    Text(
        text = text,
        fontSize = 16.sp,
        color = if (selected) Color(0xff59c606) else Color(0xff666666),
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
