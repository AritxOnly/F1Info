package com.aritxonly.f1info.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aritxonly.f1info.data.F1Repository
import com.aritxonly.f1info.model.*
import com.aritxonly.f1info.widgets.refreshF1Widgets
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class F1State(val data: SeasonData? = null, val loading: Boolean = true,
    val cached: Boolean = false, val error: String? = null, val favorites: Favorites = Favorites())
class F1ViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = F1Repository(application)
    private val mutableState = MutableStateFlow(F1State(favorites = repository.favorites()))
    val state = mutableState.asStateFlow()
    private var refreshing = false
    init { viewModelScope.launch {
        val cached = repository.cached()
        if (cached != null) mutableState.update { it.copy(data = cached, cached = true) }
        if (mutableState.value.data == null && !refreshing) refresh()
    } }
    fun refresh() {
        if (refreshing) return
        refreshing = true
        mutableState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val data = repository.fetch()
                mutableState.update { it.copy(data = data, loading = false, cached = false, error = null) }
                runCatching { refreshF1Widgets(getApplication()) }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) {
                android.util.Log.w("F1Info", "Season update failed", error)
                mutableState.update { it.copy(loading = false, error = "暂时无法更新。请检查网络后重试。", cached = it.data != null) }
            } finally { refreshing = false }
        }
    }
    fun saveFavorites(favorites: Favorites) {
        repository.saveFavorites(favorites)
        mutableState.update { it.copy(favorites = favorites) }
        viewModelScope.launch { runCatching { refreshF1Widgets(getApplication()) } }
    }
}
