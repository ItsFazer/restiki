package com.example.myapplication

import androidx.compose.ui.text.font.FontFamily
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.ui.text.style.TextOverflow

data class Dish(
    val id: Int,
    val name: String,
    val description: String,
    val portion: String,
    val cost: Int,
    val imageResId: Int,
    val isNew: Boolean = false
)

// Предполагаем, что Montserrat FontFamily определен в другом файле и доступен
// Если нет, убедитесь, что он определен или скопируйте его сюда.
// val Montserrat = FontFamily(...)


class MainMenu : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                MainMenuScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishCard(
    dish: Dish,
    onPriceButtonClick: (Dish) -> Unit = {}, // Callback для клика по кнопке цены
    onCardClick: (Dish) -> Unit = {} // Оставлен, но не используется, так как вся карта не кликабельна
) {
    // Удален Card onClick, так как кликабельна только кнопка цены
    Card(
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 4.dp), // Уменьшены отступы карточки
        shape = RoundedCornerShape(12.dp), // Сделаем углы чуть более скругленными
        // Удален модификатор .shadow()
        colors = CardDefaults.cardColors(
            containerColor = Color.White // Фон карточки белый
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Убираем тень CardElevation
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp) // Увеличена высота изображения
            ) {
                Image(
                    painter = painterResource(id = dish.imageResId),
                    contentDescription = dish.name,
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)) // Скругляем только верхние углы изображения
                        .fillMaxWidth()
                        .height(180.dp), // Высота изображения
                    contentScale = ContentScale.Crop
                )

                if (dish.isNew) {
                    Surface(
                        color = Color(0xFFF3E5F5), // Светло-пурпурный фон метки
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "НОВИНКА",
                            color = Color.Black, // Цвет текста черный
                            fontSize = 9.sp, // Уменьшен размер шрифта метки
                            fontWeight = FontWeight.SemiBold, // Полужирное начертание
                            fontFamily = Montserrat, // Применяем шрифт Montserrat
                            modifier = Modifier.padding(
                                horizontal = 6.dp, // Уменьшены горизонтальные отступы метки
                                vertical = 3.dp // Уменьшены вертикальные отступы метки
                            )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp) // Уменьшены отступы в текстовом блоке
            ) {
                val textColor = Color(0xFF333333) // Темно-серый цвет для текста (можно использовать Color.Black)

                Text(
                    text = dish.name,
                    fontSize = 12.sp, // Уменьшен размер шрифта для названия
                    fontWeight = FontWeight.Normal, // Обычное начертание (можно сделать SemiBold для плотности)
                    fontFamily = Montserrat, // Применяем шрифт Montserrat
                    color = textColor,
                    lineHeight = 18.sp
                )
                //Spacer(modifier = Modifier.height(1.dp)) // Уменьшаем отступ после названия
                Text(
                    text = "${dish.portion} г.", // Добавим г. к порции
                    fontSize = 8.sp, // Уменьшен размер шрифта
                    fontWeight = FontWeight.Normal, // Обычное начертание
                    fontFamily = Montserrat, // Применяем шрифт Montserrat
                    color = textColor,
                    lineHeight = 12.sp
                )
                Spacer(modifier = Modifier.height(1.dp)) // Уменьшаем отступ после порции (было 2.dp)
                Text(
                    text = dish.description,
                    fontSize = 8.sp, // Уменьшен размер шрифта
                    fontWeight = FontWeight.Normal, // Обычное начертание
                    fontFamily = Montserrat, // Применяем шрифт Montserrat
                    color = textColor, // Применяем единый цвет
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 12.sp
                )

                Spacer(modifier = Modifier.height(4.dp)) // Уменьшаем отступ перед блоком с ценой (было 6.dp)

                // Кликабельный блок для цены и стрелки
                Row(
                    modifier = Modifier
                        // Удален .fillMaxWidth(), чтобы блок был по ширине содержимого
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEEEEEE)) // Светло-серый фон
                        .clickable { onPriceButtonClick(dish) } // Сделали блок кликабельным
                        .padding(horizontal = 8.dp, vertical = 6.dp), // Отступы внутри кликабельного блока
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "от ${dish.cost}₽",
                        fontWeight = FontWeight.Normal, // Обычное начертание цены
                        fontSize = 12.sp, // Уменьшен размер шрифта для цены
                        fontFamily = Montserrat, // Применяем шрифт Montserrat
                        color = MaterialTheme.colorScheme.primary // Цвет цены - акцентный
                    )
                    Spacer(modifier = Modifier.width(4.dp)) // Небольшой отступ между текстом и стрелкой
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Перейти к блюду ${dish.name}",
                        tint = MaterialTheme.colorScheme.primary, // Цвет стрелки - акцентный
                        modifier = Modifier.size(16.dp) // Уменьшен размер стрелки
                    )
                }
            }
        }
    }
}

@Composable
fun MainMenuScreen() {
    val singleDish = Dish(
        id = 1,
        name = "Шашлык к пиву",
        description = "Очень вкусный шашлык, приготовленный по особому рецепту с дымком.",
        portion = "400",
        cost = 300,
        imageResId = R.drawable.shahlisk, // Убедитесь, что у вас есть ресурс R.drawable.shahlisk
        isNew = true
    )
    val dishesList = List(18) { index ->
        singleDish.copy(id = index, name = "Блюдо ${index + 1}")
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(Color(255, 255, 255)),
        //contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp), // Уменьшен общий отступ сетки
        horizontalArrangement = Arrangement.spacedBy(0.dp), // Уменьшен горизонтальный отступ между карточками
        verticalArrangement = Arrangement.spacedBy(0.dp), // Уменьшен вертикальный отступ между карточками
    ) {
        // Header items
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Доставим сюда:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "улица Студенческая, 26",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Thin,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable {
                        // TODO: Обработка клика по адресу доставки
                        println("Клик по адресу доставки")
                    } // Сделали адрес кликабельным
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "Рекомендуем",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp,8.dp),
                style = MaterialTheme.typography.headlineSmall // Можно оставить стандартный размер для заголовка секции
            )
        }

        // Dish items
        items(dishesList, key = { it.id }) { dish ->
            DishCard(
                dish = dish,
                onPriceButtonClick = { clickedDish ->
                    // TODO: Обработка клика по кнопке "от n рублей"
                    println("Нажата кнопка цены для блюда: ${clickedDish.name}")
                }
                // onCardClick больше не используется, так как вся карта не кликабельна
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuPreview() { // Переименован Preview, если это файл MainMenu.kt
    MyApplicationTheme {
        MainMenuScreen()
    }
}