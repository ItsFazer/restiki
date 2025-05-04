package com.example.myapplication

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
            .padding(horizontal = 4.dp, vertical = 8.dp),
        shape = RoundedCornerShape(10.dp),
        onClick = { onCardClick(dish) },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Image(
                    painter = painterResource(id = dish.imageResId),
                    contentDescription = dish.name,
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                    contentScale = ContentScale.Crop
                )

                if (dish.isNew) {
                    Surface(
                        color = Color(0xFFE0B0FF), // Фон метки "Новинка"
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "НОВИНКА",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(
                                horizontal = 6.dp,
                                vertical = 2.dp
                            )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = dish.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dish.portion + " г.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dish.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F1F1))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "от ${dish.cost}₽",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Перейти к блюду ${dish.name}",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
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
        description = "Ехала ехала",
        portion = "400",
        cost = 300,
        imageResId = R.drawable.shahlisk,
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
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Header items
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Доставим сюда:",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "улица Студенческая, 26",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "Новинки",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp,8.dp),
                style = MaterialTheme.typography.headlineSmall
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