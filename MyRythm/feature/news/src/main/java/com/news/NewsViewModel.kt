package com.news

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.domain.model.Favorite
import com.domain.model.News
import com.domain.repository.FavoriteRepository
import com.domain.usecase.GetNewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val getNewsUseCase: GetNewsUseCase,
    private val favoriteRepository: FavoriteRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: String = savedStateHandle["userId"] ?: ""
    //  뉴스 검색
    private val _selectedCategory = MutableStateFlow("건강")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode: StateFlow<Boolean> = _isSearchMode.asStateFlow()

    // 뉴스 Paging Flow
    val newsPagingFlow: Flow<PagingData<News>> =
        selectedCategory
            .flatMapLatest { category ->
                getNewsUseCase(category)
            }
            .cachedIn(viewModelScope)

    // Room에서 flow로 가져온 즐겨찾기 리스트
    val favorites: StateFlow<List<Favorite>> =
        favoriteRepository.getFavorites()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    //  즐겨찾기 관련 함수

    fun addFavorite(keyword: String) {
        if (keyword.isBlank()) return

        viewModelScope.launch {
            val favorite = Favorite(
                keyword = keyword,
                timestamp = System.currentTimeMillis(),
                lastUsed = System.currentTimeMillis(),
                userId = userId
            )
            favoriteRepository.insertFavorite(favorite)
        }
    }

    fun removeFavorite(keyword: String) {
        viewModelScope.launch {
            favoriteRepository.deleteFavorite(keyword,userId)
        }
    }

    fun isFavorite(keyword: String): Boolean {
        return favorites.value.any { it.keyword == keyword }
    }

    fun onFavoriteClick(keyword: String) {
        // 즐겨찾기 키워드 검색하기
        _searchQuery.value = keyword
        _selectedCategory.value = keyword

        // 마지막 사용시간 업데이트
        viewModelScope.launch {
            favoriteRepository.updateLastUsed(keyword)
        }
    }

    //  UI 검색 액션 처리

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

    //  기사 썸네일 추출 (Jsoup)
    private suspend fun fetchThumbnail(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(3000)
                .get()

            val metaTag = doc.select("meta[property=og:image]").attr("content")

            if (metaTag.isNotEmpty()) metaTag else null
        } catch (e: Exception) {
            null
        }
    }
}
