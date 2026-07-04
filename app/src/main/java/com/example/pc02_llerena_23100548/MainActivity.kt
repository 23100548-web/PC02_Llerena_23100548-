package com.example.pc02_llerena_23100548

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.pc02_llerena_23100548.presentation.navigation.AppNavGraph
import com.example.pc02_llerena_23100548.ui.theme.PC02_Llerena_23100548Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PC02_Llerena_23100548Theme {
                AppNavGraph()
            }
        }
    }
}