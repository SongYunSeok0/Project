package com.auth.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shared.R
import com.shared.ui.theme.loginTheme

@Composable
fun AutoLoginToggle(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
){
    val setAutologinText = stringResource(R.string.auth_setautologin)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = setAutologinText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.loginTheme.loginTertiary,
        )
        Spacer(Modifier.width(4.dp))
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                uncheckedThumbColor = MaterialTheme.loginTheme.loginTertiary,
                uncheckedTrackColor = MaterialTheme.loginTheme.loginTertiary.copy(alpha = 0.5f),
                uncheckedBorderColor = MaterialTheme.loginTheme.loginTertiary,
                checkedThumbColor = MaterialTheme.loginTheme.loginAppName,
                checkedTrackColor = MaterialTheme.loginTheme.loginAppName.copy(alpha = 0.7f),
                checkedBorderColor = MaterialTheme.loginTheme.loginTertiary
            )
        )
    }
}