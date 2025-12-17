package com.auth.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.shared.R
import com.shared.ui.components.AuthInputField
import com.shared.ui.components.AuthPrimaryButton
import com.shared.ui.components.AuthSecondaryButton
import com.shared.ui.components.AuthTextButton

@Composable
fun LocalLoginSection(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    autoLoginEnabled: Boolean,
    onAutoLoginToggle: (Boolean) -> Unit,
    onForgotPasswordClick: () -> Unit,
    loading: Boolean,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
) {
    val idText = stringResource(R.string.auth_id)
    val passwordText = stringResource(R.string.auth_password)
    val pwMissingMessage = stringResource(R.string.auth_message_password_missing)
    val loginText = stringResource(R.string.auth_login)
    val loginLoading = stringResource(R.string.auth_login_loading)
    val signupText = stringResource(R.string.auth_signup)

    Spacer(Modifier.height(10.dp))

    AuthInputField(
        value = email,
        onValueChange = onEmailChange,
        hint = idText,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(12.dp))

    AuthInputField(
        value = password,
        onValueChange = onPasswordChange,
        hint = passwordText,
        isPassword = true,
        modifier = Modifier.fillMaxWidth(),
        imeAction = ImeAction.Done
    )

    Spacer(Modifier.height(12.dp))

    AutoLoginToggle(
        enabled = autoLoginEnabled,
        onToggle = onAutoLoginToggle
    )

    Spacer(Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        AuthTextButton(
            text = pwMissingMessage,
            onClick = onForgotPasswordClick
        )
    }

    Spacer(Modifier.height(18.dp))

    AuthPrimaryButton(
        text = if (loading) loginLoading else loginText,
        onClick = onLoginClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = !loading
    )

    Spacer(Modifier.height(8.dp))

    AuthSecondaryButton(
        text = signupText,
        onClick = onSignUpClick,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(30.dp))
}