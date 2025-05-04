package com.example.myapplication

import android.content.pm.ActivityInfo
import android.graphics.ImageDecoder.decodeBitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.colorspace.Rgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight


class MainViewModel : ViewModel()

// Определите семейство шрифтов Montserrat
val Montserrat = FontFamily(
    Font(R.font.regular, FontWeight.Normal), // Связываем файл с начертанием Normal
    Font(R.font.bold, FontWeight.Bold),       // Связываем файл с начертанием Bold
    // Добавьте другие начертания, если вы добавили соответствующие файлы
    // Font(R.font.montserrat_medium, FontWeight.Medium),
    // Font(R.font.montserrat_semibold, FontWeight.SemiBold)
)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val viewModel = viewModel<MainViewModel>()
    var selectedItem by rememberSaveable { mutableStateOf(0) }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .height(65.dp),
                containerColor = Color(red = 241, green = 241, blue = 241),
                contentColor = Color.Black
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val items = listOf(
                    NavigationItem.Main,
                    NavigationItem.Second,
                    NavigationItem.Third
                )
                Row(modifier = Modifier
                    .fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically

                ){
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            colors = NavigationBarItemDefaults.colors(
                                unselectedTextColor = Color.Transparent,
                                selectedTextColor = Color.Transparent,
                                unselectedIconColor = Color.Gray,
                                selectedIconColor = Color.Black,
                                indicatorColor = Color.Transparent,
                            ),
                            icon = {
                                if (index == 0) {
                                    Icon(
                                        modifier = Modifier
                                            .alignByBaseline()
                                            .size(20.dp),
                                        painter = painterResource(id = R.drawable.swaga),
                                        contentDescription = "Описание иконки"
                                    )
                                }
                                if (index == 1) {
                                    Icon(
                                        imageVector = Icons.Rounded.AccountCircle,
                                        contentDescription = "Swag",
                                        modifier = Modifier
                                            .size(25.dp)
                                    )
                                }
                                if (index == 2) {
                                    Icon(
                                        imageVector = Icons.Rounded.Menu,
                                        contentDescription = "Swag",
                                        modifier = Modifier
                                            .size(25.dp)
                                    )
                                }
                            },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    navController.graph.startDestinationRoute?.let { route ->
                                        popUpTo(route) {
                                            saveState = true
                                        }
                                    }

                                    launchSingleTop = true
                                    restoreState = true
                                }
                                selectedItem = index
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = NavigationItem.Main.route, Modifier.padding(innerPadding)) {
            composable(NavigationItem.Main.route) { MainComposable() }
            composable(NavigationItem.Second.route) { SecondComposable() }
            composable(NavigationItem.Third.route) { ThirdComposable() }
        }
    }
}


@Composable
fun MainComposable() {
    MainMenuScreen()
}

@Composable
fun SecondComposable() {

}

@Composable
fun ThirdComposable() {
}

sealed class NavigationItem(var route: String, var icon: Int?, var title: String) {
    object Main : NavigationItem("main", R.drawable.ic_launcher_foreground, "Main")
    object Second : NavigationItem("second", R.drawable.ic_launcher_foreground, "Second")
    object Third : NavigationItem("third", R.drawable.ic_launcher_foreground, "Third")
}

