package com.example.myapplication

import UserPreferences
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.call.body
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class OrdersViewModel : ViewModel() {
    private val client = HttpClient {
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("KtorLogger", message)
                }
            }
            level = LogLevel.ALL
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    // Добавлено для хранения всех блюд
    private val _allDishes = MutableStateFlow<List<Dish>>(emptyList())
    val allDishes: StateFlow<List<Dish>> = _allDishes

    init {
        viewModelScope.launch {
            fetchAllDishes()
        }
    }

    suspend fun fetchAllDishes() {
        try {
            val response: HttpResponse = client.get("http://5.167.254.44:6567/dish")
            if (response.status == HttpStatusCode.OK) {
                val dishes = response.body<List<Dish>>()
                _allDishes.value = dishes
                Log.d("OrdersViewModel", "Fetched all dishes: $dishes")
            } else {
                Log.e("OrdersViewModel", "Failed to fetch all dishes: ${response.status}. Body: ${response.bodyAsText()}")
                _allDishes.value = emptyList()
            }
        } catch (e: Exception) {
            Log.e("OrdersViewModel", "Error fetching all dishes: ${e.message}", e)
            _allDishes.value = emptyList()
        }
    }

    suspend fun fetchOrders(user_id: Int) {
        if (user_id == 0) { // Prevent API call with invalid user_id
            Log.e("OrdersViewModel", "Invalid user ID (0). Not fetching orders.")
            _orders.value = emptyList()
            return
        }
        try {
            val response: HttpResponse = client.get("http://5.167.254.44:6567/user/${user_id}/orders")
            if (response.status == HttpStatusCode.OK) {
                val list = response.body<List<Order>>()
                _orders.value = list
                Log.d("OrdersViewModel", "Fetched orders: $list")
            } else {
                Log.e("OrdersViewModel", "Failed to fetch orders: ${response.status}. Body: ${response.bodyAsText()}")
                _orders.value = emptyList() // Clear orders on error
            }
        } catch (e: Exception) {
            Log.e("OrdersViewModel", "Error fetching orders: ${e.message}", e)
            _orders.value = emptyList()
        }
    }
}

@Serializable
data class DishForOrder(
    val dish_id: Int,
    val quantity: Int
)

@Serializable
data class Order(
    val id: Int,
    val user_id: Int,
    val order_data: String = "N/A",
    val status: String,
    val dishes: List<DishForOrder>
)

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
fun ProfileScreenContent(
    userPreferences: UserPreferences = UserPreferences(LocalContext.current),
    ordersViewModel: OrdersViewModel = viewModel() // ViewModel получен здесь
) {
    val token by userPreferences.token.collectAsState(initial = null)
    val orders by ordersViewModel.orders.collectAsState()

    if (token != null) {
        AuthorizedProfileScreen(userPreferences, ordersViewModel, orders)
    } else {
        UnauthorizedProfileScreen(userPreferences)
    }
}

