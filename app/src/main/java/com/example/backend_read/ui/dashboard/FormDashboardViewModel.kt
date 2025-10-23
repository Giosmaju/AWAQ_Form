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
import java.time.format.DateTimeFormatter
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

    private val _availableUserIds = MutableStateFlow<List<String>>(emptyList())
    val availableUserIds: StateFlow<List<String>> = _availableUserIds.asStateFlow()

    private val _availableCropTypes = MutableStateFlow<List<String>>(emptyList())
    val availableCropTypes: StateFlow<List<String>> = _availableCropTypes.asStateFlow()

    private val _availableCropStatuses = MutableStateFlow<List<String>>(emptyList())
    val availableCropStatuses: StateFlow<List<String>> = _availableCropStatuses.asStateFlow()

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
                _uiState.value = UiState.Error("No autenticado. Por favor, inicie sesión.")
                return@launch
            }

            try {
                val response = apiService.getFilteredSubmissions(
                    apiKey = apiKey, tenant = tenant,
                    startDate = null, endDate = null, userId = null, cropType = null, cropStatus = null
                )

                masterSubmissions = response.data
                _availableUserIds.value = masterSubmissions.mapNotNull { it.userId?.toString() }.distinct().sorted()
                _availableCropTypes.value = masterSubmissions.map { it.cultivo }.distinct().sorted()
                _availableCropStatuses.value = masterSubmissions.mapNotNull { it.estadoFollaje }.distinct().sorted()
                
                clearFilters() // Set initial state
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al obtener los envíos: ${e.message}")
            }
        }
    }

    private fun performLocalFiltering() {
        val filters = _filterState.value
        val dashDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // YYYY-MM-DD
        val slashDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        val filteredList = masterSubmissions.filter { submission ->
            val userIdMatch = filters.selectedUserIds.isEmpty() || filters.selectedUserIds.contains(submission.userId?.toString())
            val cropTypeMatch = filters.selectedCropTypes.isEmpty() || filters.selectedCropTypes.contains(submission.cultivo)
            val cropStatusMatch = filters.selectedCropStatus.isEmpty() || submission.estadoFollaje?.let { filters.selectedCropStatus.contains(it) } ?: false

            // Defensively parse the submission date, trying multiple formats.
            val submissionDate = try {
                LocalDate.parse(submission.fechaSiembra, dashDateFormatter)
            } catch (e: DateTimeParseException) {
                try {
                    LocalDate.parse(submission.fechaSiembra, slashDateFormatter)
                } catch (e2: DateTimeParseException) {
                    null
                }
            }

            // Parse filter dates using the UI's format (YYYY-MM-DD)
            val startDate = try { filters.startDate?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it, dashDateFormatter) } } catch (e: DateTimeParseException) { null }
            val endDate = try { filters.endDate?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it, dashDateFormatter) } } catch (e: DateTimeParseException) { null }

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
