package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class RegisterRequest(
    val username: String, val email: String, val password: String
)

@Serializable
data class RegisterResponse(
    val user: LoginUser, val token: Token
)

@Serializable
data class Token(
    val access_token: String, val token_type: String
)

@Serializable
data class LoginUser(
    val id: Int, val username: String, val email: String, val password: String
)

@Serializable
data class LoginResponse(
    val user: LoginUser, val token: Token
)


val BonusCardGradientStart = Color(0xFFFED253)
val ActivateButtonColor = Color(0xFFFE8C65)
val GreyText = Color(0xFF757575)
val DarkText = Color(0xFF333333)

data class MenuItemData(
    val icon: ImageVector, val label: String, val route: String? = null
)

@Composable
fun ProfileScreenContent(userPreferences: UserPreferences = UserPreferences(LocalContext.current)) {
    val token = userPreferences.token.collectAsState(initial = null)

    if (token.value != null) {
        AuthorizedProfileScreen(userPreferences)
    } else {
        UnauthorizedProfileScreen(userPreferences)
    }
}

@Composable
fun AuthorizedProfileScreen(userPreferences: UserPreferences) {
    val username = userPreferences.username.collectAsState(initial = "Гость")
    val email = userPreferences.email.collectAsState(initial = "Нет email")
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            ProfileHeader(name = username.value.toString(),
                phone = email.value.toString(),
                onNotificationClick = {})
        }

        item {
            val currentBonusPoints by bonus_card.balance.collectAsState()

            BonusCard(bonusPercentage = "5%",
                bonusPoints = currentBonusPoints,
                onCashbackButtonClick = {})
        }


        val menuItems = listOf(
            MenuItemData(Icons.Default.ShoppingCart, "Мои заказы"),
            MenuItemData(Icons.Default.Person, "Мои данные"),
            MenuItemData(Icons.Default.Home, "Екатеринбург | RU"),
        )

        items(menuItems) { item ->
            MenuItemRow(icon = item.icon,
                label = item.label,
                onClick = { /* TODO: обработка клика по пункту ${item.label} */ })
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun UnauthorizedProfileScreen(userPreferences: UserPreferences) {
    var showLoginDialog by remember { mutableStateOf(false) }
    var showRegisterDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Войдите или зарегистрируйтесь",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            fontFamily = Montserrat
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Чтобы пользоваться профилем, войдите в аккаунт или создайте новый.",
            fontSize = 16.sp,
            color = GreyText,
            fontFamily = Montserrat,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { showLoginDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ActivateButtonColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Войти",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = Montserrat
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = { showRegisterDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            border = BorderStroke(1.dp, ActivateButtonColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Зарегистрироваться",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ActivateButtonColor,
                fontFamily = Montserrat
            )
        }
    }

    if (showLoginDialog) {
        LoginDialog(userPreferences = userPreferences, onDismiss = { showLoginDialog = false })
    }

    if (showRegisterDialog) {
        RegisterDialog(userPreferences = userPreferences,
            onDismiss = { showRegisterDialog = false })
    }
}

@Composable
fun LoginDialog(userPreferences: UserPreferences, onDismiss: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val client = remember {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    AlertDialog(onDismissRequest = onDismiss, title = {
        Text(
            text = "Вход",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            fontFamily = Montserrat
        )
    }, text = {
        Column {
            OutlinedTextField(value = username,
                onValueChange = { username = it },
                label = { Text("Имя пользователя", fontFamily = Montserrat) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontFamily = Montserrat)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = password,
                onValueChange = { password = it },
                label = { Text("Пароль", fontFamily = Montserrat) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontFamily = Montserrat)
            )
        }
    }, confirmButton = {
        Button(
            onClick = {
                if (username.isNotBlank() && password.isNotBlank()) {
                    coroutineScope.launch {
                        try {
                            val requestBody = parametersOf(
                                "grant_type" to listOf("password"),
                                "username" to listOf(username),
                                "password" to listOf(password),
                                "scope" to listOf(""),
                                "client_id" to listOf("string"),
                                "client_secret" to listOf("string")
                            )

                            val response = client.post("http://5.167.254.44:6567/login") {
                                contentType(ContentType.Application.FormUrlEncoded)
                                setBody(FormDataContent(requestBody))
                            }

                            val loginResponse: LoginResponse = response.body()

                            userPreferences.saveUserData(
                                id = loginResponse.user.id,
                                username = loginResponse.user.username,
                                email = loginResponse.user.email,
                                createdAt = "2025-05-21T12:15:00Z",
                                token = loginResponse.token.access_token
                            )

                            onDismiss()
                        } finally {
                            client.close()
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ActivateButtonColor),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Войти", color = Color.White, fontFamily = Montserrat
            )
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(
                text = "Отмена", color = GreyText, fontFamily = Montserrat
            )
        }
    }, containerColor = Color.White, shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun RegisterDialog(userPreferences: UserPreferences, onDismiss: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val client = remember {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    AlertDialog(onDismissRequest = onDismiss, title = {
        Text(
            text = "Регистрация",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            fontFamily = Montserrat
        )
    }, text = {
        Column {
            OutlinedTextField(value = username,
                onValueChange = { username = it },
                label = { Text("Имя пользователя", fontFamily = Montserrat) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontFamily = Montserrat)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = email,
                onValueChange = { email = it },
                label = { Text("Email", fontFamily = Montserrat) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontFamily = Montserrat)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = password,
                onValueChange = { password = it },
                label = { Text("Пароль", fontFamily = Montserrat) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontFamily = Montserrat)
            )
        }
    }, confirmButton = {
        Button(
            onClick = {
                if (username.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                    coroutineScope.launch {
                        try {
                            val requestBody = RegisterRequest(
                                username = username, email = email, password = password
                            )

                            val response = client.post("http://5.167.254.44:6567/register") {
                                contentType(ContentType.Application.Json)
                                setBody(requestBody)
                            }


                            val registerResponse: RegisterResponse = response.body()
                            Log.d("RegisterDebug", "Deserialized response: $registerResponse")

                            userPreferences.saveUserData(
                                id = registerResponse.user.id,
                                username = registerResponse.user.username,
                                email = registerResponse.user.email,
                                createdAt = "2025-05-21T12:15:00Z",
                                token = registerResponse.token.access_token
                            )
                            Log.d(
                                "RegisterDebug",
                                "id=${registerResponse.user.id}, username=${registerResponse.user.username}, email=${registerResponse.user.email}, token=${registerResponse.token.access_token}"
                            )

                            onDismiss()
                        } finally {
                            client.close()
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ActivateButtonColor),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Зарегистрироваться", color = Color.White, fontFamily = Montserrat
            )
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(
                text = "Отмена", color = GreyText, fontFamily = Montserrat
            )
        }
    }, containerColor = Color.White, shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun ProfileHeader(name: String, phone: String, onNotificationClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontFamily = Montserrat
            )
            Text(
                text = phone, fontSize = 12.sp, color = GreyText, fontFamily = Montserrat
            )
        }
        IconButton(onClick = onNotificationClick) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Уведомления",
                tint = GreyText
            )
        }
    }
}

@Composable
fun BonusCard(bonusPercentage: String, bonusPoints: Int, onCashbackButtonClick: () -> Unit) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BonusCardGradientStart),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Кэшбэк: $bonusPercentage",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkText,
                        fontFamily = Montserrat
                    )
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Информация о бонусах",
                        tint = DarkText,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "$bonusPoints \uD83C\uDF6F",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    fontFamily = Montserrat,
                    )
            }
        }
    }
}

@Composable
fun MenuItemRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = DarkText,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label, fontSize = 16.sp, color = DarkText, fontFamily = Montserrat
        )
    }
}