package id.my.matahati.pos.ui.screen.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.my.matahati.pos.R
import id.my.matahati.pos.model.UserDto
import id.my.matahati.pos.ui.theme.AppPrimaryColor
import id.my.matahati.pos.ui.theme.OlseraLightBg

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (String, UserDto) -> Unit
) {
    val uiState = viewModel.uiState
    var passwordVisible by remember { mutableStateOf(false) }
    val passwordFocusRequester = remember { FocusRequester() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        val isTablet = maxWidth >= 600.dp

        if (isTablet) {
            // Layout Tablet: 2 Panel Kiri (Ilustrasi) & Kanan (Form Login)
            Row(modifier = Modifier.fillMaxSize()) {
                // Panel Kiri - Ilustrasi Visual
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(OlseraLightBg)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    LoginLeftIllustrationPanel()
                }

                // Panel Kanan - Form Login
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 48.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoginFormPanel(
                        viewModel = viewModel,
                        uiState = uiState,
                        passwordVisible = passwordVisible,
                        onPasswordVisibleChange = { passwordVisible = it },
                        passwordFocusRequester = passwordFocusRequester,
                        onLoginSuccess = onLoginSuccess,
                        showIllustrationHeader = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 440.dp)
                    )
                }
            }
        } else {
            // Layout Phone: Single Column Langsung (Tanpa Card)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                LoginFormPanel(
                    viewModel = viewModel,
                    uiState = uiState,
                    passwordVisible = passwordVisible,
                    onPasswordVisibleChange = { passwordVisible = it },
                    passwordFocusRequester = passwordFocusRequester,
                    onLoginSuccess = onLoginSuccess,
                    showIllustrationHeader = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 480.dp)
                )
            }
        }
    }
}

@Composable
private fun LoginLeftIllustrationPanel() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        // Graphic Illustration Box
        Box(
            contentAlignment = Alignment.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(AppPrimaryColor.copy(alpha = 0.2f))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.matahati_pos),
                    contentDescription = "POS Illustration",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(150.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "MATAHATI POS",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AppPrimaryColor,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun LoginFormPanel(
    viewModel: LoginViewModel,
    uiState: LoginUiState,
    passwordVisible: Boolean,
    onPasswordVisibleChange: (Boolean) -> Unit,
    passwordFocusRequester: FocusRequester,
    onLoginSuccess: (String, UserDto) -> Unit,
    modifier: Modifier = Modifier,
    showIllustrationHeader: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        if (showIllustrationHeader) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(AppPrimaryColor.copy(alpha = 0.15f))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.matahati_pos),
                        contentDescription = "POS Illustration",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "MATAHATI POS",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppPrimaryColor,
                    letterSpacing = 0.5.sp
                )

            }
        }

        if (!showIllustrationHeader) {
            Text(
                text = "Selamat Datang",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Silakan login menggunakan akun POS Anda",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )
        }

        // Field Email
        Text(
            text = "Email *",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            placeholder = { Text("Masukkan Email") },
            singleLine = true,
            shape = RoundedCornerShape(5.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = AppPrimaryColor
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { passwordFocusRequester.requestFocus() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppPrimaryColor,
                unfocusedBorderColor = Color.LightGray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Field Password
        Text(
            text = "Password *",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            placeholder = { Text("Masukkan Password") },
            singleLine = true,
            shape = RoundedCornerShape(5.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = AppPrimaryColor
                )
            },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"
                IconButton(onClick = { onPasswordVisibleChange(!passwordVisible) }) {
                    Icon(imageVector = image, contentDescription = description, tint = Color.Gray)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { viewModel.login(onLoginSuccess) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppPrimaryColor,
                unfocusedBorderColor = Color.LightGray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester)
        )

        if (uiState is LoginUiState.Error) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Button Login
        Button(
            onClick = { viewModel.login(onLoginSuccess) },
            enabled = uiState !is LoginUiState.Loading,
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppPrimaryColor,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            if (uiState is LoginUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Login",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


