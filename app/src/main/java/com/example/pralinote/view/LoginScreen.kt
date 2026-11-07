package com.example.pralinote.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.pralinote.Content
import com.example.pralinote.Login
import com.example.pralinote.model.LoginModel
import com.example.pralinote.model.ThemeModel
import com.example.pralinote.storage.Credentials
import com.example.pralinote.ui.theme.PralinoteTheme

@Composable
fun LoginScreen(
    backStack: NavBackStack<NavKey>,
    themeModel: ThemeModel = viewModel(),
    loginModel: LoginModel = viewModel(),
) {
    val loginState by loginModel.state.collectAsStateWithLifecycle()

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Comptes") }
        )
    }, floatingActionButton = {
        // TODO: fab menu w/ qr code, pwd, geolocation
        FloatingActionButton(
            onClick = {
                loginModel.newProfile()
                backStack.add(Login)
            }) {
            Icon(
                Icons.Filled.Add, contentDescription = "Ajouter un compte"
            )
        }
    }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            if (loginState.profiles.isEmpty()) {
                Text(
                    "Aucun compte enregistré",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // profiles list
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(loginState.profiles, key = { it.id }) { profile ->
                        PralinoteTheme(
                            color = getPraliColor(profile.color)
                        ) {
                            Profile(profile, onConnect = {
                                loginModel.openProfile(profile) {
                                    themeModel.setColor(it)
                                    backStack.clear()
                                    backStack.add(Content)
                                    loginModel.cancel()
                                }
                                backStack.add(Login)
                            }, onDelete = {
                                loginModel.deleteProfile(profile.id)
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Profile(profile: Credentials, onConnect: () -> Unit, onDelete: () -> Unit) {
    var dropdownOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart = 8.dp,
                    bottomStart = 8.dp,
                    topEnd = 12.dp,
                    bottomEnd = 12.dp,
                )
            )
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically // the options icon
        ) {
            // like in official app, a vertical stripe with the school's color
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(8.dp)
                    .background(
                        if (isSystemInDarkTheme()) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            // otherwise it's barely visible
                            MaterialTheme.colorScheme.primaryFixedDim
                        }
                    )
            )

            // name + school name (+ dirty)
            Column(
                modifier = Modifier
                    .weight(1f) // so that the options icon is at top right
                    .clickable { onConnect() }
                    .padding(start = 16.dp)
                    .padding(vertical = 16.dp)) {
                Text(
                    text = profile.studentName,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = profile.schoolName, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (profile.dirty) {
                    Text(
                        text = "Connexion expirée", color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // options icon
            Box {
                IconButton(
                    onClick = { dropdownOpen = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert, contentDescription = "Options"
                    )
                }

                DropdownMenu(
                    expanded = dropdownOpen, onDismissRequest = {
                        dropdownOpen = false
                    }) {
                    DropdownMenuItem(text = { Text("Supprimer") }, leadingIcon = {
                        Icon(
                            Icons.Default.Delete, contentDescription = null
                        )
                    }, onClick = {
                        onDelete()
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginView(
    backStack: NavBackStack<NavKey>,
    themeModel: ThemeModel = viewModel(),
    loginModel: LoginModel = viewModel()
) {
    val state by loginModel.state.collectAsStateWithLifecycle()

    val profile = state.profiles.find { it.id == state.editingId }

    var url by rememberSaveable(state.editingId) {
        mutableStateOf(profile?.rootUrl ?: "")
    }
    var user by rememberSaveable(state.editingId) {
        mutableStateOf(profile?.username ?: "")
    }
    var pass by rememberSaveable(state.editingId) {
        mutableStateOf(profile?.password ?: "")
    }

    var showPassword by remember { mutableStateOf(false) }
    var showUrlHelp by remember { mutableStateOf(false) }

    val isLoading = state.isLoading
    val error = state.error

    val urlRegex = Regex("""^https://[a-z0-9-]+\.index-education\.net/pronote$""")
    val urlValid = urlRegex.matches(url.trim())
    val urlError = url.isNotBlank() && !urlValid
    val formValid = urlValid && user.isNotBlank() && pass.isNotEmpty()

    // idk why but otherwise won't work
    val supportingText: (@Composable () -> Unit)? = if (urlError) {
        { Text("Lien invalide") }
    } else {
        null
    }

    BackHandler(enabled = !isLoading) {
        loginModel.cancel()
        // BackHandler intercepts back nav behaviour apparently...
        backStack.removeLastOrNull()
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Connexion") }
        )
    }) { padding ->
        // TODO: LazyColumn?
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Lien du site") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = false,
                isError = urlError,
                supportingText = supportingText,
                trailingIcon = {
                    IconButton(
                        onClick = { showUrlHelp = true }) {
                        Icon(
                            Icons.Outlined.Info, contentDescription = "Aide"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri
                )
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = user,
                onValueChange = { user = it },
                label = { Text("Identifiant") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            val passwordTransform: VisualTransformation = if (!showPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            }
            // TODO: handle token auth (don't display token + user can't edit it)
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Mot de passe") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true,
                visualTransformation = passwordTransform,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                trailingIcon = @Composable {
                    IconButton(
                        onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = "Aide"
                        )
                    }
                },
            )
            Spacer(Modifier.height(16.dp))

            error?.let {
                Text(
                    text = it, color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    loginModel.doLogin(
                        url,
                        user,
                        pass,
                        state.editingId
                    ) {
                        themeModel.setColor(it)
                        backStack.clear()
                        backStack.add(Content)
                        loginModel.cancel()
                    }
                },
                enabled = !isLoading && formValid,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isLoading) {
                    LoadingIndicator()
                } else {
                    Text("Se connecter")
                }
            }

            if (showUrlHelp) {
                AlertDialog(
                    onDismissRequest = { showUrlHelp = false },
                    title = { Text("Lien du site") },
                    text = {
                        Text(
                            "Entrez le lien Pronote de votre établissement sous la forme: " + "https://XXX.index-education.net/pronote"
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showUrlHelp = false }) {
                            Text("Fermer")
                        }
                    }
                )
            }
        }
    }
}