package uestc.b3dman.ftpclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import uestc.b3dman.ftpclient.ui.screens.addaccount.AddAccountScreen
import uestc.b3dman.ftpclient.ui.screens.browser.BrowserScreen
import uestc.b3dman.ftpclient.ui.screens.login.LoginScreen


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Preview
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // TODO: 可能要添加更多的屏幕
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onNavigateToBrowser = {
                    navController.navigate("browser")
                },
                onNavigateToAddAccount = {
                    navController.navigate("add_account")
                }
            )
       }
        composable("add_account") {
            AddAccountScreen(onBack = {
                navController.popBackStack()
            })
        }
        composable("browser") {
            BrowserScreen(onExit = {
                navController.popBackStack()
            })
        }
    }
}