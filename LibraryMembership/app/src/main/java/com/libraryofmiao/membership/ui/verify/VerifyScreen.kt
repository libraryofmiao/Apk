package com.libraryofmiao.membership.ui.verify

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.libraryofmiao.membership.R
import com.libraryofmiao.membership.data.network.ApiClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyScreen(
    initialCode: String?,
    onBack: () -> Unit,
    viewModel: VerifyViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { viewModel.verify(it) }
    }

    LaunchedEffect(initialCode) {
        if (!initialCode.isNullOrBlank()) viewModel.verify(initialCode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.member_verification)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.library_name), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))

            // New capability vs the web verify.html, which only supported a code in the URL:
            // an in-app camera scan of the printed/digital card's QR.
            Button(onClick = {
                scanLauncher.launch(ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE).setBeepEnabled(true))
            }) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.scan_qr))
            }

            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.enter_code_manually), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.code,
                    onValueChange = viewModel::onCodeChanged,
                    label = { Text("Verification Code") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = { viewModel.verify() }, enabled = state.code.isNotBlank()) { Text("Go") }
            }

            Spacer(Modifier.height(24.dp))

            when {
                state.loading -> CircularProgressIndicator()
                state.error != null -> Text(state.error!!, color = MaterialTheme.colorScheme.error)
                state.result != null -> {
                    val r = state.result!!
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(
                                model = r.memberId?.let { ApiClient.photoUrl(it, state.code) },
                                contentDescription = "Member Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("Name: ${r.fullName ?: "-"}", fontWeight = FontWeight.Bold)
                            Text("Member ID: ${r.memberId ?: "-"}")
                            Text("Membership Type: ${r.membershipType ?: "-"}")
                            Text("Status: ${r.status ?: "-"}")
                        }
                    }
                }
            }
        }
    }
}
