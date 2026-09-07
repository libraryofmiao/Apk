package com.library.membership.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.library.membership.api.ApiClient
import com.library.membership.api.Member
import com.library.membership.api.StatusUpdateRequest
import com.library.membership.util.ImageUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val GENDER_OPTIONS = listOf("Male", "Female", "Other")
private val MEMBERSHIP_TYPE_OPTIONS = listOf("Student", "General", "Senior Citizen", "Research Scholar", "Faculty")
private val DURATION_OPTIONS = listOf("Permanent ( Paid )", "Permanent ( Free of Cost )", "New Age Learning Centre")
private val ID_TYPE_OPTIONS = listOf("Aadhaar Card", "Voter ID", "PAN Card", "Driving License", "Passport", "Govt. ID")

private val FREE_TEXT_FIELDS = listOf(
    "fullName", "guardian", "dob", "occupation", "address",
    "district", "state", "pincode", "mobile", "email", "idNumber"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(memberId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    var member by remember { mutableStateOf<Member?>(null) }
    var fields by remember { mutableStateOf(mutableMapOf<String, String>()) }
    var gender by remember { mutableStateOf("") }
    var membershipType by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var idType by remember { mutableStateOf("") }
    var newPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) newPhotoUri = uri }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                val res = ApiClient.service.getMember(memberId)
                if (res.success && res.member != null) {
                    member = res.member
                    val m = res.member
                    fields = mutableMapOf(
                        "fullName" to (m.fullName ?: ""), "guardian" to (m.guardian ?: ""),
                        "dob" to (m.dob ?: ""), "occupation" to (m.occupation ?: ""),
                        "address" to (m.address ?: ""), "district" to (m.district ?: ""),
                        "state" to (m.state ?: ""), "pincode" to (m.pincode ?: ""),
                        "mobile" to (m.mobile ?: ""), "email" to (m.email ?: ""),
                        "idNumber" to (m.idNumber ?: "")
                    )
                    gender = m.gender ?: ""
                    membershipType = m.membershipType ?: ""
                    duration = m.duration ?: ""
                    idType = m.idType ?: ""
                    newPhotoUri = null
                } else {
                    error = res.message ?: "Member not found"
                }
            } catch (e: Exception) {
                error = e.message ?: "Network error"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(memberId) { load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(memberId) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when {
                loading -> CircularProgressIndicator()
                error != null -> Text("Error: $error", color = MaterialTheme.colorScheme.error)
                member != null -> {
                    val m = member!!

                    // --- Photo ---
                    val displayUrl = newPhotoUri ?: ApiClient.photoUrl(memberId, m.verify, m.photoKey)
                    if (displayUrl != null) {
                        AsyncImage(
                            model = displayUrl,
                            contentDescription = "Member photo",
                            modifier = Modifier.size(120.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    OutlinedButton(
                        onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) { Text(if (newPhotoUri == null) "Change photo" else "Photo selected — will replace current") }

                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text(
                            "Status: ${m.status}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(onClick = {
                            scope.launch {
                                saving = true
                                try {
                                    val newStatus = if (m.status == "Active") "Inactive" else "Active"
                                    val res = ApiClient.service.updateStatus(StatusUpdateRequest(memberId, newStatus))
                                    info = res.message
                                    load()
                                } catch (e: Exception) {
                                    error = e.message
                                } finally {
                                    saving = false
                                }
                            }
                        }) {
                            Text(if (m.status == "Active") "Deactivate" else "Activate")
                        }
                    }

                    FREE_TEXT_FIELDS.forEach { key ->
                        OutlinedTextField(
                            value = fields[key] ?: "",
                            onValueChange = { fields = (fields.toMutableMap().apply { put(key, it) }) },
                            label = { Text(key) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                    }

                    DropdownField("Gender", GENDER_OPTIONS, gender, { gender = it }, Modifier.padding(vertical = 4.dp))
                    DropdownField("Membership Type", MEMBERSHIP_TYPE_OPTIONS, membershipType, { membershipType = it }, Modifier.padding(vertical = 4.dp))
                    DropdownField("Membership Duration", DURATION_OPTIONS, duration, { duration = it }, Modifier.padding(vertical = 4.dp))
                    DropdownField("ID Proof Type", ID_TYPE_OPTIONS, idType, { idType = it }, Modifier.padding(vertical = 4.dp))

                    Text(
                        "Note: saving any change generates a new verification code and QR for this member.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    if (info != null) {
                        Text(info!!, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp))
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    saving = true
                                    error = null
                                    try {
                                        val photoDataUri = newPhotoUri?.let { uri ->
                                            withContext(Dispatchers.Default) { ImageUtil.uriToJpegDataUri(context, uri) }
                                        }
                                        val body = mutableMapOf<String, Any?>("memberId" to memberId)
                                        body.putAll(fields)
                                        body["gender"] = gender
                                        body["membershipType"] = membershipType
                                        body["duration"] = duration
                                        body["idType"] = idType
                                        if (photoDataUri != null) body["photoUrl"] = photoDataUri
                                        val res = ApiClient.service.updateMember(body)
                                        info = res.message ?: "Updated"
                                        load()
                                    } catch (e: Exception) {
                                        error = e.message
                                    } finally {
                                        saving = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) { Text(if (saving) "Saving..." else "Save changes") }

                        androidx.compose.foundation.layout.Spacer(Modifier.padding(4.dp))

                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.height(48.dp)
                        ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete member?") },
            text = { Text("This permanently removes $memberId. This cannot be undone.") },
            confirmButton = {
                Button(onClick = {
                    showDeleteConfirm = false
                    scope.launch {
                        try {
                            ApiClient.service.deleteMember(memberId)
                            onBack()
                        } catch (e: Exception) {
                            error = e.message
                        }
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
