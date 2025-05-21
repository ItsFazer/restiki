package com.example.myapplication

import UserPreferences
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.Database
import com.example.Orders
import com.example.OrderQueries
import com.example.myapplication.ui.theme.MyApplicationTheme
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import java.io.IOException
import com.example.myapplication.BonusCard

// Модели данных
@Serializable
data class AddOn(
    val id: Int,
    val name: String,
    val price: Double,
    var isSelected: Boolean = false
)

@Serializable
data class Dish(
    val id: Int,
    val name: String,
    val description: String,
    val portion: Int,
    val cost: Int,
    val tag: String?,
    val img: String?
)

@Serializable
data class OrderDish(
    val dish_id: Long,
    val quantity: Long
)

@Serializable
data class OrderRequest(
    val user_id: Long,
    val dishes: List<OrderDish>,
    val status: String = "pending"
)

@Serializable
data class OrderResponse(
    val id: Long,
    val user_id: Long,
    val order_date: String,
    val status: String,
    val dishes: List<OrderDish>
)

sealed class UiState {
    object Loading : UiState()
    data class Success(val dishes: List<Dish>) : UiState()
    data class Error(val message: String) : UiState()
    object Empty : UiState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishDetailCard(
    dish: Dish,
    onDismiss: () -> Unit,
    onAddToCart: (Dish, List<AddOn>) -> Unit,
    viewModel: MainMenuViewModel = viewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        dragHandle = null,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                val imageModel = dish.img
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(imageModel)
                            .build()
                    ),
                    contentDescription = dish.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = dish.name,
                    fontSize = 24.sp,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${dish.portion} г.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    fontFamily = Montserrat,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = dish.description,
                    fontSize = 14.sp,
                    fontFamily = Montserrat,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(24.dp))

                val totalSum = dish.cost
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                viewModel.addOrder(dish)
                                onAddToCart(dish, emptyList())
                                sheetState.hide()
                            } catch (e: Exception) {
                                Log.e(
                                    "DishDetailCard",
                                    "Ошибка при добавлении заказа: ${e.message}",
                                    e
                                )
                            }
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onDismiss()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${totalSum} ₽",
                        fontSize = 18.sp,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersSummaryCard(
    onDismiss: () -> Unit,
    viewModel: MainMenuViewModel,
    uiState: UiState
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val orders by viewModel.orders.collectAsState()
    val utensilsCount by viewModel.utensilsCount.collectAsState()
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val token by userPreferences.token.collectAsState(initial = null)
    var showLoginDialog by remember { mutableStateOf(false) }

    val bonusBalance: StateFlow<Int> = bonus_card.balance
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        dragHandle = null,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ваш заказ (${orders.size} позиции)",
                        fontSize = 24.sp,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Очистить",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                scope.launch {
                                    viewModel.clearDatabase()
                                    sheetState.hide()
                                    onDismiss()
                                }
                            }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (orders.isEmpty() && utensilsCount == 0) {
                    Text(
                        text = "Корзина пуста",
                        fontSize = 16.sp,
                        fontFamily = Montserrat,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        if (orders.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Блюда",
                                    fontSize = 18.sp,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            itemsIndexed(orders) { _, order ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(50.dp)) {
                                        Image(
                                            painter = rememberAsyncImagePainter(order.imageUrl),
                                            contentDescription = order.dishName,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row {
                                            Text(
                                                text = order.dishName,
                                                fontSize = 16.sp,
                                                fontFamily = Montserrat,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                modifier = Modifier.fillMaxWidth(0.7F),
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${order.dishPortion} г.",
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(
                                                    start = 3.dp,
                                                    top = 2.dp
                                                ),
                                                fontWeight = FontWeight.Normal,
                                                fontFamily = Montserrat,
                                                color = Color(0x9E020202)
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "${order.dishCost * order.counter} ₽",
                                                fontSize = 16.sp,
                                                fontFamily = Montserrat,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        viewModel.decrementOrderCounter(
                                                            order.id
                                                        )
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Text(
                                                        text = "–",
                                                        fontSize = 16.sp,
                                                        fontFamily = Montserrat,
                                                        color = Color.Gray
                                                    )
                                                }
                                                Text(
                                                    text = "${order.counter}",
                                                    fontSize = 16.sp,
                                                    fontFamily = Montserrat,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                IconButton(
                                                    onClick = {
                                                        viewModel.incrementOrderCounter(
                                                            order.id
                                                        )
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Text(
                                                        text = "+",
                                                        fontSize = 16.sp,
                                                        fontFamily = Montserrat,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))

                        Row {
                            Text(
                                text = "Приборы",
                                fontSize = 16.sp,
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(modifier = Modifier.weight(1f))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    IconButton(
                                        onClick = { viewModel.decrementUtensilCounter() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Text(
                                            text = "–",
                                            fontSize = 16.sp,
                                            fontFamily = Montserrat,
                                            color = Color.Gray
                                        )
                                    }
                                    Text(
                                        text = "$utensilsCount",
                                        fontSize = 16.sp,
                                        fontFamily = Montserrat,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { viewModel.incrementUtensilCounter() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Text(
                                            text = "+",
                                            fontSize = 16.sp,
                                            fontFamily = Montserrat,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                if (token == null) {
                                    showLoginDialog = true
                                } else {
                                    bonus_card.addPoints((0.05 * orders.sumOf { it.dishCost.toInt() * it.counter }).toInt())
                                    scope.launch {
                                        val userId = userPreferences.id.firstOrNull()
                                        val tokenValue = userPreferences.token.firstOrNull()
                                            orders.forEachIndexed { index, order ->
                                                Log.d(
                                                    "OrdersSummaryCard",
                                                    "Dish ${index + 1}: ${order.dishName} (${order.dishPortion} г, ${order.dishCost} ₽, Quantity: ${order.counter})"
                                                )
                                            }

                                            if (userId != null && tokenValue != null) {
                                                val orderRequest = OrderRequest(
                                                    user_id = userId.toLong(),
                                                    dishes = orders.map { order ->
                                                        OrderDish(
                                                            dish_id = order.dishId,
                                                            quantity = order.counter
                                                        )
                                                    },
                                                    status = "pending"
                                                )
                                                viewModel.submitOrder(orderRequest, tokenValue, onDismiss)
                                            }

                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Заказать ${orders.sumOf { it.dishCost.toInt() * it.counter }} ₽",
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    )

    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            title = {
                Text(
                    text = "Вход требуется",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    fontFamily = Montserrat
                )
            },
            text = {
                Text(
                    text = "Пожалуйста, войдите или зарегистрируйтесь, чтобы оформить заказ.",
                    fontSize = 16.sp,
                    color = Color(0xFF757575),
                    fontFamily = Montserrat,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { showLoginDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE8C65)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Войти",
                        color = Color.White,
                        fontFamily = Montserrat
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginDialog = false }) {
                    Text(
                        text = "Отмена",
                        color = Color(0xFF757575),
                        fontFamily = Montserrat
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun DishCard(
    dish: Dish,
    onPriceButtonClick: (Dish) -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val imageModel = dish.img
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(imageModel)
                            .build()
                    ),
                    contentDescription = dish.name,
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                val textColor = Color(0xFF333333)

                Text(
                    text = dish.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = Montserrat,
                    color = textColor,
                    lineHeight = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${dish.portion} г.",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = Montserrat,
                    color = textColor,
                    lineHeight = 12.sp
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = dish.description,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = Montserrat,
                    color = textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 12.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEEEEEE))
                        .clickable { onPriceButtonClick(dish) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "от ${dish.cost}₽",
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        fontFamily = Montserrat,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Перейти к блюду ${dish.name}",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

object MainMenuViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState
    private val _hasOrders = MutableStateFlow(false)
    val hasOrders: StateFlow<Boolean> = _hasOrders
    private val _orders = MutableStateFlow<List<Orders>>(emptyList())
    val orders: StateFlow<List<Orders>> = _orders
    private val _utensilsCount = MutableStateFlow(0)
    val utensilsCount: StateFlow<Int> = _utensilsCount

    private lateinit var database: Database
    private lateinit var orderQueries: OrderQueries

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
            json()
        }
    }

    fun initializeDatabase(context: android.content.Context) {
        database = Database_s.getDatabase(context)
        orderQueries = database.orderQueries
        checkOrders()
        fetchDishes()
    }

    fun clearDatabase() {
        viewModelScope.launch {
            try {
                orderQueries.deleteAllOrders()
                _utensilsCount.value = 0
                Log.d("MainMenuViewModel", "База данных заказов очищена, приборы сброшены")
                _orders.value = emptyList()
                checkOrders()
            } catch (e: Exception) {
                Log.e("MainMenuViewModel", "Ошибка при очистке базы данных: ${e.message}", e)
            }
        }
    }

    fun checkOrders() {
        viewModelScope.launch {
            try {
                val ordersList = orderQueries.selectAllOrders().executeAsList()
                _hasOrders.value = ordersList.isNotEmpty()
                _orders.value = ordersList
            } catch (e: Exception) {
                Log.e("MainMenuViewModel", "Ошибка при проверке заказов: ${e.message}", e)
            }
        }
    }

    fun fetchDishes() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response: HttpResponse = client.get("http://5.167.254.44:6567/dish")
                if (response.status.value == 200) {
                    val fetchedList = response.body<List<Dish>>()
                    Log.d("MainMenuViewModel", "Количество загруженных блюд: ${fetchedList.size}")
                    if (fetchedList.isEmpty()) {
                        _uiState.value = UiState.Empty
                    } else {
                        _uiState.value = UiState.Success(fetchedList)
                        fetchedList.forEachIndexed { index, dish ->
                            Log.d(
                                "MainMenuViewModel",
                                "Блюдо $index: ${dish.name}, URL изображения: ${dish.img}"
                            )
                        }
                    }
                } else {
                    val errorBody = response.body<String>()
                    Log.e(
                        "MainMenuViewModel",
                        "Ошибка при загрузке блюд: ${response.status.value}, Тело ответа: $errorBody"
                    )
                    _uiState.value =
                        UiState.Error("Ошибка загрузки данных: ${response.status.value}")
                }
            } catch (e: SerializationException) {
                Log.e("MainMenuViewModel", "Ошибка десериализации JSON: ${e.message}", e)
                _uiState.value = UiState.Error("Ошибка обработки данных: ${e.message}")
            } catch (e: IOException) {
                Log.e("MainMenuViewModel", "Сетевая ошибка: ${e.message}", e)
                _uiState.value = UiState.Error("Проверьте подключение к интернету: ${e.message}")
            } catch (e: Exception) {
                Log.e("MainMenuViewModel", "Неизвестная ошибка: ${e.message}", e)
                _uiState.value = UiState.Error("Произошла неизвестная ошибка: ${e.message}")
            }
        }
    }

    fun addOrder(dish: Dish) {
        viewModelScope.launch {
            try {
                val existingOrder = orderQueries.selectAllOrders().executeAsList()
                    .find { it.dishId == dish.id.toLong() }
                if (existingOrder != null) {
                    orderQueries.incrementCounter(existingOrder.id)
                    Log.d("MainMenuViewModel", "Увеличено количество для ${dish.name}")
                } else {
                    orderQueries.insertOrder(
                        dishId = dish.id.toLong(),
                        dishName = dish.name,
                        dishCost = dish.cost.toLong(),
                        imageUrl = dish.img,
                        dishPortion = dish.portion.toLong(),
                        counter = 1
                    )
                    Log.d(
                        "MainMenuViewModel",
                        "Добавлен заказ: ${dish.name}, ID: ${dish.id}, Стоимость: ${dish.cost}, ImageUrl: ${dish.img}"
                    )
                }
                checkOrders()
            } catch (e: Exception) {
                Log.e("MainMenuViewModel", "Ошибка при работе с базой данных: ${e.message}", e)
            }
        }
    }

    fun submitOrder(orderRequest: OrderRequest, token: String, onDismiss: () -> Unit) {
        viewModelScope.launch {
            try {
                val response: HttpResponse = client.post("http://5.167.254.44:6567/order") {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer $token")
                    setBody(orderRequest)
                }
                if (response.status.value == 200) {
                    val orderResponse: OrderResponse = response.body()
                    Log.d("OrdersSummaryCard", "Order submitted successfully:")
                    Log.d("OrdersSummaryCard", "Order ID: ${orderResponse.id}")
                    Log.d("OrdersSummaryCard", "User ID: ${orderResponse.user_id}")
                    Log.d("OrdersSummaryCard", "Order Date: ${orderResponse.order_date}")
                    Log.d("OrdersSummaryCard", "Status: ${orderResponse.status}")
                    Log.d("OrdersSummaryCard", "Dishes:")
                    orderResponse.dishes.forEachIndexed { index, dish ->
                        Log.d(
                            "OrdersSummaryCard",
                            "Dish ${index + 1}: ID ${dish.dish_id}, Quantity: ${dish.quantity}"
                        )
                    }
                    clearDatabase()
                    onDismiss()
                } else {
                    val errorBody = response.body<String>()
                    Log.e(
                        "OrdersSummaryCard",
                        "Ошибка при отправке заказа: ${response.status.value}, Тело ответа: $errorBody"
                    )
                }
            } catch (e: SerializationException) {
                Log.e("OrdersSummaryCard", "Ошибка десериализации ответа: ${e.message}", e)
            } catch (e: IOException) {
                Log.e("OrdersSummaryCard", "Сетевая ошибка при отправке заказа: ${e.message}", e)
            } catch (e: Exception) {
                Log.e("OrdersSummaryCard", "Неизвестная ошибка при отправке заказа: ${e.message}", e)
            }
        }
    }

    fun incrementOrderCounter(orderId: Long) {
        viewModelScope.launch {
            try {
                orderQueries.incrementCounter(orderId)
                Log.d("MainMenuViewModel", "Увеличено количество для заказа ID: $orderId")
                checkOrders()
            } catch (e: Exception) {
                Log.e("MainMenuViewModel", "Ошибка при увеличении количества: ${e.message}", e)
            }
        }
    }

    fun decrementOrderCounter(orderId: Long) {
        viewModelScope.launch {
            try {
                val order = orderQueries.selectAllOrders().executeAsList().find { it.id == orderId }
                if (order != null) {
                    if (order.counter > 1) {
                        orderQueries.decrementCounter(orderId)
                        Log.d("MainMenuViewModel", "Уменьшено количество для заказа ID: $orderId")
                    } else {
                        orderQueries.deleteOrderById(orderId)
                        Log.d(
                            "MainMenuViewModel",
                            "Удален заказ ID: $orderId, так как количество стало 0"
                        )
                    }
                    checkOrders()
                }
            } catch (e: Exception) {
                Log.e("MainMenuViewModel", "Ошибка при уменьшении количества: ${e.message}", e)
            }
        }
    }

    fun incrementUtensilCounter() {
        _utensilsCount.value += 1
    }

    fun decrementUtensilCounter() {
        if (_utensilsCount.value > 0) {
            _utensilsCount.value -= 1
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
        Log.d("MainMenuViewModel", "HttpClient закрыт.")
    }
}

@Composable
fun MainMenuScreen(viewModel: MainMenuViewModel = viewModel()) {
    var selectedDish: Dish? by remember { mutableStateOf(null) }
    var showOrdersSummary by remember { mutableStateOf(false) }
    val showDishDetailSheet by remember { derivedStateOf { selectedDish != null } }
    val uiState by viewModel.uiState.collectAsState()
    val hasOrders by viewModel.hasOrders.collectAsState()
    val context = LocalContext.current.applicationContext

    LaunchedEffect(Unit) {
        viewModel.initializeDatabase(context)
    }

    Scaffold(
        floatingActionButton = {
            if (hasOrders) {
                val orders = viewModel.orders.collectAsState().value
                val totalCost = orders.sumOf { it.dishCost.toInt() * it.counter }
                val orderCount = orders.size

                Box(
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxWidth(0.8f)
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Color(254,198,113)
                        )
                        .clickable { showOrdersSummary = true }
                        .padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .background(
                                            Color(254,198,113)
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "Корзина",
                                        modifier = Modifier.size(30.dp).padding(start = 10.dp)
                                    )
                                    Text(
                                        text = "$orderCount",
                                        fontSize = 20.sp,
                                        fontFamily = Montserrat,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Black
                                    )
                                }
                            }
                            Text(
                                text = "Корзина",
                                fontSize = 20.sp,
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black
                            )
                        }
                        Text(
                            text = "$totalCost ₽",
                            fontSize = 20.sp,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black
                        )
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .background(Color(255, 255, 255))
                .padding(paddingValues),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(
                horizontal = 4.dp,
                vertical = 0.dp
            )
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 8.dp,
                            vertical = 8.dp
                        )
                ) {
                    Text(
                        text = "Доставим сюда:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = Montserrat
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "улица Студенческая, 26",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Thin,
                        fontFamily = Montserrat,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable {
                            println("Клик по адресу доставки")
                        }
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Рекомендуем",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 8.dp,
                            vertical = 8.dp
                        ),
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = Montserrat
                )
            }

            when (uiState) {
                UiState.Loading -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is UiState.Success -> {
                    val dishes = (uiState as UiState.Success).dishes
                    items(dishes, key = { it.id }) { dish ->
                        DishCard(
                            dish = dish,
                            onPriceButtonClick = { clickedDish ->
                                selectedDish = clickedDish
                            }
                        )
                    }
                }

                is UiState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ошибка: ${(uiState as UiState.Error).message}",
                                color = Color.Red
                            )
                        }
                    }
                }

                UiState.Empty -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Список блюд пуст.")
                        }
                    }
                }
            }
        }
    }

    if (showDishDetailSheet) {
        selectedDish?.let { dish ->
            DishDetailCard(
                dish = dish,
                onDismiss = { selectedDish = null },
                onAddToCart = { addedDish, addOns ->
                    Log.d("MainMenuScreen", "Добавлено в корзину: ${addedDish.name}")
                },
                viewModel = viewModel
            )
        }
    }

    if (showOrdersSummary) {
        OrdersSummaryCard(
            onDismiss = { showOrdersSummary = false },
            viewModel = viewModel,
            uiState = uiState
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuPreview() {
    MyApplicationTheme {
        MainMenuScreen()
    }
}