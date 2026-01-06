package com.news.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.domain.model.News
import com.domain.usecase.news.GetNewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val getNewsUseCase: GetNewsUseCase
) : ViewModel() {

    // --------------------
    // UI State
    // --------------------
    private val _selectedCategory = MutableStateFlow("건강")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode: StateFlow<Boolean> = _isSearchMode.asStateFlow()

    // --------------------
    // News Paging
    // --------------------
    val newsPagingFlow: Flow<PagingData<News>> =
        selectedCategory
            .flatMapLatest { category ->
                getNewsUseCase(category)
            }
            .cachedIn(viewModelScope)

    // --------------------
    // UI Actions
    // --------------------
    fun updateCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun triggerSearch() {
        if (_searchQuery.value.isNotBlank()) {
            _selectedCategory.value = _searchQuery.value
        }
    }

    fun openSearch() {
        _isSearchMode.value = true
    }

    fun closeSearch() {
        _isSearchMode.value = false
    }
}
