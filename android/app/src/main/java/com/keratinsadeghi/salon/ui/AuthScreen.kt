package com.keratinsadeghi.salon.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.keratinsadeghi.salon.data.Database

@Composable
fun AuthScreen(db: Database, onAuthenticated: () -> Unit) {
    val isSetup = remember { !db.hasUser() }
    val savedUsername = remember { db.currentUsername().orEmpty() }
    var username by remember { mutableStateOf(savedUsername) }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(78.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Brush.linearGradient(listOf(SalonColors.Hot, SalonColors.RoseDeep))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp),
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "سالن کراتین خانم صادقی",
                style = MaterialTheme.typography.headlineSmall,
                color = SalonColors.Ink,
                textAlign = TextAlign.Center,
            )
            Text(
                if (isSetup) "برای امنیت پنل، حساب مدیر را بسازید" else "برای ورود، نام کاربری و رمز عبور را وارد کنید",
                style = MaterialTheme.typography.bodySmall,
                color = SalonColors.Muted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(18.dp))
            PinCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SalonColors.Blush),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = SalonColors.RoseDeep, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.size(8.dp))
                    Text(
                        if (isSetup) "ساخت حساب مدیر" else "ورود مدیر",
                        style = MaterialTheme.typography.titleMedium,
                        color = SalonColors.Ink,
                    )
                }
                Spacer(Modifier.height(12.dp))
                LabeledField(
                    label = "نام کاربری",
                    value = username,
                    onValueChange = { username = it },
                    required = true,
                )
                Spacer(Modifier.height(8.dp))
                Column {
                    Text(
                        "رمز عبور *",
                        style = MaterialTheme.typography.labelMedium,
                        color = SalonColors.Muted,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                        ),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SalonColors.Hot,
                            unfocusedBorderColor = SalonColors.Line,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White.copy(alpha = 0.92f),
                            cursorColor = SalonColors.Hot,
                        ),
                    )
                }
                if (isSetup) {
                    Spacer(Modifier.height(8.dp))
                    Column {
                        Text(
                            "تکرار رمز عبور *",
                            style = MaterialTheme.typography.labelMedium,
                            color = SalonColors.Muted,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        androidx.compose.material3.OutlinedTextField(
                            value = confirm,
                            onValueChange = { confirm = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                            ),
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SalonColors.Hot,
                                unfocusedBorderColor = SalonColors.Line,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White.copy(alpha = 0.92f),
                                cursorColor = SalonColors.Hot,
                            ),
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                ErrorText(error)
                Spacer(Modifier.height(10.dp))
                PinkButton(
                    text = if (isSetup) "ساخت حساب و ورود" else "ورود به پنل",
                    onClick = {
                        error = null
                        if (isSetup) {
                            if (password != confirm) {
                                error = "تکرار رمز عبور یکسان نیست"
                            } else {
                                val message = db.setupUser(username, password)
                                if (message == null) onAuthenticated() else error = message
                            }
                        } else {
                            if (db.verifyUser(username, password)) onAuthenticated()
                            else error = "نام کاربری یا رمز عبور درست نیست"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "همه اطلاعات فقط روی همین گوشی ذخیره می‌شود.",
                style = MaterialTheme.typography.bodySmall,
                color = SalonColors.Muted,
                textAlign = TextAlign.Center,
            )
        }
    }
}
