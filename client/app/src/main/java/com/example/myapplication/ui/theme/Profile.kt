package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme

val BonusCardGradientStart = Color(0xFFFED253)
val BonusCardGradientEnd = Color(0xFFFEA24F)
val ActivateButtonColor = Color(0xFFFE8C65)
val GreyText = Color(0xFF757575)
val DarkText = Color(0xFF333333)


data class MenuItemData(
    val icon: ImageVector,
    val label: String,
    val route: String? = null
)


@Composable
fun ProfileScreenContent() {
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
                name = "Константин",
                phone = "+7 (922) 291-94-62",
                onNotificationClick = { /* TODO: обработка клика по колокольчику */ }
            )
        }

        item {
            BonusCard(
                bonusPercentage = "5%",
                bonusPoints = 0,
                onCashbackButtonClick = { /* TODO: обработка клика по кнопке Кэшбэк */ }
            )
        }

        item {
            ActivateBonusesSection(
                onActivateClick = { /* TODO: обработка клика по Активировать баллы */ }
            )
        }

        val menuItems = listOf(
            MenuItemData(Icons.Default.ShoppingCart, "Мои заказы"),
            MenuItemData(Icons.Default.LocationOn, "Мои адреса"),
            MenuItemData(Icons.Default.Person, "Мои данные"),
            MenuItemData(Icons.Default.Add, "Банковские карты"),
            MenuItemData(Icons.Default.Home, "Екатеринбург | RU"),
        )

        items(menuItems) { item ->
            MenuItemRow(
                icon = item.icon,
                label = item.label,
                onClick = { /* TODO: обработка клика по пункту ${item.label} */ }
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
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
                text = phone,
                fontSize = 12.sp,
                color = GreyText,
                fontFamily = Montserrat
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
                        text = "Друг $bonusPercentage",
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
                    fontFamily = Montserrat
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .clickable(onClick = onCashbackButtonClick)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = bonusPercentage,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ActivateButtonColor,
                        fontFamily = Montserrat
                    )
                    Text(
                        text = "Кэшбэк",
                        fontSize = 12.sp,
                        color = GreyText,
                        fontFamily = Montserrat
                    )
                }
            }
        }
    }
}

@Composable
fun ActivateBonusesSection(onActivateClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ActivateButtonColor)
            .clickable(onClick = onActivateClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Иконка баллов",
                tint = Color.White,
                modifier = Modifier.size(24.dp).padding(end = 8.dp)
            )
            Column {
                Text(
                    text = "Активировать баллы",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    fontFamily = Montserrat
                )
                Text(
                    text = "Дополните данные профиля, чтобы использовать\nбаллы",
                    fontSize = 10.sp,
                    color = Color(0xFFF5F5F5),
                    fontFamily = Montserrat
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Перейти",
            modifier = Modifier.size(24.dp)
        )
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
            text = label,
            fontSize = 16.sp,
            color = DarkText,
            fontFamily = Montserrat
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MyApplicationTheme {
        Scaffold(
            bottomBar = {
                BottomAppBar(modifier = Modifier.height(65.dp)) {
                    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Home, contentDescription = null)
                        Icon(Icons.Default.Search, contentDescription = null)
                        Icon(Icons.Default.Person, contentDescription = null)
                        Icon(Icons.Default.Menu, contentDescription = null)
                    }
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                ProfileScreenContent()
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun MenuItemRowPreview() {
    MyApplicationTheme {
        MenuItemRow(icon = Icons.Default.ShoppingCart, label = "Мои заказы") {}
    }
}

@Preview(showBackground = true)
@Composable
fun ActivateBonusesSectionPreview() {
    MyApplicationTheme {
        ActivateBonusesSection {}
    }
}

@Preview(showBackground = true)
@Composable
fun BonusCardPreview() {
    MyApplicationTheme {
        BonusCard(bonusPercentage = "5%", bonusPoints = 150) {}
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileHeaderPreview() {
    MyApplicationTheme {
        ProfileHeader(name = "Константин", phone = "+7 (922) 291-94-62") {}
    }
}