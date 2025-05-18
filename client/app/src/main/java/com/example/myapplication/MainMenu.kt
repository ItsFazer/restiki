package com.example.myapplication

import android.os.Bundle
import android.util.Log // Импорт для Logcat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.myapplication.MainViewModel.client // Assuming MainViewModel exists and client is public
import com.example.myapplication.ui.theme.MyApplicationTheme
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import java.io.IOException

// Data classes
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
    val img: String? // Поле img теперь String?, для обработки возможных null значений
)

// UiState для управления состоянием экрана
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
    onAddToCart: (Dish, List<AddOn>) -> Unit
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
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(24.dp))

                val totalSum = dish.cost
                Button(
                    onClick = {
                        onAddToCart(dish, emptyList())
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
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
                        text = "${totalSum.toInt()} ₽",
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
                        text = "от ${dish.cost.toInt()}₽",
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

@Composable
fun MainMenuScreen() {
    var selectedDish: Dish? by remember { mutableStateOf(null) }
    val showDishDetailSheet by remember { derivedStateOf { selectedDish != null } }

    // uiState будет управлять отображением: загрузка, успех, ошибка, пустота
    var uiState: UiState by remember { mutableStateOf(UiState.Loading) }

    val scope = rememberCoroutineScope()

    // LaunchedEffect запускает загрузку данных только один раз при первом отображении экрана
    LaunchedEffect(Unit) {
        uiState = UiState.Loading // Убедимся, что состояние загрузки установлено до начала загрузки
        try {
            val response: HttpResponse = MainViewModel.client.get("http://5.167.254.44:6567/dish")

            if (response.status.value == 200) {
                val fetchedList = response.body<List<Dish>>()
                Log.d("MainMenuScreen", "Количество загруженных блюд: ${fetchedList.size}")
                if (fetchedList.isEmpty()) {
                    uiState = UiState.Empty // Если список пуст
                } else {
                    uiState = UiState.Success(fetchedList) // Успешная загрузка данных
                    // Для отладки: выводим URL-адреса изображений
                    fetchedList.forEachIndexed { index, dish ->
                        Log.d(
                            "MainMenuScreen",
                            "Блюдо $index: ${dish.name}, URL изображения: ${dish.img}"
                        )
                    }
                }
            } else {
                val errorBody = response.body<String>()
                Log.e(
                    "MainMenuScreen",
                    "Ошибка при загрузке блюд: ${response.status.value}, Тело ответа: $errorBody"
                )
                uiState = UiState.Error("Ошибка загрузки данных: ${response.status.value}")
            }
        } catch (e: SerializationException) {
            Log.e("MainMenuScreen", "Ошибка десериализации JSON: ${e.message}", e)
            uiState = UiState.Error("Ошибка обработки данных: ${e.message}")
        } catch (e: IOException) {
            Log.e("MainMenuScreen", "Сетевая ошибка: ${e.message}", e)
            uiState = UiState.Error("Проверьте подключение к интернету: ${e.message}")
        } catch (e: Exception) {
            Log.e("MainMenuScreen", "Неизвестная ошибка: ${e.message}", e)
            uiState = UiState.Error("Произошла неизвестная ошибка: ${e.message}")
        }
    }


    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(Color(255, 255, 255)),
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

        // Отображение контента в зависимости от UiState
        when (uiState) {
            UiState.Loading -> {
                // Отображаем индикатор загрузки на весь экран сетки
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp), // Можно настроить высоту
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            is UiState.Success -> {
                val dishes = (uiState as UiState.Success).dishes
                // Отображаем все блюда, когда данные успешно загружены
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
                // Отображаем сообщение об ошибке на весь экран сетки
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp), // Можно настроить высоту
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
                // Отображаем сообщение о пустом списке на весь экран сетки
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp), // Можно настроить высоту
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Список блюд пуст.")
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
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuPreview() {
    MyApplicationTheme {
        MainMenuScreen()
    }
}