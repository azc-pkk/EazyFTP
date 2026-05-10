package uestc.b3dman.ftpclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import uestc.b3dman.ftpclient.ui.screens.addaccount.AddAccountScreen
import uestc.b3dman.ftpclient.ui.screens.browser.BrowserScreen
import uestc.b3dman.ftpclient.ui.screens.history.HistoryScreen
import uestc.b3dman.ftpclient.ui.screens.login.LoginScreen


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
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
    NavHost(
        navController = navController,
        startDestination = "login",
        enterTransition = { slideInHorizontally { it } + fadeIn() },
        exitTransition = { slideOutHorizontally { -it } + fadeOut() },
        popEnterTransition = { slideInHorizontally { -it } + fadeIn() },
        popExitTransition = { slideOutHorizontally { it } + fadeOut() }
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToBrowser = { accountId ->
                    navController.navigate("browser?accountId=$accountId")
                },
                onNavigateToAddAccount = {
                    navController.navigate("add_account")
                },
                onNavigateToEditAccount = { accountId ->
                    navController.navigate("add_account?accountId=$accountId")
                }
            )
        }
        composable(
            "add_account?accountId={accountId}",
            arguments = listOf(
                navArgument("accountId") {
                    type = NavType.IntType
                    defaultValue = -1 // -1 表示新建账号，其他值表示编辑对应 ID 的账号
                }
            )
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getInt("accountId") ?: -1
            AddAccountScreen(
                accountId = accountId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            "browser?accountId={accountId}",
            arguments = listOf(
                navArgument("accountId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getInt("accountId") ?: -1
            BrowserScreen(
                accountId = accountId,
                onExit = { navController.popBackStack() },
                onNavigateToHistory = { accountId ->
                    navController.navigate("history?accountId=$accountId")
                }
            )
        }

        composable(
            "history?accountId={accountId}",
            arguments = listOf(
                navArgument("accountId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getInt("accountId") ?: -1
            HistoryScreen(
                accountId = accountId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}