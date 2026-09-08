package com.libraryofmiao.membership.ui.register

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.libraryofmiao.membership.data.model.Member
import com.libraryofmiao.membership.data.model.OptionSets
import com.libraryofmiao.membership.data.network.ApiClient
import com.libraryofmiao.membership.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

data class RegisterUiState(
    val isEditMode: Boolean = false,
    val member: Member = Member(),
    val photoUri: Uri? = null,
    val options: OptionSets = OptionSets(),
    val submitting: Boolean = false,
    val submitted: Boolean = false,
    val declarationChecked: Boolean = false,
    val error: String? = null
)

class RegisterViewModel(app: Application) : AndroidViewModel(app) {

    private val session = SessionManager(app)
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun loadForEdit(memberId: String) {
        _uiState.value = _uiState.value.copy(isEditMode = true)
        viewModelScope.launch {
            try {
                val response = ApiClient.membershipApi.getMember(memberId)
                response.body()?.member?.let { m ->
                    _uiState.value = _uiState.value.copy(member = m)
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(error = "Unable to load member for editing")
            }
        }
    }

    fun updateField(update: (Member) -> Member) {
        _uiState.value = _uiState.value.copy(member = update(_uiState.value.member))
    }

    fun onPhotoPicked(uri: Uri) {
        _uiState.value = _uiState.value.copy(photoUri = uri)
    }

    fun onDeclarationChanged(checked: Boolean) {
        _uiState.value = _uiState.value.copy(declarationChecked = checked)
    }

    fun reset() {
        _uiState.value = RegisterUiState(isEditMode = _uiState.value.isEditMode)
    }

    /** Mirrors register.html's "Manage Options" panel -> POST /api/options (admin-key gated). */
    fun addOption(field: String, value: String, adminKey: String, onResult: (Boolean, String?) -> Unit) {
        val current = _uiState.value.options
        val updated = when (field) {
            "membershipTypes" -> current.copy(membershipTypes = current.membershipTypes + value)
            "membershipDurations" -> current.copy(membershipDurations = current.membershipDurations + value)
            "idTypes" -> current.copy(idTypes = current.idTypes + value)
            else -> current
        }
        viewModelScope.launch {
            try {
                val response = ApiClient.membershipApi.updateOptions(adminKey, updated)
                if (response.isSuccessful && response.body()?.success == true) {
                    session.setOptionsAdminKey(adminKey)
                    _uiState.value = _uiState.value.copy(options = updated)
                    onResult(true, null)
                } else {
                    onResult(false, response.body()?.error ?: "Failed to save option")
                }
            } catch (e: Exception) {
                onResult(false, "Server connection error")
            }
        }
    }

    fun submit(onSuccess: () -> Unit) {
        val m = _uiState.value.member
        if (m.fullName.isBlank() || m.mobile.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please fill all required fields")
            return
        }
        _uiState.value = _uiState.value.copy(submitting = true, error = null)

        viewModelScope.launch {
            try {
                val fields = buildFieldMap(m)
                val photoPart = _uiState.value.photoUri?.let { uri ->
                    uriToMultipart(uri)
                }
                val response = if (_uiState.value.isEditMode) {
                    ApiClient.membershipApi.updateMember(fields, photoPart)
                } else {
                    ApiClient.membershipApi.register(fields, photoPart)
                }
                if (response.isSuccessful && response.body()?.success != false) {
                    _uiState.value = _uiState.value.copy(submitting = false, submitted = true)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        submitting = false,
                        error = response.body()?.error ?: "Submission failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(submitting = false, error = "Server connection error")
            }
        }
    }

    private fun buildFieldMap(m: Member): Map<String, okhttp3.RequestBody> {
        val map = mutableMapOf<String, okhttp3.RequestBody>()
        fun put(key: String, value: String?) {
            if (!value.isNullOrBlank()) map[key] = value.toRequestBody("text/plain".toMediaTypeOrNull())
        }
        m.memberId?.let { put("memberId", it) }
        put("fullName", m.fullName)
        put("guardianName", m.guardianName)
        put("gender", m.gender)
        put("dob", m.dob)
        put("occupation", m.occupation)
        put("address", m.address)
        put("district", m.district)
        put("state", m.state)
        put("pincode", m.pincode)
        put("mobile", m.mobile)
        put("email", m.email)
        put("membershipType", m.membershipType)
        put("membershipDuration", m.membershipDuration)
        put("idType", m.idType)
        put("idNumber", m.idNumber)
        put("status", m.status)
        return map
    }

    private fun uriToMultipart(uri: Uri): MultipartBody.Part? {
        val context = getApplication<Application>()
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("member_photo", ".jpg", context.cacheDir)
        tempFile.outputStream().use { out -> inputStream.copyTo(out) }
        val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("photo", tempFile.name, requestFile)
    }
}
