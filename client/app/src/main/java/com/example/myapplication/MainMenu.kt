package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme


class MainMenu : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainMenuScreen()
                }
            }
        }
    }
}

data class Dish(
    val name: String,
    val description: String,
    val portion: String,
    val cost: Int,
    val imageResId: Int
) : DishData {
    override fun is_new(): Boolean {
        return true // Реализация метода
    }
}
interface DishData {
    fun is_new(): Boolean
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishCard(dish: Dish, onCardClick: (Dish) -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(10.dp),
        onClick = { onCardClick(dish) }, // Add click handling
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Column {
            Box( // Use Box to layer image and the "New" label
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Adjust height as needed
            ) {
                Image(
                    painter = painterResource(id = dish.imageResId),
                    contentDescription = dish.name, // Use dish name for accessibility
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)), // Clip top corners
                    contentScale = ContentScale.Crop
                )

                if (dish.is_new()) {
                    Surface( // Use Surface for background color and shape
                        color = Color(0xFFE0B0FF), // Light purple color from the image
                        shape = RoundedCornerShape(bottomEnd = 8.dp), // Shape for the label
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp) // Padding around the label
                    ) {
                        Text(
                            text = "НОВИНКА",
                            color = Color.Black, // Adjust text color if needed
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp) // Inner padding
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp) // Padding for the text content
            ) {
                Text(
                    text = dish.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface // Use theme color
                )
                Spacer(modifier = Modifier.height(4.dp)) // Space between name and description
                Text(
                    text = dish.portion + " г", // Assuming portion is in grams
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Use a secondary color
                )
                Spacer(modifier = Modifier.height(4.dp)) // Space between portion and description
                Text(
                    text = dish.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Use a secondary color
                    maxLines = 2 // Limit description to 2 lines as in the image
                )

                Spacer(modifier = Modifier.height(16.dp)) // Space before price row

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween, // Distribute space
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${dish.cost}₽",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // This could be an IconButton or just an Icon depending on desired click behavior
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Go to dish details",
                        tint = MaterialTheme.colorScheme.primary // Use theme primary color
                    )
                }
            }
        }
    }
}

@Composable
fun MainMenuScreen() {

    Scaffold { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Column {
                Text(
                    text = "Доставим сюда:",
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "улица Студенческая, 26",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    color = Color(121, 121, 121)
                )
            }



            val dish = Dish(
                name = "Шашлык",
                description = "Традиционный шашлындос к пиву",
                portion = "300",
                cost = 250,
                imageResId = R.drawable.shahlisk
            )
            Row {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    item {
                        DishCard(dish)
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    item {
                        DishCard(dish)
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme { // Оберните также и Preview в тему
        MainMenuScreen()
    }
}