@Composable
fun AuthorizedProfileScreen(
    userPreferences: UserPreferences,
    ordersViewModel: OrdersViewModel,
    orders: List<Order>
) {
    val username by userPreferences.username.collectAsState(initial = "Гость")
    val email by userPreferences.email.collectAsState(initial = "Нет email")
    val userId by userPreferences.id.collectAsState(initial = 0)
    val coroutineScope = rememberCoroutineScope()
    var showOrdersDialog by remember { mutableStateOf(false) }

    val bonus_card = remember { mutableStateOf(bonus_card.balance) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            ProfileHeader(
                name = username ?: "Гость",
                phone = email ?: "Нет email",
                onNotificationClick = {}
            )
        }

        item {
            BonusCard(
                bonusPercentage = "5%",
                bonusPoints = bonus_card.value.value,
                onCashbackButtonClick = {}
            )
        }

        val menuItems = listOf(
            MenuItemData(Icons.Default.ShoppingCart, "Мои заказы"),
            MenuItemData(Icons.Default.Person, "Мои данные"),
            MenuItemData(Icons.Default.Home, "Екатеринбург | RU"),
        )

        items(menuItems) { item ->
            MenuItemRow(
                icon = item.icon,
                label = item.label,
                onClick = {
                    if (item.label == "Мои заказы") {
                        coroutineScope.launch {
                            val currentUserId = userId

                            currentUserId?.let { id ->
                                if (id != 0) {
                                    ordersViewModel.fetchOrders(id)
                                    showOrdersDialog = true
                                } else {
                                    Log.e("AuthorizedProfileScreen", "Cannot fetch orders: User ID is 0, which is likely an unauthenticated or invalid ID.")
                                }
                            } ?: run {
                                Log.e("AuthorizedProfileScreen", "Cannot fetch orders: User ID is null.")
                            }
                        }
                    }
                }
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    if (showOrdersDialog) {
        OrdersDialog(orders = orders, ordersViewModel = ordersViewModel, onDismiss = { showOrdersDialog = false })
    }
}

@Composable
fun OrdersDialog(orders: List<Order>, ordersViewModel: OrdersViewModel, onDismiss: () -> Unit) { // Принимаем ordersViewModel
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Мои заказы",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontFamily = Montserrat
            )
        },
        text = {
            if (orders.isEmpty()) {
                Text(
                    text = "У вас пока нет заказов.",
                    fontSize = 16.sp,
                    color = GreyText,
                    fontFamily = Montserrat
                )
            } else {
                LazyColumn {
                    items(orders) { order ->
                        // Передаем ordersViewModel в OrderCard
                        OrderCard(order = order, ordersViewModel = ordersViewModel)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Закрыть",
                    color = ActivateButtonColor,
                    fontFamily = Montserrat
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun OrderCard(order: Order, ordersViewModel: OrdersViewModel) {
    // Собираем состояние списка всех блюд из OrdersViewModel
    val allDishes = ordersViewModel.allDishes.collectAsState().value

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Заказ #${order.id}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = DarkText,
                fontFamily = Montserrat
            )
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = GreyText.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Дата:",
                    fontSize = 14.sp,
                    color = GreyText,
                    fontFamily = Montserrat
                )
                Text(
                    text = order.order_data,
                    fontSize = 14.sp,
                    color = DarkText,
                    fontWeight = FontWeight.Medium,
                    fontFamily = Montserrat
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Статус:",
                    fontSize = 14.sp,
                    color = GreyText,
                    fontFamily = Montserrat
                )
                Text(
                    text = order.status,
                    fontSize = 14.sp,
                    color = DarkText,
                    fontWeight = FontWeight.Medium,
                    fontFamily = Montserrat
                )
            }

            if (order.dishes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Блюда в заказе:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = DarkText,
                    fontFamily = Montserrat
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    order.dishes.forEach { orderDish ->
                        val dish = allDishes.find { it.id == orderDish.dish_id }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${dish?.name ?: "Неизвестное блюдо"}",
                                fontSize = 14.sp,
                                color = DarkText,
                                fontFamily = Montserrat,
                                modifier = Modifier.fillMaxWidth(0.6f)
                            )
                            Text(
                                text = "Кол-во: ${orderDish.quantity}",
                                fontSize = 14.sp,
                                color = DarkText,
                                fontWeight = FontWeight.Medium,
                                fontFamily = Montserrat
                            )
                        }
                        Divider(
                            color = Color.Gray,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "В этом заказе нет блюд.",
                    fontSize = 14.sp,
                    color = GreyText,
                    fontFamily = Montserrat
                )
            }
        }
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
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("LoginKtor", message)
                    }
                }
                level = LogLevel.ALL
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

                            if (response.status == HttpStatusCode.OK) {
                                val loginResponse: LoginResponse = response.body()

                                userPreferences.saveUserData(
                                    id = loginResponse.user.id,
                                    username = loginResponse.user.username,
                                    email = loginResponse.user.email,
                                    createdAt = "2025-05-21T12:15:00Z", // Assuming this is a static value for now
                                    token = loginResponse.token.access_token
                                )
                                onDismiss()
                            } else {
                                Log.e("LoginDialog", "Login failed: ${response.status}. Body: ${response.bodyAsText()}")
                            }
                        } catch (e: Exception) {
                            Log.e("LoginDialog", "Error during login: ${e.message}", e)
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
            install(Logging) { // Добавьте логирование для отладки
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("RegisterKtor", message)
                    }
                }
                level = LogLevel.ALL
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

                            if (response.status == HttpStatusCode.OK) {
                                val registerResponse: RegisterResponse = response.body()
                                Log.d("RegisterDebug", "Deserialized response: $registerResponse")

                                userPreferences.saveUserData(
                                    id = registerResponse.user.id,
                                    username = registerResponse.user.username,
                                    email = registerResponse.user.email,
                                    createdAt = "2025-05-21T12:15:00Z", // Assuming this is a static value for now
                                    token = registerResponse.token.access_token
                                )
                                Log.d(
                                    "RegisterDebug",
                                    "id=${registerResponse.user.id}, username=${registerResponse.user.username}, email=${registerResponse.user.email}, token=${registerResponse.token.access_token}"
                                )
                                onDismiss()
                            } else {
                                Log.e("RegisterDialog", "Registration failed: ${response.status}. Body: ${response.bodyAsText()}")
                            }

                        } catch (e: Exception) {
                            Log.e("RegisterDialog", "Error during registration: ${e.message}", e)
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