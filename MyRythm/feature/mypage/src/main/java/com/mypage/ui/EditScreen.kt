package com.mypage.ui

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.mypage.viewmodel.EditProfileEvent
import com.mypage.viewmodel.EditProfileViewModel
import com.mypage.viewmodel.MyPageViewModel
import com.shared.R

@Composable
fun EditScreen(
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {},
    viewModel: EditProfileViewModel = hiltViewModel(),
    myPageVm: MyPageViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val context = LocalContext.current

    val savedMessage = stringResource(R.string.mypage_message_saved)
    val saveFailedText = stringResource(R.string.save_failed)
    val errorprofileLoadFailed = stringResource(R.string.error_profile_load_failed)

    // 이벤트 처리
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                EditProfileEvent.SaveSuccess -> {
                    myPageVm.refreshProfile()
                    Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
                    onDone()
                }
                EditProfileEvent.SaveFailed -> {
                    Toast.makeText(context, saveFailedText, Toast.LENGTH_SHORT).show()
                }
                EditProfileEvent.LoadFailed -> {
                    Toast.makeText(context, errorprofileLoadFailed, Toast.LENGTH_SHORT).show()
                }
                EditProfileEvent.EmailSent -> {
                    // 이벤트는 유지하되, 타이머 로직은 개별 처리
                }
                is EditProfileEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 🔥 상태 호이스팅: UI 레이어(EditContent)로 순수 데이터만 전달
    EditContent(
        modifier = modifier,
        initialName = profile?.username ?: "",
        initialHeight = profile?.height?.toString() ?: "",
        initialWeight = profile?.weight?.toString() ?: "",
        initialBirthDate = profile?.birth_date ?: "",
        initialPhone = profile?.phone ?: "",
        initialGender = profile?.gender ?: "",
        initialProtEmail = profile?.prot_email ?: "",
        initialProtName = profile?.prot_name ?: "",
        initialEmail = profile?.email ?: "",
        onSave = { name, h, w, age, email, ph, pEmail, pName, g ->
            viewModel.saveProfile(name, h, w, age, email, ph, pEmail, pName, g)
        },
        sendEmailCode = { email, name -> viewModel.sendEmailCode(email, name) },
        verifyEmailCode = { email, code, onResult -> viewModel.verifyEmailCode(email, code, onResult) },
        checkEmailDuplicate = { email, onResult -> viewModel.checkEmailDuplicate(email, onResult) }
    )
}