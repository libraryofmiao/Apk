package com.libraryofmiao.membership.ui.register

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.libraryofmiao.membership.R
import com.libraryofmiao.membership.data.session.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    memberId: String?,
    onBack: () -> Unit,
    onSubmitted: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showOptionsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(memberId) {
        if (!memberId.isNullOrBlank()) viewModel.loadForEdit(memberId)
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let(viewModel::onPhotoPicked)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (state.isEditMode) R.string.edit_title else R.string.register_title)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.form_note), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))

            LabeledField(stringResource(R.string.full_name), state.member.fullName) {
                viewModel.updateField { m -> m.copy(fullName = it) }
            }
            LabeledField(stringResource(R.string.guardian_name), state.member.guardianName ?: "") {
                viewModel.updateField { m -> m.copy(guardianName = it) }
            }
            DropdownField(stringResource(R.string.gender), listOf("Male", "Female", "Other"), state.member.gender) {
                viewModel.updateField { m -> m.copy(gender = it) }
            }
            LabeledField(stringResource(R.string.dob), state.member.dob ?: "", placeholder = "YYYY-MM-DD") {
                viewModel.updateField { m -> m.copy(dob = it) }
            }
            LabeledField(stringResource(R.string.occupation), state.member.occupation ?: "") {
                viewModel.updateField { m -> m.copy(occupation = it) }
            }
            LabeledField(stringResource(R.string.address), state.member.address ?: "") {
                viewModel.updateField { m -> m.copy(address = it) }
            }
            LabeledField(stringResource(R.string.district), state.member.district ?: "") {
                viewModel.updateField { m -> m.copy(district = it) }
            }
            LabeledField(stringResource(R.string.state), state.member.state ?: "") {
                viewModel.updateField { m -> m.copy(state = it) }
            }
            LabeledField(stringResource(R.string.pincode), state.member.pincode ?: "") {
                viewModel.updateField { m -> m.copy(pincode = it) }
            }
            LabeledField(stringResource(R.string.mobile), state.member.mobile) {
                viewModel.updateField { m -> m.copy(mobile = it) }
            }
            LabeledField(stringResource(R.string.email), state.member.email ?: "") {
                viewModel.updateField { m -> m.copy(email = it) }
            }
            DropdownField(stringResource(R.string.membership_type), state.options.membershipTypes, state.member.membershipType) {
                viewModel.updateField { m -> m.copy(membershipType = it) }
            }
            DropdownField(stringResource(R.string.membership_duration), state.options.membershipDurations, state.member.membershipDuration) {
                viewModel.updateField { m -> m.copy(membershipDuration = it) }
            }
            DropdownField(stringResource(R.string.id_type), state.options.idTypes, state.member.idType) {
                viewModel.updateField { m -> m.copy(idType = it) }
            }
            LabeledField(stringResource(R.string.id_number), state.member.idNumber ?: "") {
                viewModel.updateField { m -> m.copy(idNumber = it) }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text(stringResource(R.string.photo_upload), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.photoUri != null) {
                    AsyncImage(
                        model = state.photoUri,
                        contentDescription = "Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                }
                OutlinedButton(onClick = { photoPicker.launch("image/*") }) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Choose Photo")
                }
            }
            Text(stringResource(R.string.photo_note), style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = state.declarationChecked, onCheckedChange = viewModel::onDeclarationChanged)
                Text(stringResource(R.string.declaration), style = MaterialTheme.typography.bodySmall)
            }

            if (state.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { viewModel.submit(onSubmitted) },
                    enabled = state.declarationChecked && !state.submitting,
                    modifier = Modifier.weight(1f)
                ) {
                    if (state.submitting) CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    else Text(stringResource(if (state.isEditMode) R.string.save_changes else R.string.submit_registration))
                }
                OutlinedButton(onClick = { viewModel.reset() }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.reset))
                }
            }

            Spacer(Modifier.height(24.dp))
            TextButton(onClick = { showOptionsSheet = true }) {
                Text(stringResource(R.string.manage_options))
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showOptionsSheet) {
        ManageOptionsSheet(
            onDismiss = { showOptionsSheet = false },
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageOptionsSheet(onDismiss: () -> Unit, viewModel: RegisterViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val session = remember { SessionManager(context) }
    var field by remember { mutableStateOf("membershipTypes") }
    var newValue by remember { mutableStateOf("") }
    var adminKey by remember { mutableStateOf(session.getOptionsAdminKey() ?: "") }
    var message by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(20.dp)) {
            Text(stringResource(R.string.add_new_option), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            DropdownField("Field", listOf("membershipTypes", "membershipDurations", "idTypes"), field) { field = it ?: field }
            OutlinedTextField(
                value = newValue, onValueChange = { newValue = it },
                label = { Text("New value") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = adminKey, onValueChange = { adminKey = it },
                label = { Text("Admin Key (X-Option-Admin-Key)") },
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            message?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        viewModel.addOption(field, newValue, adminKey) { success, error ->
                            message = if (success) "Saved!" else error
                            if (success) onDismiss()
                        }
                    },
                    enabled = newValue.isNotBlank() && adminKey.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.add)) }
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.cancel)) }
            }
        }
    }
}

@Composable
private fun LabeledField(label: String, value: String, placeholder: String? = null, onChange: (String) -> Unit) {
    Column(Modifier.padding(bottom = 12.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(label: String, options: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.padding(bottom = 12.dp)) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selected ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
                }
            }
        }
    }
}
