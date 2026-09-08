package com.libraryofmiao.membership.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.libraryofmiao.membership.R
import com.libraryofmiao.membership.ui.theme.*

@Composable
fun LoginScreen(
    onAuthenticated: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.success) {
        if (state.success) onAuthenticated()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LoginBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LoginCardBg, RoundedCornerShape(20.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.Shield, contentDescription = null, tint = AccentBlueLight, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.welcome_admin),
                color = TextLight,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.login_prompt),
                color = TextMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = state.pin,
                onValueChange = { if (it.length <= 6) viewModel.onPinChanged(it) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                textStyle = androidx.compose.ui.text.TextStyle(
                    textAlign = TextAlign.Center, letterSpacing = 8.sp, color = TextLight
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlueLight,
                    unfocusedBorderColor = LoginBorder,
                    cursorColor = AccentBlueLight,
                    focusedContainerColor = LoginBg,
                    unfocusedContainerColor = LoginBg
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(state.error!!, color = ErrorRed, fontSize = 13.sp, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.submit() },
                enabled = state.pin.length == 6 && !state.loading,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (state.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.authenticate), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
