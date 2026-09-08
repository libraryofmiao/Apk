package com.libraryofmiao.membership.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.libraryofmiao.membership.data.model.MemberSummary
import com.libraryofmiao.membership.data.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val loading: Boolean = true,
    val members: List<MemberSummary> = emptyList(),
    val total: Int = 0,
    val query: String = "",
    val error: String? = null
) {
    /** Same client-side filter logic as dashboard.html's search box. */
    val filtered: List<MemberSummary>
        get() = if (query.isBlank()) members else members.filter {
            listOfNotNull(it.fullName, it.memberId, it.mobile, it.email)
                .any { field -> field.contains(query, ignoreCase = true) }
        }
}

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadMembers()
    }

    fun onQueryChanged(q: String) {
        _uiState.value = _uiState.value.copy(query = q)
    }

    fun loadMembers() {
        _uiState.value = _uiState.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val response = ApiClient.membershipApi.listMembers()
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        members = body.members,
                        total = body.stats?.total ?: body.members.size
                    )
                } else {
                    _uiState.value = _uiState.value.copy(loading = false, error = "Unable to load member data")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, error = "Unable to load member data")
            }
        }
    }

    fun deleteMember(memberId: String) {
        viewModelScope.launch {
            try {
                ApiClient.membershipApi.deleteMember(memberId)
                loadMembers()
            } catch (_: Exception) { /* surfaced via next load's error state */ }
        }
    }

    fun toggleStatus(memberId: String, currentStatus: String) {
        val newStatus = if (currentStatus.equals("Active", true)) "Inactive" else "Active"
        viewModelScope.launch {
            try {
                ApiClient.membershipApi.updateStatus(mapOf("memberId" to memberId, "status" to newStatus))
                loadMembers()
            } catch (_: Exception) { }
        }
    }
}
