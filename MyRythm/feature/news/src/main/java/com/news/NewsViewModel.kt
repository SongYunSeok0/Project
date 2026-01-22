package com.news

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.domain.model.Favorite
import com.domain.model.News
import com.domain.usecase.news.AddFavoriteUseCase
import com.domain.usecase.news.GetFavoritesUseCase
import com.domain.usecase.news.RemoveFavoriteUseCase
import com.domain.usecase.news.UpdateFavoriteLastUsedUseCase
import com.news.utils.NewsPagingFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsPagingFactory: NewsPagingFactory, // ✅ GetNewsUseCase 대신
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val updateFavoriteLastUsedUseCase: UpdateFavoriteLastUsedUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: String = savedStateHandle["userId"] ?: ""

    // 뉴스 검색
    private val _selectedCategory = MutableStateFlow("건강")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode: StateFlow<Boolean> = _isSearchMode.asStateFlow()

    // ✅ 뉴스 Paging Flow: factory로 생성
    val newsPagingFlow: Flow<PagingData<News>> =
        selectedCategory
            .flatMapLatest { category -> newsPagingFactory.create(category) }
            .cachedIn(viewModelScope)

    // Room에서 flow로 가져온 즐겨찾기 리스트
    val favorites = getFavoritesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addFavorite(keyword: String) {
        if (keyword.isBlank()) return

        viewModelScope.launch {
            val favorite = Favorite(
                keyword = keyword,
                timestamp = System.currentTimeMillis(),
                lastUsed = System.currentTimeMillis(),
                userId = userId
            )
            addFavoriteUseCase(favorite)
        }
    }

    fun removeFavorite(keyword: String) {
        viewModelScope.launch {
            removeFavoriteUseCase(keyword, userId)
        }
    }

    fun isFavorite(keyword: String): Boolean {
        return favorites.value.any { it.keyword == keyword }
    }

    fun onFavoriteClick(keyword: String) {
        _searchQuery.value = keyword
        _selectedCategory.value = keyword

        viewModelScope.launch {
            updateFavoriteLastUsedUseCase(keyword)
        }
    }

    fun updateCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun updateSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun triggerSearch() {
        if (_searchQuery.value.isNotBlank()) {
            _selectedCategory.value = _searchQuery.value
        }
    }

    fun openSearch() { _isSearchMode.value = true }
    fun closeSearch() { _isSearchMode.value = false }
}
