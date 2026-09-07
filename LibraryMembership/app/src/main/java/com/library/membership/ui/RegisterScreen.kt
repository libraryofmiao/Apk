package com.library.membership.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.library.membership.api.ApiClient
import com.library.membership.util.ImageUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Matches the live register.html exactly (DEFAULT_MEMBERSHIP_TYPES / DEFAULT_ID_TYPES
// in the Worker's GitHub-managed source, plus the fixed gender select).
private val GENDER_OPTIONS = listOf("Male", "Female", "Other")
private val MEMBERSHIP_TYPE_OPTIONS = listOf("Student", "General", "Senior Citizen", "Research Scholar", "Faculty")
private val DURATION_OPTIONS = listOf("Permanent ( Paid )", "Permanent ( Free of Cost )", "New Age Learning Centre")
private val ID_TYPE_OPTIONS = listOf("Aadhaar Card", "Voter ID", "PAN Card", "Driving License", "Passport", "Govt. ID")

private data class TextFieldSpec(val key: String, val label: String, val required: Boolean)

// Free-text fields, in the same order as the web form. Dropdowns (gender,
// membershipType, duration, idType) are rendered separately below.
private val TEXT_FIELDS = listOf(
    TextFieldSpec("fullName", "Full Name", true),
    TextFieldSpec("guardian", "Father's / Guardian's Name", false),
    TextFieldSpec("dob", "Date of Birth (YYYY-MM-DD)", true),
    TextFieldSpec("occupation", "Occupation", false),
    TextFieldSpec("address", "Address", true),
    TextFieldSpec("district", "District", false),
    TextFieldSpec("state", "State", false),
    TextFieldSpec("pincode", "PIN Code", false),
    TextFieldSpec("mobile", "Mobile Number", true),
    TextFieldSpec("email", "Email Address", false),
    TextFieldSpec("idNumber", "ID Proof Number", false)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onBack: () -> Unit, onRegistered: (memberId: String) -> Unit) {
    val context = LocalContext.current
    var values by remember { mutableStateOf(TEXT_FIELDS.associate { it.key to "" }) }
    var gender by remember { mutableStateOf("") }
    var membershipType by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var idType by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) photoUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register new member") },
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
            // --- Photograph upload ---
            Text("Photograph", style = MaterialTheme.typography.titleMedium)
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(140.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Selected photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
            OutlinedButton(onClick = {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                Text(if (photoUri == null) "Upload passport size photograph" else "Change photo")
            }
            Text(
                "Photo is resized and sent as JPEG.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // --- Text fields (fullName, guardian, dob, ...) ---
            TEXT_FIELDS.forEach { spec ->
                OutlinedTextField(
                    value = values[spec.key] ?: "",
                    onValueChange = { values = values.toMutableMap().apply { put(spec.key, it) } },
                    label = { Text(if (spec.required) "${spec.label} *" else spec.label) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }

            // --- Dropdowns, matching the web form's <select> fields ---
            DropdownField(
                label = "Gender *",
                options = GENDER_OPTIONS,
                selected = gender,
                onSelected = { gender = it },
                modifier = Modifier.padding(vertical = 4.dp)
            )
            DropdownField(
                label = "Membership Type *",
                options = MEMBERSHIP_TYPE_OPTIONS,
                selected = membershipType,
                onSelected = { membershipType = it },
                modifier = Modifier.padding(vertical = 4.dp)
            )
            DropdownField(
                label = "Membership Duration",
                options = DURATION_OPTIONS,
                selected = duration,
                onSelected = { duration = it },
                modifier = Modifier.padding(vertical = 4.dp)
            )
            DropdownField(
                label = "ID Proof Type",
                options = ID_TYPE_OPTIONS,
                selected = idType,
                onSelected = { idType = it },
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
            }

            Button(
                onClick = {
                    val missing = mutableListOf<String>()
                    TEXT_FIELDS.filter { it.required }.forEach { if (values[it.key].isNullOrBlank()) missing.add(it.label) }
                    if (gender.isBlank()) missing.add("Gender")
                    if (membershipType.isBlank()) missing.add("Membership Type")
                    if (missing.isNotEmpty()) {
                        error = "Missing required: ${missing.joinToString(", ")}"
                        return@Button
                    }
                    scope.launch {
                        submitting = true
                        error = null
                        try {
                            val photoDataUri = photoUri?.let { uri ->
                                withContext(Dispatchers.Default) { ImageUtil.uriToJpegDataUri(context, uri) }
                            }
                            val body = mutableMapOf<String, Any?>()
                            values.filterValues { it.isNotBlank() }.forEach { (k, v) -> body[k] = v }
                            body["gender"] = gender
                            if (membershipType.isNotBlank()) body["membershipType"] = membershipType
                            if (duration.isNotBlank()) body["duration"] = duration
                            if (idType.isNotBlank()) body["idType"] = idType
                            if (photoDataUri != null) body["photoUrl"] = photoDataUri

                            val res = ApiClient.service.register(body)
                            if (res.success && res.memberId != null) {
                                onRegistered(res.memberId)
                            } else {
                                error = res.message ?: "Registration failed"
                            }
                        } catch (e: Exception) {
                            error = e.message ?: "Network error"
                        } finally {
                            submitting = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(top = 12.dp)
            ) { Text(if (submitting) "Registering..." else "Register member") }
        }
    }
}
