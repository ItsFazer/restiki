package com.example.myapplication

import android.os.Bundle
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

// Data classes (Updated for consistency and imageUrl)
data class AddOn(
    val id: Int,
    val name: String,
    val price: Double,
    var isSelected: Boolean = false // State for selection
)

data class Dish(
    val id: Int,
    val name: String,
    val weight: String, // Using 'weight' instead of 'portion' for consistency with DishDetailCard
    val isNew: Boolean = false,
    val description: String,
    val imageUrl: String?, // Using imageUrl
    val basePrice: Double, // Using 'basePrice' instead of 'cost'
    val addOns: List<AddOn> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishDetailCard(
    dish: Dish,
    onDismiss: () -> Unit,
    onAddToCart: (Dish, List<AddOn>) -> Unit // Callback when "Add to Cart" is clicked
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Manage selected add-ons within the sheet's state
    var selectedAddOns by remember { mutableStateOf(dish.addOns.map { it.copy() }) }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        dragHandle = null, // You can add a custom drag handle if needed
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                // Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                // Dish Image
                val imageModel = dish.imageUrl
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(imageModel) // Use the determined imageModel
                            // Добавляем placeholder и error изображение для Coil
                            // Убедитесь, что у вас есть ресурс, например, R.drawable.placeholder_image
                            // Для демонстрации используем стандартную иконку лаунчера.
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
                    fontFamily = Montserrat, // Здесь используется Montserrat
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${dish.weight} г.", // Added ' г.' here for consistency
                    fontSize = 16.sp,
                    color = Color.Gray,
                    fontFamily = Montserrat, // Здесь используется Montserrat
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (dish.isNew) {
                    Text(
                        text = "НОВИНКА",
                        fontSize = 12.sp,
                        fontFamily = Montserrat, // Здесь используется Montserrat
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .background(Color(0xFF8A2BE2), RoundedCornerShape(4.dp)) // Example color (purple)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Description (Added TextOverflow.Ellipsis if needed)
                Text(
                    text = dish.description,
                    fontSize = 14.sp,
                    fontFamily = Montserrat, // Здесь используется Montserrat
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3, // Limit description lines
                    overflow = TextOverflow.Ellipsis
                )

                if (dish.addOns.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Add-ons section header
                    Text(
                        text = "Добавки несладкие", // Or dynamically set
                        fontSize = 18.sp,
                        fontFamily = Montserrat, // Здесь используется Montserrat
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Add-ons list
                    Column {
                        selectedAddOns.forEachIndexed { index, addOn ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedAddOns = selectedAddOns.toMutableList().apply {
                                            this[index] = addOn.copy(isSelected = !addOn.isSelected)
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = addOn.name,
                                        fontSize = 16.sp,
                                        fontFamily = Montserrat // Здесь используется Montserrat
                                    )
                                    Text(
                                        text = "+${addOn.price.toInt()} ₽",
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        fontFamily = Montserrat // Здесь используется Montserrat
                                    )
                                }
                                Checkbox(
                                    checked = addOn.isSelected,
                                    onCheckedChange = { isChecked ->
                                        selectedAddOns = selectedAddOns.toMutableList().apply {
                                            this[index] = addOn.copy(isSelected = isChecked)
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = Color.Black)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Total price and Add to Cart button
                val totalSum = dish.basePrice + selectedAddOns.filter { it.isSelected }.sumOf { it.price }

                Button(
                    onClick = {
                        onAddToCart(dish, selectedAddOns.filter { it.isSelected })
                        // Optionally close the sheet after adding to cart
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onDismiss() // Dismiss after animation
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)), // Example color (orange)
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${totalSum.toInt()} ₽",
                        fontSize = 18.sp,
                        fontFamily = Montserrat, // Здесь используется Montserrat
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    )
}

// Остальная часть кода (DishCard, MainMenuScreen, Preview) остается без изменений
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishCard(
    dish: Dish,
    onPriceButtonClick: (Dish) -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .fillMaxWidth(), // Ensure card fills its grid cell width
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
                // Load image from URL using Coil, handle null URL
                val imageModel = dish.imageUrl
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(imageModel) // Use the determined imageModel
                            // Добавляем placeholder и error изображение для Coil
                            // Убедитесь, что у вас есть ресурс, например, R.drawable.placeholder_image
                            // Для демонстрации используем стандартную иконку лаунчера.
                            .build()
                    ),
                    contentDescription = dish.name,
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .fillMaxSize(), // Image fills the Box
                    contentScale = ContentScale.Crop
                )

                if (dish.isNew) {
                    Surface(
                        color = Color(0xFFF3E5F5),
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "НОВИНКА",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Montserrat, // Здесь используется Montserrat
                            modifier = Modifier.padding(
                                horizontal = 6.dp,
                                vertical = 3.dp
                            )
                        )
                    }
                }
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
                    fontFamily = Montserrat, // Здесь используется Montserrat
                    color = textColor,
                    lineHeight = 18.sp,
                    maxLines = 1, // Prevent name from wrapping too much
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${dish.weight} г.",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = Montserrat, // Здесь используется Montserrat
                    color = textColor,
                    lineHeight = 12.sp
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = dish.description,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = Montserrat, // Здесь используется Montserrat
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
                        text = "от ${dish.basePrice.toInt()}₽", // Use basePrice
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        fontFamily = Montserrat, // Здесь используется Montserrat
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
    // State to control the visibility of the bottom sheet and which dish is selected
    var selectedDish: Dish? by remember { mutableStateOf(null) }
    val showDishDetailSheet by remember { derivedStateOf { selectedDish != null } }

    // Sample data (replace with your actual data loading logic)
    val sampleDishes = remember {
        listOf(
            Dish(
                id = 1,
                name = "Мафальдине болоньезе",
                weight = "350",
                isNew = true,
                description = "Итальянская паста мафальдине в соусе Болоньезе, с вялеными томатами. Щедро посыпанная сыром пармезан.",
                imageUrl = "https://bolshoy.rest/userfls/bg/shop/goods_list/2543_vozdushnyy-khumus.png?v4", // Пример URL
                basePrice = 690.0,
                addOns = listOf(
                    AddOn(1, "Соус из печеного перца 50 г", 100.0),
                    AddOn(2, "Авокадо", 200.0),
                    AddOn(3, "Хлеб белый", 70.0)
                )
            ),
            Dish(
                id = 2,
                name = "Шашлык к пиву",
                weight = "400",
                isNew = false,
                description = "Очень вкусный шашлык, приготовленный по особому рецепту с дымком.",
                imageUrl = "https://bolshoy.rest/userfls/bg/shop/goods_list/2224_mtsvadi-из-свинины-.jpg?v4", // Пример URL
                basePrice = 300.0,
                addOns = emptyList() // No add-ons for this dish
            ),
            Dish(
                id = 3,
                name = "Цезарь с курицей",
                weight = "250",
                isNew = true,
                description = "Классический салат Цезарь с нежным куриным филе, гренками и соусом.",
                imageUrl = "https://bolshoy.rest/userfls/bg/shop/goods_list/2224_mtsvadi-из-свинины-.jpg?v4", // Пример URL
                basePrice = 450.0,
                addOns = listOf(
                    AddOn(4, "Дополнительная курица", 150.0),
                    AddOn(5, "Анчоусы", 80.0)
                )
            ),
            // Добавьте больше блюд здесь с реальными URL изображений
        )
    }

    val dishesList = List(18) { index ->
        // Пример генерации данных для сетки, используем данные из sampleDishes по кругу
        sampleDishes[index % sampleDishes.size].copy(id = index + 100) // Уникальный ID
    }

    val real_dishes_list = List(500) {
        index: Int -> 
    }


    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(Color(255, 255, 255)),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp) // Добавлен горизонтальный отступ для сетки
    ) {
        // Header items
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp) // Увеличиваем отступы для верхнего блока
            ) {
                Text(
                    text = "Доставим сюда:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = Montserrat // Здесь используется Montserrat
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "улица Студенческая, 26",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Thin, // Используем Thin для более легкого начертания
                    fontFamily = Montserrat, // Здесь используется Montserrat
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable {
                        // TODO: Обработка клика по адресу доставки
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
                    .padding(horizontal = 8.dp, vertical = 8.dp), // Увеличиваем отступы для заголовка "Рекомендуем"
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = Montserrat // Здесь используется Montserrat
            )
        }

        // Dish items
        items(dishesList, key = { it.id }) { dish ->
            DishCard(
                dish = dish,
                onPriceButtonClick = { clickedDish ->
                    selectedDish = clickedDish // Show the bottom sheet
                }
            )
        }
    }

    // Show the bottom sheet when selectedDish is not null
    if (showDishDetailSheet) {
        selectedDish?.let { dish -> // Более безопасный способ обработки nullable selectedDish
            DishDetailCard(
                dish = dish, // Теперь 'dish' гарантированно не null
                onDismiss = { selectedDish = null }, // Hide the bottom sheet
                onAddToCart = { addedDish, addOns -> // Переименован параметр, чтобы избежать конфликта имен
                    // TODO: Implement logic to add the dish with selected add-ons to the cart
                    println("Added to cart: ${addedDish.name} with add-ons: ${addOns.joinToString { it.name }}")
                }
            )
        }
    }
}

// Preview Composable (Optional)
@Preview(showBackground = true)
@Composable
fun MainMenuPreview() {
    MyApplicationTheme {
        MainMenuScreen()
    }
}