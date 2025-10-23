package com.example.backend_read.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.backend_read.data.model.FilterState
import com.example.backend_read.data.model.Submission
import com.example.backend_read.data.model.UiState
import com.example.backend_read.data.remote.ApiService
import com.example.backend_read.data.remote.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Manages the state and business logic for the form dashboard.
 * Implements local filtering with a UDF pattern.
 */
class FormDashboardViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private var masterSubmissions: List<Submission> = emptyList()

    init {
        fetchInitialSubmissions()
    }

    // --- Event Handlers for UI --- //

    fun onFilterChange(newFilterState: FilterState) {
        _filterState.value = newFilterState
    }

    fun applyFilters() {
        performLocalFiltering()
    }

    fun clearFilters() {
        _filterState.value = FilterState()
        if (masterSubmissions.isEmpty()) {
            _uiState.value = UiState.Empty
        } else {
            _uiState.value = UiState.Success(masterSubmissions)
        }
    }

    // --- Private Logic --- //

    private fun fetchInitialSubmissions() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val tenant = SessionManager.tenant
            val apiKey = SessionManager.apiKey

            if (tenant.isNullOrBlank() || apiKey.isNullOrBlank() || SessionManager.authToken.isNullOrBlank()) {
                _uiState.value = UiState.Error("Not authenticated. Please log in.")
                return@launch
            }

            try {
                val response = apiService.getFilteredSubmissions(
                    apiKey = apiKey, tenant = tenant,
                    startDate = null, endDate = null, userId = null, cropType = null, cropStatus = null
                )

                masterSubmissions = response.data
                clearFilters() // Set initial state
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error fetching submissions: ${e.message}")
            }
        }
    }

    private fun performLocalFiltering() {
        val filters = _filterState.value

        val filteredList = masterSubmissions.filter { submission ->
            val userIdMatch = filters.selectedUserId.isNullOrBlank() || submission.userId?.toString() == filters.selectedUserId
            val cropTypeMatch = filters.cropType.isNullOrBlank() || submission.cultivo.equals(filters.cropType, ignoreCase = true)
            val cropStatusMatch = filters.cropStatus.isNullOrBlank() || submission.estadoFollaje?.equals(filters.cropStatus, ignoreCase = true) ?: false

            // Safely parse dates assuming YYYY-MM-DD format, which LocalDate handles by default.
            val submissionDate = try { LocalDate.parse(submission.fechaSiembra) } catch (e: DateTimeParseException) { null }
            val startDate = try { filters.startDate?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) } } catch (e: DateTimeParseException) { null }
            val endDate = try { filters.endDate?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) } } catch (e: DateTimeParseException) { null }

            val startDateMatch = startDate == null || (submissionDate != null && (submissionDate.isAfter(startDate) || submissionDate.isEqual(startDate)))
            val endDateMatch = endDate == null || (submissionDate != null && (submissionDate.isBefore(endDate) || submissionDate.isEqual(endDate)))

            userIdMatch && cropTypeMatch && cropStatusMatch && startDateMatch && endDateMatch
        }

        if (filteredList.isEmpty()) {
            _uiState.value = UiState.Empty
        } else {
            _uiState.value = UiState.Success(filteredList)
        }
    }
}
