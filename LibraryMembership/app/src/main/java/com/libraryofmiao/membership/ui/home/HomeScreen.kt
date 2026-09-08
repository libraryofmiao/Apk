package com.libraryofmiao.membership.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.libraryofmiao.membership.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRegister: () -> Unit,
    onMembers: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.library_name)) },
                actions = {
                    TextButton(onClick = onLogout) {
                        Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.logout))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.LocalLibrary, contentDescription = null, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onRegister) { Text(stringResource(R.string.register_now)) }
                OutlinedButton(onClick = onMembers) { Text(stringResource(R.string.members)) }
            }

            Spacer(Modifier.height(36.dp))

            FeatureCard(Icons.Filled.HowToReg, stringResource(R.string.feature_online_reg), stringResource(R.string.feature_online_reg_desc))
            Spacer(Modifier.height(12.dp))
            FeatureCard(Icons.Filled.QrCode2, stringResource(R.string.feature_digital_card), stringResource(R.string.feature_digital_card_desc))
            Spacer(Modifier.height(12.dp))
            FeatureCard(Icons.Filled.ManageSearch, stringResource(R.string.feature_easy_mgmt), stringResource(R.string.feature_easy_mgmt_desc))
        }
    }
}

@Composable
private fun FeatureCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(desc, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
