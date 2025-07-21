package com.example.todorevamp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.todorevamp.repository.AuthRepository
import com.example.todorevamp.ui.addEditTodo.AddEditTodoScreen
import com.example.todorevamp.ui.AITestScreen
import com.example.todorevamp.ui.login.LoginScreen
import com.example.todorevamp.ui.theme.TodoRevampTheme
import com.example.todorevamp.ui.todoList.TodoListScreen
import com.example.todorevamp.util.Routes
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            TodoRevampTheme {
                val navController = rememberNavController()
                
                // Determine start destination based on authentication status
                val startDestination = if (authRepository.isUserLoggedIn()) {
                    Routes.TODO_LIST
                } else {
                    Routes.LOGIN
                }
                
                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable(Routes.LOGIN) {
                        LoginScreen(
                            onNavigate = { event ->
                                navController.navigate(event.route) {
                                    // Clear the login screen from back stack
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Routes.TODO_LIST) {
                        TodoListScreen(
                            onNavigate = { event ->
                                if (event.route == Routes.LOGIN) {
                                    navController.navigate(event.route) {
                                        // Clear entire back stack when logging out
                                        popUpTo(0) { inclusive = true }
                                    }
                                } else {
                                    navController.navigate(event.route)
                                }
                            }
                        )
                    }
                    composable(Routes.AI_TEST) {
                        AITestScreen()
                    }
                    composable(
                        route = Routes.ADD_EDIT_TODO + "?todoId={todoId}",  // as this screen needs the todo we pass it
                        arguments = listOf(
                            navArgument(name = "todoId") {  // this was stored in the saved state handle
                                type = NavType.IntType
                                defaultValue = -1
                            }
                        )
                    ) { navBackStackEntry ->
                        val todoId = navBackStackEntry.arguments?.getInt("todoId") ?: -1
                        AddEditTodoScreen(
                            onPopBackStack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}

