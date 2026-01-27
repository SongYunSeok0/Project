package com.mypage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.ApiResult
import com.domain.model.Inquiry
import com.domain.model.InquiryComment
import com.domain.model.RegiHistoryWithPlans
import com.domain.model.User
import com.domain.usecase.inquiry.AddCommentUseCase
import com.domain.usecase.inquiry.GetAllInquiriesUseCase
import com.domain.usecase.inquiry.GetCommentsUseCase
import com.domain.usecase.regi.GetUserRegiHistoriesUseCase
import com.domain.usecase.user.GetAllUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mypage.ui.toUiError
import com.shared.model.UiError


@HiltViewModel
class StaffManagementViewModel @Inject constructor(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val getAllInquiriesUseCase: GetAllInquiriesUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val getUserRegiHistoriesUseCase: GetUserRegiHistoriesUseCase
) : ViewModel() {

    // 선택된 탭
    private val _selectedTab = MutableStateFlow(StaffTab.USERS)
    val selectedTab: StateFlow<StaffTab> = _selectedTab.asStateFlow()

    // 사용자 목록
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // 선택된 사용자 (복약 기록 조회용)
    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser.asStateFlow()

    // 🔥 선택된 사용자의 복약 기록 (RegiHistory + Plans)
    private val _userRegiHistories = MutableStateFlow<List<RegiHistoryWithPlans>>(emptyList())
    val userRegiHistories: StateFlow<List<RegiHistoryWithPlans>> = _userRegiHistories.asStateFlow()

    // 문의사항 목록
    private val _inquiries = MutableStateFlow<List<Inquiry>>(emptyList())
    val inquiries: StateFlow<List<Inquiry>> = _inquiries.asStateFlow()

    // 선택된 문의사항 (댓글 보기용)
    private val _selectedInquiry = MutableStateFlow<Inquiry?>(null)
    val selectedInquiry: StateFlow<Inquiry?> = _selectedInquiry.asStateFlow()

    // 선택된 문의사항의 댓글 목록
    private val _inquiryComments = MutableStateFlow<List<InquiryComment>>(emptyList())
    val inquiryComments: StateFlow<List<InquiryComment>> = _inquiryComments.asStateFlow()

    // 검색어
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 로딩
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 에러
    private val _error = MutableStateFlow<UiError?>(null)
    val error: StateFlow<UiError?> = _error.asStateFlow()

    // 댓글 작성 성공 이벤트
    private val _commentAdded = MutableSharedFlow<Boolean>()
    val commentAdded: SharedFlow<Boolean> = _commentAdded.asSharedFlow()

    // 필터된 사용자 목록
    val filteredUsers: StateFlow<List<User>> = combine(
        _users,
        _searchQuery
    ) { users, query ->
        if (query.isBlank()) {
            users
        } else {
            users.filter { user ->
                user.username?.contains(query, ignoreCase = true) == true ||
                        user.email?.contains(query, ignoreCase = true) == true ||
                        user.phone?.contains(query, ignoreCase = true) == true
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 필터된 문의사항 목록
    val filteredInquiries: StateFlow<List<Inquiry>> = combine(
        _inquiries,
        _searchQuery
    ) { inquiries, query ->
        if (query.isBlank()) {
            inquiries
        } else {
            inquiries.filter { inquiry ->
                inquiry.title.contains(query, ignoreCase = true) ||
                        inquiry.content.contains(query, ignoreCase = true) ||
                        inquiry.username?.contains(query, ignoreCase = true) == true
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadUsers()
    }

    fun selectTab(tab: StaffTab) {
        _selectedTab.value = tab
        _searchQuery.value = ""  // 탭 전환 시 검색어 초기화
        when (tab) {
            StaffTab.USERS -> {
                loadUsers()
                _selectedUser.value = null
            }
            StaffTab.INQUIRIES -> {
                loadInquiries()
                _selectedInquiry.value = null
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = getAllUsersUseCase()) {
                is ApiResult.Success<*> -> {
                    _users.value = result.data as List<User>
                }
                is ApiResult.Failure -> {
                    _error.value = result.error.toUiError()
                }
            }

            _isLoading.value = false
        }
    }


    fun selectUser(user: User) {
        _selectedUser.value = user
        loadUserRegiHistories(user.id)
    }

    fun backToUserList() {
        _selectedUser.value = null
        _userRegiHistories.value = emptyList()
    }

    // 🔥 특정 사용자의 복약 기록 로드
    private fun loadUserRegiHistories(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = getUserRegiHistoriesUseCase(userId)) {
                is ApiResult.Success -> {
                    _userRegiHistories.value = result.data
                }
                is ApiResult.Failure -> {
                    _error.value = result.error.toUiError()
                }
            }

            _isLoading.value = false
        }
    }


    // 문의사항 목록 로드
    fun loadInquiries() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = getAllInquiriesUseCase()) {
                is ApiResult.Success<*> -> {
                    _inquiries.value = result.data as List<Inquiry>
                }
                is ApiResult.Failure -> {
                    _error.value = result.error.toUiError()
                }
            }

            _isLoading.value = false
        }
    }


    // 문의사항 선택 (댓글 보기)
    fun selectInquiry(inquiry: Inquiry) {
        _selectedInquiry.value = inquiry
        loadInquiryComments(inquiry.id)
    }

    // 문의사항 목록으로 돌아가기
    fun backToInquiryList() {
        _selectedInquiry.value = null
        _inquiryComments.value = emptyList()
    }

    // 문의사항 댓글 로드
    private fun loadInquiryComments(inquiryId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = getCommentsUseCase(inquiryId)) {
                is ApiResult.Success<*> -> {
                    _inquiryComments.value = result.data as List<InquiryComment>
                }
                is ApiResult.Failure -> {
                    _error.value = result.error.toUiError()
                }
            }

            _isLoading.value = false
        }
    }


    // 댓글 작성
    fun addComment(inquiryId: Long, content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = addCommentUseCase(inquiryId, content)) {
                is ApiResult.Success<*> -> {
                    loadInquiryComments(inquiryId)
                    loadInquiries()
                    _commentAdded.emit(true)
                }
                is ApiResult.Failure -> {
                    _error.value = result.error.toUiError()
                    _commentAdded.emit(false)
                }
            }

            _isLoading.value = false
        }
    }


    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearError() {
        _error.value = null
    }
}

enum class StaffTab {
    USERS,      // 사용자 관리 (복약 기록 포함)
    INQUIRIES   // 문의사항
}