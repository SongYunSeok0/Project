package com.news.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.Favorite
import com.domain.usecase.news.AddFavoriteUseCase
import com.domain.usecase.news.GetFavoritesUseCase
import com.domain.usecase.news.RemoveFavoriteUseCase
import com.domain.usecase.news.UpdateFavoriteLastUsedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val updateFavoriteLastUsedUseCase: UpdateFavoriteLastUsedUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: String = savedStateHandle["userId"] ?: ""

    // --------------------
    // Favorites State
    // --------------------
    val favorites = getFavoritesUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    // --------------------
    // Actions
    // --------------------
    fun addFavorite(keyword: String) {
        if (keyword.isBlank()) return

        viewModelScope.launch {
            addFavoriteUseCase(
                Favorite(
                    keyword = keyword,
                    timestamp = System.currentTimeMillis(),
                    lastUsed = System.currentTimeMillis(),
                    userId = userId
                )
            )
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

    fun onFavoriteUsed(keyword: String) {
        viewModelScope.launch {
            updateFavoriteLastUsedUseCase(keyword)
        }
    }
}
