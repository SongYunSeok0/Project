package com.auth.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shared.R
import com.shared.ui.theme.AppFieldHeight
import com.shared.ui.theme.loginTheme

@Composable
fun SocialLoginSection(
    onKakaoClick: () -> Unit,
    onGoogleClick: () -> Unit,
) {
    val oauthText = stringResource(R.string.auth_oauth)
    val kakaoLoginText = stringResource(R.string.auth_kakaologin_description)
    val googleLoginText = stringResource(R.string.auth_googlelogin_description)

    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = oauthText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.loginTheme.loginTertiary
        )
        Spacer(Modifier.width(8.dp))
    }

    if (expanded) {
        Spacer(Modifier.height(14.dp))

        Image(
            painter = painterResource(R.drawable.kakao_login_button),
            contentDescription = kakaoLoginText,
            modifier = Modifier
                .fillMaxWidth()
                .height(AppFieldHeight)
                .clip(MaterialTheme.shapes.medium)
                .clickable(onClick = onKakaoClick),
            contentScale = ContentScale.FillBounds
        )

        Spacer(Modifier.height(8.dp))

        Image(
            painter = painterResource(R.drawable.google_login_button),
            contentDescription = googleLoginText,
            modifier = Modifier
                .fillMaxWidth()
                .height(AppFieldHeight)
                .clip(MaterialTheme.shapes.medium)
                .clickable(onClick = onGoogleClick),
            contentScale = ContentScale.FillBounds
        )

        Spacer(Modifier.height(30.dp))
    }
}
