package com.mypage.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domain.model.Plan
import com.domain.model.RegiHistoryWithPlans
import com.domain.model.User
import com.mypage.viewmodel.StaffManagementViewModel
import com.shared.R
import com.shared.bar.AppTopBar
import com.shared.ui.components.AppIconButton
import com.shared.ui.components.AppInputField
import com.shared.ui.theme.AppTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    viewModel: StaffManagementViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val userMediRecordText = stringResource(R.string.user_medi_record)
    val userManagementText = stringResource(R.string.user_management)

    val users by viewModel.filteredUsers.collectAsState()
    val selectedUser by viewModel.selectedUser.collectAsState()
    val userRegiHistories by viewModel.userRegiHistories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    // 🔙 하드웨어 뒤로가기 처리
    BackHandler {
        if (selectedUser != null) {
            // 복약 상세 → 사용자 목록으로
            viewModel.backToUserList()
        } else {
            // 사용자 목록 → 마이페이지(상위 화면)로
            onBackClick()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast
                .makeText(context, it.toMessage(context), Toast.LENGTH_SHORT)
                .show()
            viewModel.clearError()
        }
    }


    // 화면 진입 시 사용자 목록 로드
    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    AppTheme {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = if (selectedUser != null)
                        "${selectedUser?.username}$userMediRecordText"
                    else
                        userManagementText,
                    showBack = true,
                    onBackClick = {
                        if (selectedUser != null) {
                            viewModel.backToUserList()
                        } else {
                            onBackClick()
                        }
                    },
                    showSearch = false
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    when (selectedUser) {
                        null -> {
                            // 사용자 목록
                            SearchBar(
                                query = searchQuery,
                                onQueryChange = { viewModel.updateSearchQuery(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                            UsersListContent(
                                users = users,
                                onUserClick = { viewModel.selectUser(it) }
                            )
                        }
                        else -> {
                            // 선택된 사용자의 복약 기록
                            UserRegiHistoriesContent(
                                user = selectedUser!!,
                                regiHistories = userRegiHistories
                            )
                        }
                    }
                }
            }
        }
    }
}

// 🔥 검색창
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchBarText = stringResource(R.string.searchbar)
    val searchText = stringResource(R.string.search)
    val clearText = stringResource(R.string.clear)

    AppInputField(
        value = query,
        onValueChange = onQueryChange,
        label = searchBarText,
        singleLine = true,
        modifier = modifier
            .fillMaxWidth(),
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = searchText,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            if (query.isNotEmpty()) {
                AppIconButton(
                    onClick = { onQueryChange("") },
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    icon = {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = clearText
                        )
                    }
                )
            }
        }
    )
}

// 🔥 사용자 목록
@Composable
private fun UsersListContent(
    users: List<User>,
    onUserClick: (User) -> Unit
) {
    val noUsersMessage = stringResource(R.string.mypage_message_no_users)

    if (users.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = noUsersMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(users) { user ->
                UserCard(
                    user = user,
                    onClick = { onUserClick(user) }
                )
            }
        }
    }
}

// 🔥 사용자 카드
@Composable
private fun UserCard(
    user: User,
    onClick: () -> Unit
) {
    val labelUserText = stringResource(R.string.label_user)
    val labelAdminText = stringResource(R.string.label_admin)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.2f)
        ),
        //elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), //테두리그림자설정
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.username ?: labelUserText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (user.isStaff == true) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = labelAdminText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                user.email?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                user.phone?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (user.isActive == true)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
            )
        }
    }
}

// 🔥 사용자의 복약 기록 목록
@Composable
private fun UserRegiHistoriesContent(
    user: User,
    regiHistories: List<RegiHistoryWithPlans>
) {
    val noRegiHistoryMessage = stringResource(R.string.mypage_message_no_regi_history)

    if (regiHistories.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = noRegiHistoryMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(regiHistories) { regiHistory ->
                RegiHistoryCard(regiHistory = regiHistory)
            }
        }
    }
}

// 🔥 복약 기록 카드
@Composable
private fun RegiHistoryCard(
    regiHistory: RegiHistoryWithPlans
) {
    val collapseText = stringResource(R.string.collapse)
    val expandText = stringResource(R.string.expand)
    val planCountText = stringResource(R.string.plan_count)
    val issueDateText = stringResource(R.string.issued_date)
    val alarmText = stringResource(R.string.alarm)
    val onText = stringResource(R.string.on)
    val offText = stringResource(R.string.off)
    val medScheduleText = stringResource(R.string.med_schedule)

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.2f)
        ),
        //elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), //테두리그림자설정
        shape = MaterialTheme.shapes.medium    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = regiHistory.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = regiHistory.regiType,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${regiHistory.planCount}$planCountText",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) collapseText else expandText
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                regiHistory.issuedDate?.let {
                    DetailRow(label = issueDateText, value = it)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                DetailRow(
                    label = alarmText,
                    value = if (regiHistory.useAlarm) onText else offText
                )

                if (regiHistory.plans.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = medScheduleText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    regiHistory.plans.forEach { plan ->
                        PlanItem(plan = plan)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

// 🔥 Plan 아이템
@Composable
private fun PlanItem(
    plan: com.domain.model.Plan
) {
    val mealBeforeText = stringResource(R.string.meal_relation_before)
    val mealAfterText = stringResource(R.string.meal_relation_after)
    val mealWithText = stringResource(R.string.meal_with)
    val planDoseTimeText = stringResource(R.string.plan_dose_time)
    val doseTypeText = stringResource(R.string.dose_type)
    val noteLabel = stringResource(R.string.note_label)
    val doseCompleteCheckText = stringResource(R.string.dose_complete_check)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f)
        ),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = plan.medName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            plan.takenAt?.let { timestamp ->
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val dateTime = Instant.ofEpochMilli(timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()

                Text(
                    text = "$planDoseTimeText ${dateTime.format(formatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            plan.mealTime?.let {
                val mealTimeText = when (it) {
                    "before" -> mealBeforeText
                    "after" -> mealAfterText
                    "with" -> mealWithText
                    else -> it
                }
                Text(
                    text = "$doseTypeText $mealTimeText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            plan.note?.let {
                Text(
                    text = "$noteLabel $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (plan.taken != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = doseCompleteCheckText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// 🔥 상세 정보 행
@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}