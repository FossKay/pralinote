package com.example.pralinote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.pralinote.model.AppClient
import com.example.pralinote.model.LoginModel
import com.example.pralinote.ui.theme.PralinoteTheme
import com.example.pralinote.view.GradesView
import com.example.pralinote.view.HomeHomeworkView
import com.example.pralinote.view.HomeTimetableView
import com.example.pralinote.view.LoginScreen
import com.example.pralinote.view.getPraliColor
import kotlinx.serialization.Serializable
import com.example.pralinote.model.ThemeModel
import com.example.pralinote.view.LoginView

@Serializable
data object Accounts : NavKey

@Serializable
data object Login : NavKey

@Serializable
data object Content : NavKey

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themeModel: ThemeModel = viewModel()
            // val loginModel: LoginModel = viewModel()
            val themeState by themeModel.state.collectAsStateWithLifecycle()
            val backStack = rememberNavBackStack(Accounts)

            PralinoteTheme(
                color = themeState.color,
            ) {
                NavDisplay(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    transitionSpec = {
                        slideInHorizontally { it } togetherWith
                            slideOutHorizontally { -it }
                    },
                    popTransitionSpec = {
                        slideInHorizontally(
                            //animationSpec = tween(5000)
                        ) { it } togetherWith
                                slideOutHorizontally(
                                    //animationSpec = tween(5000)
                                ) { -it }
                    },
                    entryProvider = entryProvider {
                        entry<Content> {
                            MainContent(onDisconnection = {
                                AppClient.instance = null
                                AppClient.params = null
                                AppClient.userParams = null
                                // cannot go back, pops to accounts
                                // don't lazy cancel form
                                // loginModel.cancel()
                                backStack.removeLastOrNull() // removeLast = newer func
                            })
                        }

                        entry<Accounts> {
                            LoginScreen(backStack)
                        }

                        entry<Login> {
                            LoginView(backStack)
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun MainContent(onDisconnection: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    var dropdownOpen by rememberSaveable { mutableStateOf(false) }

    BackHandler {
        onDisconnection()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        listOf(
                            "Devoirs",
                            "Emploi du temps",
                            "Notes",
                        )[page]
                    )
                },
                navigationIcon = {
                    val bytes = Base64.decode(AppClient.userParams!!.image, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val cropped = Bitmap.createBitmap(
                        bitmap,
                        0,
                        0,
                        bitmap.width,
                        bitmap.width,
                    )

                    Image(
                        bitmap = cropped.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(CircleShape)
                            .size(42.dp),
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            dropdownOpen = true
                        }
                    ) {
                        Icon(
                            Icons.Outlined.Menu,
                            contentDescription = "Options"
                        )
                    }
                    DropdownMenu(
                        expanded = dropdownOpen,
                        onDismissRequest = {
                            dropdownOpen = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Se déconnecter") },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Logout,
                                    contentDescription = "Se déconnecter",
                                )
                            },
                            onClick = {
                                dropdownOpen = false
                                onDisconnection()
                            }
                        )
                    }
                },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    subtitleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        bottomBar = {
            ShortNavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                ShortNavigationBarItem(
                    selected = page == 0,
                    onClick = { page = 0 },
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Outlined.Assignment,
                            contentDescription = "Devoirs"
                        )
                    },
                    label = { Text("Devoirs") }
                )
                ShortNavigationBarItem(
                    selected = page == 1,
                    onClick = { page = 1 },
                    icon = {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            contentDescription = "Emploi du temps"
                        )
                    },
                    label = { Text("Cours") }
                )
                ShortNavigationBarItem(
                    selected = page == 2,
                    onClick = { page = 2 },
                    icon = {
                        Icon(
                            Icons.Outlined.Grade,
                            contentDescription = "Emploi du temps"
                        )
                    },
                    label = { Text("Notes") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .graphicsLayer { alpha = if (page == 0) 1f else 0f }
                    .zIndex(if (page == 0) 1f else 0f)) {
                HomeHomeworkView(padding)
            }
            Box(
                modifier = Modifier
                    .graphicsLayer { alpha = if (page == 1) 1f else 0f }
                    .zIndex(if (page == 1) 1f else 0f)) {
                HomeTimetableView(padding)
            }
            Box(
                modifier = Modifier
                    .graphicsLayer { alpha = if (page == 2) 1f else 0f }
                    .zIndex(if (page == 2) 1f else 0f)) {
                GradesView(padding)
            }
        }
    }
}