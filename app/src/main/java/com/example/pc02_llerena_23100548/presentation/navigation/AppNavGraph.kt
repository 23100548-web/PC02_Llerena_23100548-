package com.example.pc02_llerena_23100548.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pc02_llerena_23100548.presentation.auth.LoginScreen
import com.example.pc02_llerena_23100548.presentation.converter.ConverterScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") { LoginScreen(navController) }
        composable("converter") { ConverterScreen(navController) }
    }
}
