package com.example.myapplication

import androidx.compose.ui.text.font.FontFamily
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
fun DishCard(dish: Dish, onCardClick: (Dish) -> Unit = {}) {
    Card(
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 4.dp) // Уменьшим вертикальные и горизонтальные отступы карточки
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp)), // Добавим легкую тень
        shape = RoundedCornerShape(12.dp), // Сделаем углы чуть более скругленными
        onClick = { onCardClick(dish) },
        colors = CardDefaults.cardColors(
            containerColor = Color.White // Фон карточки белый
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Тень задали выше модификатором shadow
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp) // Увеличим высоту изображения для большей картинки
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
                            fontSize = 9.sp, // Уменьшим размер шрифта метки
                            fontWeight = FontWeight.SemiBold, // Полужирное начертание
                            fontFamily = Montserrat, // Применяем шрифт Montserrat
                            modifier = Modifier.padding(
                                horizontal = 6.dp, // Уменьшим горизонтальные отступы метки
                                vertical = 3.dp // Уменьшим вертикальные отступы метки
                            )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp) // Уменьшим горизонтальные и вертикальные отступы в текстовом блоке
            ) {
                Text(
                    text = dish.name,
                    fontSize = 13.sp, // Уменьшим размер шрифта для названия
                    fontWeight = FontWeight.Medium,
                    fontFamily = Montserrat, // Применяем шрифт Montserrat
                    color = MaterialTheme.colorScheme.onSurface // Цвет текста
                )
                Spacer(modifier = Modifier.height(1.dp)) // Уменьшим отступ после названия
                Text(
                    text = dish.portion + " г.",
                    fontSize = 11.sp, // Уменьшим размер шрифта для порции
                    fontWeight = FontWeight.Normal,
                    fontFamily = Montserrat, // Применяем шрифт Montserrat
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Цвет текста серый
                )
                Spacer(modifier = Modifier.height(2.dp)) // Уменьшим отступ после порции
                Text(
                    text = dish.description,
                    fontSize = 11.sp, // Уменьшим размер шрифта для описания
                    fontWeight = FontWeight.Normal,
                    fontFamily = Montserrat, // Применяем шрифт Montserrat
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Цвет текста серый
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp)) // Уменьшим отступ перед блоком с ценой

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEEEEEE)) // Светло-серый фон
                        .padding(horizontal = 8.dp, vertical = 6.dp), // Уменьшим отступы внутри блока с ценой
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "от ${dish.cost}₽",
                        fontWeight = FontWeight.Bold, // Жирное начертание цены
                        fontSize = 13.sp, // Уменьшим размер шрифта для цены
                        fontFamily = Montserrat, // Применяем шрифт Montserrat
                        color = MaterialTheme.colorScheme.onSurface // Цвет текста цены черный
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Перейти к блюду ${dish.name}",
                        tint = MaterialTheme.colorScheme.primary, // Цвет стрелки
                        modifier = Modifier.size(20.dp) // Уменьшим размер стрелки
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
        description = "Ехала ехалафффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффффф",
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
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp), // Уменьшим общий отступ сетки
        horizontalArrangement = Arrangement.spacedBy(4.dp), // Уменьшим горизонтальный отступ между карточками
        verticalArrangement = Arrangement.spacedBy(4.dp), // Уменьшим вертикальный отступ между карточками
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
                    color = MaterialTheme.colorScheme.onSurface
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
            DishCard(dish = dish) { clickedDish ->
                println("Нажато на блюдо: ${clickedDish.name}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        MainMenuScreen()
    }
}