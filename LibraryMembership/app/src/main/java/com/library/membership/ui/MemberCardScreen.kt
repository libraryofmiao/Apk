package com.library.membership.ui

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.library.membership.api.ApiClient
import com.library.membership.api.Member
import com.library.membership.util.QrUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberCardScreen(onBack: () -> Unit) {
    var idInput by remember { mutableStateOf("") }
    var member by remember { mutableStateOf<Member?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My membership card") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = idInput,
                    onValueChange = { idInput = it },
                    label = { Text("Your Member ID (e.g. SDLM0001)") },
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.foundation.layout.Spacer(Modifier.padding(4.dp))
                Button(
                    onClick = {
                        val id = idInput.trim()
                        if (id.isBlank()) return@Button
                        scope.launch {
                            loading = true
                            error = null
                            member = null
                            try {
                                val res = ApiClient.service.getMemberBasic(id)
                                if (res.success) member = res.member else error = res.message ?: "Not found"
                            } catch (e: Exception) {
                                error = e.message ?: "Network error"
                            } finally {
                                loading = false
                            }
                        }
                    },
                    modifier = Modifier.height(56.dp)
                ) { Text("Find") }
            }

            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))

            when {
                loading -> CircularProgressIndicator()
                error != null -> Text("Error: $error", color = MaterialTheme.colorScheme.error)
                member != null -> {
                    val m = member!!
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Column(
                            modifier = Modifier.padding(20.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val photoUrl = ApiClient.photoUrl(idInput.trim(), m.verify, m.photoKey)
                            if (photoUrl != null) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Your photo",
                                    modifier = Modifier.size(100.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Text(m.fullName ?: "", style = MaterialTheme.typography.titleLarge)
                            Text(idInput.trim(), style = MaterialTheme.typography.bodyMedium)
                            Text(m.membershipType ?: "", style = MaterialTheme.typography.bodySmall)
                            Text(
                                m.status ?: "",
                                color = if (m.status == "Active") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )

                            androidx.compose.foundation.layout.Spacer(Modifier.padding(12.dp))

                            if (!m.verify.isNullOrBlank()) {
                                val qrBitmap = remember(m.verify) { QrUtil.generate(m.verify) }
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Membership QR code",
                                    modifier = Modifier.size(220.dp)
                                )
                                Text("Show this to library staff for verification", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
