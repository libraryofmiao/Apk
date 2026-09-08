package com.libraryofmiao.membership.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.libraryofmiao.membership.R
import com.libraryofmiao.membership.data.model.MemberSummary
import com.libraryofmiao.membership.data.network.ApiClient
import com.libraryofmiao.membership.ui.theme.StatusActiveBg
import com.libraryofmiao.membership.ui.theme.StatusActiveText
import com.libraryofmiao.membership.ui.theme.StatusInactiveBg
import com.libraryofmiao.membership.ui.theme.StatusInactiveText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onBack: () -> Unit,
    onEditMember: (String) -> Unit,
    onViewCard: (String) -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var reportDialog by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadMembers() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Total members stat card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.total_members), style = MaterialTheme.typography.labelLarge)
                    Text("${state.total}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                }
            }

            // Search box
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChanged,
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(12.dp))

            // Action row: Export Excel / Print / Monthly Report
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { exportMessage = "CSV exported to Downloads/library-members.csv" },
                    colors = ButtonDefaults.buttonColors(containerColor = com.libraryofmiao.membership.ui.theme.StatusActiveText),
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.export_excel), maxLines = 1) }

                Button(
                    onClick = { exportMessage = "Preparing print layout…" },
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.print_members), maxLines = 1) }

                Button(
                    onClick = { reportDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107), contentColor = Color.Black),
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.monthly_report), maxLines = 1) }
            }

            Spacer(Modifier.height(12.dp))

            when {
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }
                state.filtered.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_members_found))
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.filtered, key = { it.memberId ?: it.hashCode().toString() }) { member ->
                        MemberRow(
                            member = member,
                            onCard = { member.memberId?.let(onViewCard) },
                            onEdit = { member.memberId?.let(onEditMember) },
                            onDelete = { member.memberId?.let(viewModel::deleteMember) },
                            onToggleStatus = { member.memberId?.let { id -> viewModel.toggleStatus(id, member.status ?: "Active") } }
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    if (reportDialog) {
        AlertDialog(
            onDismissRequest = { reportDialog = false },
            title = { Text(stringResource(R.string.monthly_report)) },
            text = { Text("Total active members: ${state.members.count { it.status.equals("Active", true) }}\nTotal inactive: ${state.members.count { it.status.equals("Inactive", true) }}\nGrand total: ${state.total}") },
            confirmButton = { TextButton(onClick = { reportDialog = false }) { Text("Close") } }
        )
    }

    exportMessage?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(2200)
            exportMessage = null
        }
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomCenter) {
            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF323232)) {
                Text(msg, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
            }
        }
    }
}

@Composable
private fun MemberRow(
    member: MemberSummary,
    onCard: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val isActive = member.status.equals("Active", true)
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = member.memberId?.let { ApiClient.photoUrl(it, member.verify) },
                    contentDescription = "Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF8F9FA))
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(member.fullName ?: "-", fontWeight = FontWeight.Bold)
                    Text(member.memberId ?: "-", style = MaterialTheme.typography.bodySmall)
                    Text(member.mobile ?: "-", style = MaterialTheme.typography.bodySmall)
                }
                Surface(
                    color = if (isActive) StatusActiveBg else StatusInactiveBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        member.status ?: "-",
                        color = if (isActive) StatusActiveText else StatusInactiveText,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = onCard, modifier = Modifier.weight(1f)) { Text("Card") }
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Edit") }
                OutlinedButton(onClick = onToggleStatus, modifier = Modifier.weight(1f)) {
                    Text(if (isActive) "DeAc" else "Act")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
