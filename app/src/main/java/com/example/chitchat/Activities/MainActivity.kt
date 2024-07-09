package com.example.chitchat.Activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.chitchat.MyUI.Screens.ChatsScreen
import com.example.chitchat.MyUI.Screens.ProfileScreen
import com.example.chitchat.MyUI.Screens.StatusScreen
import com.example.chitchat.MyUI.bottomNavigation.BottomNavItem
import com.example.chitchat.ui.theme.ChitChatTheme
import com.example.chitchat.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel : MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChitChatTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    start(viewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        viewModel.cleanUp()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun start(viewModel: MainViewModel) {
    var title by remember {
        mutableStateOf("CHITCHAT")
    }

    val navController = rememberNavController()


    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            title = when (destination.route) {
                BottomNavItem.Chats.route -> {
                    "CHITCHAT"
                }

                BottomNavItem.Status.route -> {
                    "STATUS"
                }

                BottomNavItem.Profile.route -> {
                    "PROFILE"
                }
                // Add more cases for other destinations as needed
                else -> {
                    "CHITCHAT"
                }
            }
        }
    }

    Scaffold(topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = Color.White
                )
            )
    },
        bottomBar = {
            bottomNavBar(navController = navController)
        }
    ) {
        navigation(navController, viewModel)
    }
}

@Composable
fun navigation(navController: NavHostController, viewModel: MainViewModel) {
    NavHost(navController = navController, startDestination = BottomNavItem.Chats.route) {
        composable(BottomNavItem.Chats.route) {
            ChatsScreen(viewModel)
        }
        composable(BottomNavItem.Status.route) {
            StatusScreen(viewModel)
        }
        composable(BottomNavItem.Profile.route) {
            ProfileScreen(viewModel)
        }
    }
}

@Composable
fun bottomNavBar(navController: NavController) {
    val values = listOf(BottomNavItem.Chats, BottomNavItem.Status, BottomNavItem.Profile)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar (
    ){
        values.forEachIndexed { index, item ->
            NavigationBarItem(
                //modifier = Modifier.background(myPrimary2),
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        painter = if (currentRoute == item.route) item.getSelectedIcon() else item.getUnselectedIcon(),
                        contentDescription = null,
                    )
                },
                label = { Text(text = item.label) }
            )
        }
    }
}