package com.mypage.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.shared.R
import android.net.Uri
import androidx.hilt.navigation.compose.hiltViewModel
import com.mypage.viewmodel.MyPageEvent
import com.mypage.viewmodel.MyPageViewModel
import com.shared.ui.components.AppInputField
import com.shared.ui.components.AppSelectableButton

@Composable
fun InquiryTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    val inquiryTypeText = stringResource(R.string.inquirytype)

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = inquiryTypeText,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                stringResource(R.string.general_inquiry),
                stringResource(R.string.bug_report)
            ).forEach { type ->
                val isSelected = selectedType == type

                AppSelectableButton(
                    text = type,
                    selected = isSelected,
                    onClick = { onTypeSelected(type) },
                    modifier = Modifier.weight(1f),
                    height = 50.dp,
                    useClickEffect = true
                )
            }
        }
    }
}

@Composable
fun NewInquiryForm(
    viewModel: MyPageViewModel = hiltViewModel()
) {
    var selectedType by remember { mutableStateOf("일반 문의") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var images by remember { mutableStateOf(listOf<Uri>()) }

    val titleText = stringResource(R.string.title)
    val title_Message = stringResource(R.string.mypage_message_title)
    val contentText = stringResource(R.string.content)
    val content_Message = stringResource(R.string.mypage_message_content)
    val inquirySubmittedSuccessMessage = stringResource(R.string.message_inquiry_submitted_success)

    val context = LocalContext.current

    // 🔥 ViewModel 이벤트 수신
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MyPageEvent.InquirySubmitSuccess -> {
                    Toast.makeText(context, inquirySubmittedSuccessMessage, Toast.LENGTH_SHORT).show()
                }

                is MyPageEvent.InquirySubmitFailed -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                else -> Unit
            }
        }
    }

    Column {
        InquiryTypeSelector(
            selectedType = selectedType,
            onTypeSelected = { selectedType = it }
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {

            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium, // AppTheme 기반 폰트 적용
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start)
            )
            AppInputField(
                value = title,
                onValueChange = { title = it },
                label = title_Message,
                singleLine = true,
                maxLines = 1,
                useFloatingLabel = true
            )

            Text(
                text = contentText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start)
            )
            AppInputField(
                value = content,
                onValueChange = { content = it },
                label = content_Message,
                height = 150.dp,
                useFloatingLabel = true
            )

            // 이미지 첨부 (3번 ImageAttachmentSection 컴포넌트) -> 1201
            // 이미지첨부는되는데 서버로전달은안됨
            ImageAttachmentSection(
                images = images,
                onImagesSelected = { newImages ->
                    images = newImages
                },
                onImageRemove = { index ->
                    images = images.filterIndexed { i, _ -> i != index }
                }
            )

            SubmitButton {
                if (title.isNotBlank() && content.isNotBlank()) {
                    viewModel.addInquiry(
                        type = selectedType,
                        title = title,
                        content = content
                    )

                    // 입력 초기화
                    title = ""
                    content = ""
                }
            }
        }
    }
}

@Composable
fun SubmitButton(
    onClick: () -> Unit
) {
    val inquiryText = stringResource(R.string.inquiry)
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable { onClick() }
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = inquiryText,
            color = Color.White,
            fontSize = 16.sp,
            lineHeight = 1.5.em
        )
    }
}