package com.library.membership.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RoleSelectScreen(
    onAdmin: () -> Unit,
    onVerify: () -> Unit,
    onMemberCard: () -> Unit
) {
    Scaffold { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Library Membership", style = MaterialTheme.typography.headlineMedium)
            Text("Choose how you'd like to continue", style = MaterialTheme.typography.bodyMedium)

            androidx.compose.foundation.layout.Spacer(Modifier.height(32.dp))

            Button(onClick = onAdmin, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Admin — manage members")
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
            Button(onClick = onVerify, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Staff — scan / verify")
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
            Button(onClick = onMemberCard, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Member — my card")
            }
        }
    }
}
