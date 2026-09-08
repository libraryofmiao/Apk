package com.library.membership.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.library.membership.api.ApiClient
import com.library.membership.api.Member
import com.library.membership.util.QrScannerView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var manualCode by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Member?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var lastScanned by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    fun runVerify(code: String) {
        if (code.isBlank() || loading) return
        scope.launch {
            loading = true
            error = null
            result = null
            try {
                val res = ApiClient.service.verifyByCode(code.trim())
                if (res.success) result = res.member else error = res.message ?: "Not found"
            } catch (e: Exception) {
                error = e.message ?: "Network error"
            } finally {
                loading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify member") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (hasCameraPermission) {
                QrScannerView(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    onDetected = { code ->
                        if (code != lastScanned) {
                            lastScanned = code
                            runVerify(code)
                        }
                    }
                )
            } else {
                Text("Camera permission is needed to scan QR codes.")
            }

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                OutlinedTextField(
                    value = manualCode,
                    onValueChange = { manualCode = it },
                    label = { Text("Or enter code manually") },
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.foundation.layout.Spacer(Modifier.padding(4.dp))
                Button(onClick = { runVerify(manualCode) }, modifier = Modifier.height(56.dp)) {
                    Text("Check")
                }
            }

            when {
                loading -> CircularProgressIndicator()
                error != null -> Text("✗ $error", color = MaterialTheme.colorScheme.error)
                result != null -> {
                    val m = result!!
                    val isActive = m.status == "Active"
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp)) {
                            val photoUrl = ApiClient.photoUrl(m.memberId, m.verify, m.photoKey)
                            if (photoUrl != null) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Member photo",
                                    modifier = Modifier.size(80.dp),
                                    contentScale = ContentScale.Crop
                                )
                                androidx.compose.foundation.layout.Spacer(Modifier.padding(4.dp))
                            }
                            Column {
                                Text(
                                    if (isActive) "✓ ACTIVE" else "✗ ${m.status?.uppercase() ?: "INACTIVE"}",
                                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(m.fullName ?: "", style = MaterialTheme.typography.titleSmall)
                                Text("${m.memberId} · ${m.membershipType ?: ""}")
                                Text(m.mobile ?: "")
                            }
                        }
                    }
                }
            }
        }
    }
}
