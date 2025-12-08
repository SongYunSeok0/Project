package com.chatbot.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chatbot.viewmodel.ChatbotViewModel
import com.chatbot.viewmodel.ChatbotViewModelFactory
import com.shared.R
import com.shared.ui.components.*
import com.shared.ui.theme.componentTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val viewModel: ChatbotViewModel = viewModel(factory = ChatbotViewModelFactory(context))
    val state by viewModel.state.collectAsStateWithLifecycle()

    val sendText = stringResource(R.string.send)
    val errorText = stringResource(R.string.error)
    val exampleText = stringResource(R.string.chat_example)
    val sideEffectReportedText = stringResource(R.string.sideeffectreported)
    val promptStartMessage = stringResource(R.string.chatbot_message_prompt_start)
    val promptQuestionMessage = stringResource(R.string.chatbot_message_prompt_question)
    val contentMessage = stringResource(R.string.chatbot_message_content)
    val answerLoadingMessage = stringResource(R.string.chatbot_message_answer_loading)

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size, state.loading) {
        coroutineScope.launch {
            listState.animateScrollToItem(Int.MAX_VALUE)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                item {
                    ChatbotHeader(onResetClick = { viewModel.clearAllMessages() })
                    Spacer(Modifier.height(12.dp))
                    Divider(
                        color = MaterialTheme.componentTheme.dividerColor,
                        thickness = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                }

                item {
                    AppMessageCard(text = promptStartMessage, isUser = false)
                    Spacer(Modifier.height(8.dp))
                    AppMessageCard(text = sideEffectReportedText, isUser = false) {
                        Text(
                            exampleText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }

                items(state.messages.size) { index ->
                    val msg = state.messages[index]
                    AppMessageCard(
                        text = msg.text,
                        isUser = msg.isUser,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .widthIn(max = 280.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }

                if (state.loading) {
                    item {
                        AppMessageCard(
                            text = answerLoadingMessage,
                            textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            isUser = false,
                            alpha = 0.7f
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (!state.loading && state.messages.isEmpty()) {
                    item {
                        Text(
                            promptQuestionMessage,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                if (state.error != null) {
                    item {
                        Text(
                            "$errorText ${state.error}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // 입력창
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 12.dp,
                        bottom = 35.dp
                    )
            ) {
                AppInputField(
                    value = state.input,
                    onValueChange = { viewModel.onQuestionChange(it) },
                    label = contentMessage,
                    modifier = Modifier.weight(1f),
                    imeAction = ImeAction.Send,
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (state.input.isNotBlank() && !state.loading) {
                                viewModel.send()
                            }
                        }
                    ),
                    trailingContent = {
                        AppButton(
                            isCircle = true,
                            width = 44.dp,
                            height = 44.dp,
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                if (!state.loading && state.input.isNotBlank()) {
                                    viewModel.send()
                                }
                            }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.send),
                                contentDescription = sendText
                            )
                        }
                    }
                )
            }
        }
    }
}
