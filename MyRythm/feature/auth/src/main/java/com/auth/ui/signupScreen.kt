package com.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auth.viewmodel.AuthViewModel
import com.domain.model.SignupRequest
import com.shared.R
import com.shared.ui.components.AuthActionButton
import com.shared.ui.components.AuthGenderDropdown
import com.shared.ui.components.AuthInputField
import com.shared.ui.components.AuthLogoIcon
import com.shared.ui.components.AuthPrimaryButton
import com.shared.ui.components.AuthSecondaryButton
import com.shared.ui.components.AuthSectionTitle
import com.shared.ui.components.AuthTextButton
import com.shared.ui.theme.AuthBackground
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onSignupComplete: () -> Unit = {},
    onBackToLogin: () -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    var birthYear by rememberSaveable { mutableStateOf("") }
    var birthMonth by rememberSaveable { mutableStateOf("") }
    var birthDay by rememberSaveable { mutableStateOf("") }

    var height by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

    var gender by rememberSaveable { mutableStateOf("") }

    var isEmailCodeSent by rememberSaveable { mutableStateOf(false) }
    var isVerificationCompleted by rememberSaveable { mutableStateOf(false) }
    var code by rememberSaveable { mutableStateOf("") }

    val ui by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    val signupComplete = stringResource(R.string.auth_signupcomplete)
    val emailText = stringResource(R.string.email)
    val nameText = stringResource(R.string.name)
    val passwordText = stringResource(R.string.auth_password)
    val birthText = stringResource(R.string.birth)
    val yearText = stringResource(R.string.year)
    val monthText = stringResource(R.string.month)
    val dayText = stringResource(R.string.day)
    val genderText = stringResource(R.string.gender)
    val heightText = stringResource(R.string.height)
    val weightText = stringResource(R.string.weight)
    val phoneNumberPlaceholderText = stringResource(R.string.phone_number_placeholder)
    val emailVerification = stringResource(R.string.email_verification)
    val sendText = stringResource(R.string.send)
    val sentText = stringResource(R.string.sent)
    val verificationText = stringResource(R.string.verification)
    val verificationCodeText = stringResource(R.string.verification_code)
    val signupLoading = stringResource(R.string.auth_signup_loading)
    val signupText = stringResource(R.string.auth_signup)
    val backText = stringResource(R.string.back)
    val backToLoginMessage = stringResource(R.string.auth_message_backtologin)

    LaunchedEffect(Unit) {
        viewModel.events.collect { msg ->
            snackbar.showSnackbar(msg)
            when {
                msg.contains("회원가입 성공") -> onSignupComplete()
                msg == "인증코드 전송" -> isEmailCodeSent = true
                msg == "인증 성공" -> isVerificationCompleted = true
            }
        }
    }

    fun validNumber(s: String) = s.toDoubleOrNull() != null

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .background(AuthBackground)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            AuthLogoIcon()

            Spacer(Modifier.height(24.dp))

            AuthSectionTitle(emailVerification)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AuthInputField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (isVerificationCompleted) {
                            isVerificationCompleted = false
                            isEmailCodeSent = false
                        }
                    },
                    hint = emailText,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Email,
                    enabled = !isVerificationCompleted
                )

                Spacer(Modifier.width(8.dp))

                AuthActionButton(
                    text = if (isEmailCodeSent) sentText else sendText,
                    onClick = {
                        if (email.isNotBlank()) {
                            viewModel.updateSignupEmail(email)
                            viewModel.sendCode()
                        }
                    },
                    enabled = !isEmailCodeSent && email.isNotBlank(),
                    modifier = Modifier.widthIn(min = 90.dp)
                )
            }

            if (isEmailCodeSent) {
                Spacer(Modifier.height(20.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AuthInputField(
                        value = code,
                        onValueChange = {
                            code = it
                            viewModel.updateCode(it)
                        },
                        hint = verificationCodeText,
                        modifier = Modifier.weight(1f),
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number,
                        enabled = !isVerificationCompleted
                    )

                    Spacer(Modifier.width(8.dp))

                    AuthActionButton(
                        text = verificationText,
                        onClick = {
                            viewModel.updateSignupEmail(email)
                            viewModel.updateCode(code)
                            viewModel.verifyCode()
                        },
                        enabled = !isVerificationCompleted && code.isNotBlank(),
                        modifier = Modifier.widthIn(min = 90.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            AuthSectionTitle(nameText)

            AuthInputField(
                value = username,
                onValueChange = { username = it },
                hint = nameText,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            AuthSectionTitle(passwordText)

            AuthInputField(
                value = password,
                onValueChange = { password = it },
                hint = passwordText,
                isPassword = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Password
            )

            Spacer(Modifier.height(12.dp))

            AuthSectionTitle(birthText)

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AuthInputField(
                    value = birthYear,
                    onValueChange = { birthYear = it.filter { c -> c.isDigit() }.take(4) },
                    hint = yearText,
                    modifier = Modifier.weight(1.5f),
                    keyboardType = KeyboardType.Number
                )
                AuthInputField(
                    value = birthMonth,
                    onValueChange = { birthMonth = it.filter { c -> c.isDigit() }.take(2) },
                    hint = monthText,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
                AuthInputField(
                    value = birthDay,
                    onValueChange = { birthDay = it.filter { c -> c.isDigit() }.take(2) },
                    hint = dayText,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
            }

            Spacer(Modifier.height(20.dp))

            AuthGenderDropdown(
                value = gender,
                onValueChange = { gender = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AuthSectionTitle(heightText)
                }
                Box(modifier = Modifier.weight(1f)) {
                    AuthSectionTitle(weightText)
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AuthInputField(
                    value = height,
                    onValueChange = { height = it },
                    hint = heightText,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
                AuthInputField(
                    value = weight,
                    onValueChange = { weight = it },
                    hint = weightText,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
            }

            Spacer(Modifier.height(12.dp))

            AuthSectionTitle(phoneNumberPlaceholderText)

            AuthInputField(
                value = phone,
                onValueChange = { phone = it },
                hint = phoneNumberPlaceholderText,
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Phone
            )

            Spacer(Modifier.height(24.dp))

            AuthPrimaryButton(
                text = if (ui.loading) signupLoading else signupText,
                onClick = {
                    val birthDate = "${birthYear}-${birthMonth.padStart(2, '0')}-${birthDay.padStart(2, '0')}"
                    val heightOk = validNumber(height)
                    val weightOk = validNumber(weight)

                    if (
                        email.isBlank() || username.isBlank() || password.isBlank() ||
                        birthYear.length != 4 || birthMonth.isBlank() || birthDay.isBlank() ||
                        !heightOk || !weightOk || phone.isBlank()
                    ) {
                        return@AuthPrimaryButton
                    }

                    if (!isVerificationCompleted) {
                        return@AuthPrimaryButton
                    }

                    val req = SignupRequest(
                        email = email,
                        username = username,
                        password = password,
                        phone = phone,
                        birthDate = birthDate,
                        gender = gender,
                        height = height.toDouble(),
                        weight = weight.toDouble()
                    )
                    viewModel.signup(req)
                },
                enabled = !ui.loading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            AuthSecondaryButton(
                text = backText,
                onClick = { onBackToLogin() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                AuthTextButton(
                    text = backToLoginMessage,
                    onClick = { onBackToLogin() }
                )
            }

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}
