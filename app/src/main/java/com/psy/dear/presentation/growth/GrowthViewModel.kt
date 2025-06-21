package com.psy.dear.presentation.growth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.dear.core.Result
import com.psy.dear.domain.model.GrowthStatistics
import com.psy.dear.domain.use_case.journal.GetGrowthStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GrowthState(
    val stats: GrowthStatistics? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GrowthViewModel @Inject constructor(
    private val getGrowthStatisticsUseCase: GetGrowthStatisticsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GrowthState())
    val state = _state.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _state.value = GrowthState(isLoading = true)
            when (val result = getGrowthStatisticsUseCase()) {
                is Result.Success -> _state.value = GrowthState(stats = result.data)
                is Result.Error -> _state.value = GrowthState(error = result.exception?.message)
                else -> {}
            }
        }
    }
}
