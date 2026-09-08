package com.libraryofmiao.membership.ui.membercard

import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.print.PrintHelper
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.libraryofmiao.membership.R
import com.libraryofmiao.membership.data.network.ApiClient
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberCardScreen(
    memberId: String,
    onBack: () -> Unit,
    onVerify: (String) -> Unit,
    viewModel: MemberCardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var savedMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(memberId) { viewModel.load(memberId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.digital_card_title)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                state.loading -> CircularProgressIndicator()
                state.error != null -> Text(state.error!!, color = MaterialTheme.colorScheme.error)
                state.member != null -> {
                    val member = state.member!!
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.library_name), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.digital_card_title), style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(12.dp))

                        AsyncImage(
                            model = member.memberId?.let { ApiClient.photoUrl(it, member.verify) },
                            contentDescription = "Member Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(120.dp).clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(Modifier.height(12.dp))

                        Text("Name: ${member.fullName}", fontWeight = FontWeight.Bold)
                        Text("Member ID: ${member.memberId ?: "-"}")
                        Text("Email: ${member.email ?: "-"}")
                        Text("Membership: ${member.membershipType ?: "-"}")
                        Text("Issue Date: ${member.issueDate ?: "-"}")
                        Text("Status: ${member.status}")

                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.scan_verification), style = MaterialTheme.typography.labelMedium)
                        state.qrBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "QR",
                                modifier = Modifier.size(160.dp).padding(top = 8.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        Text("____________________")
                        Text(stringResource(R.string.authorized_signatory), style = MaterialTheme.typography.bodySmall)
                        Text(stringResource(R.string.library_name), style = MaterialTheme.typography.bodySmall)

                        Spacer(Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = {
                                val path = exportCardAsPdf(context, member.fullName, member.memberId ?: "member")
                                savedMessage = if (path != null) "Saved: $path" else "Failed to save PDF"
                            }) { Text(stringResource(R.string.download_pdf_card)) }

                            OutlinedButton(onClick = {
                                state.qrBitmap?.let { bmp ->
                                    PrintHelper(context).apply { scaleMode = PrintHelper.SCALE_MODE_FIT }
                                        .printBitmap("member_card", bmp)
                                }
                            }) { Text(stringResource(R.string.print_card)) }
                        }
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { member.verify?.let(onVerify) }) {
                            Text(stringResource(R.string.verify_member))
                        }
                    }
                }
            }
        }
        savedMessage?.let {
            LaunchedEffect(it) {
                kotlinx.coroutines.delay(2500)
                savedMessage = null
            }
        }
    }
}

/** Minimal single-page PDF containing the card's text summary — kept simple and dependency-free. */
private fun exportCardAsPdf(context: android.content.Context, name: String, memberId: String): String? {
    return try {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 450, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint().apply { textSize = 14f }
        canvas.drawText("Sub Divisional Library Miao", 20f, 40f, paint)
        canvas.drawText("Library Membership Card", 20f, 60f, paint)
        canvas.drawText("Name: $name", 20f, 100f, paint)
        canvas.drawText("Member ID: $memberId", 20f, 120f, paint)
        document.finishPage(page)

        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(dir, "${memberId}_card.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}
