package com.example.pralinote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import com.example.pralinote.model.AppClient
import com.example.pralinote.ui.theme.PralinoteTheme
import com.example.pralinote.view.GradesView
import com.example.pralinote.view.HomeHomeworkView
import com.example.pralinote.view.HomeTimetableView
import com.example.pralinote.view.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var theme by remember { mutableStateOf<Color?>(null) }
            // REMEMBER loggedIn otherwise won't be recomposed!!!
            var loggedIn by remember { mutableStateOf(false) }

            PralinoteTheme(
                color = theme,
            ) {
                if (!loggedIn) {
                    LoginScreen(onLoginSuccess = { themeColor ->
                        theme = themeColor
                        // smth changed, check login state -> recomposition
                        loggedIn = AppClient.instance != null
                    })
                } else {
                    MainContent()
                }
            }
        }
    }
}


@Composable
fun MainContent() {
    var page by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                NavigationBarItem(
                    selected = page == 0,
                    onClick = { page = 0 },
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Outlined.Assignment,
                            contentDescription = "Devoirs"
                        )
                    },
                )
                NavigationBarItem(
                    selected = page == 1,
                    onClick = { page = 1 },
                    icon = {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            contentDescription = "Emploi du temps"
                        )
                    },
                )
                NavigationBarItem(
                    selected = page == 2,
                    onClick = { page = 2 },
                    icon = {
                        Icon(
                            Icons.Outlined.Grade,
                            contentDescription = "Emploi du temps"
                        )
                    },
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier
                .graphicsLayer { alpha = if (page == 0) 1f else 0f }
                .zIndex(if (page == 0) 1f else 0f)) {
                HomeHomeworkView(padding)
            }
            Box(modifier = Modifier
                .graphicsLayer { alpha = if (page == 1) 1f else 0f }
                .zIndex(if (page == 1) 1f else 0f)) {
                HomeTimetableView(padding)
            }
            Box(modifier = Modifier
                .graphicsLayer { alpha = if (page == 2) 1f else 0f }
                .zIndex(if (page == 2) 1f else 0f)) {
                GradesView(padding)
            }
        }
    }
}