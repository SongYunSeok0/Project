package com.shared.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shared.R
import com.shared.ui.theme.LoginTertiary

@Composable
fun ChatbotHeader(
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chatbotText = stringResource(R.string.chatbot)
    val botIconText = stringResource(R.string.chatbot_icon)
    val chatbotProfile = stringResource(R.string.chatbotprofile)

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                botIconText,
                style = MaterialTheme.typography.labelLarge
            )
        }

        Column {
            Text(
                chatbotText,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                chatbotProfile,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.reload),
            contentDescription = "reload",
            modifier = Modifier
                .size(35.dp)
                .clickable { onResetClick() }
        )
    }
}


@Composable
fun ProfileHeader(
    username: String?,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        // 프로필 이미지
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0xffffb7c5)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "😊", fontSize = 48.sp)
        }

        Spacer(Modifier.width(16.dp))

        // 인사말 + 사용자명
        Column {
            Text(text = "안녕하세요",
                color = LoginTertiary
            )

            Text(
                text = "${username ?: ""}님",
                style = TextStyle(fontSize = 18.sp),
                color = LoginTertiary
            )
        }
    }
}
