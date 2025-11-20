package com.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.domain.model.News
import com.domain.usecase.GetNewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val getNewsUseCase: GetNewsUseCase
) : ViewModel() {

    // 🔥 카테고리 상태
    private val _selectedCategory = MutableStateFlow("건강")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // 🔥 검색어
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 🔥 검색 모드 여부
    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode: StateFlow<Boolean> = _isSearchMode.asStateFlow()

    // 🔥 PagingData 흐름 (핵심)
    val newsPagingFlow: Flow<PagingData<News>> =
        selectedCategory
            .flatMapLatest { category ->
                getNewsUseCase(category)
            }
            .cachedIn(viewModelScope)

    // -------------------
    // 🔥 UI 액션
    // -------------------

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
