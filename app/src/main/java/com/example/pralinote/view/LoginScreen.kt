package com.example.pralinote.view

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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Visibility
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pralinote.model.AppClient
import com.example.pralinote.model.MOCK
import com.example.pralinote.storage.Credentials
import com.example.pralinote.storage.CredentialsStore
import com.example.pralinote.ui.theme.PralinoteTheme
import kotlinx.coroutines.launch
import uniffi.pronote_rs.PronoteClient
import uniffi.pronote_rs.Exception as PronoteException

@Composable
fun LoginScreen(onLoginSuccess: (String?) -> Unit) {
    val context = LocalContext.current
    val store = remember { CredentialsStore(context) }
    val scope = rememberCoroutineScope()

    var profiles by remember { mutableStateOf(store.getAll()) }

    var showForm by rememberSaveable { mutableStateOf(false) }
    // id of the profile whose creds are loaded (in login screen)
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun doLogin(rootUrl: String, user: String, pwd: String, profileId: Long?) {
        // already logging in...
        // doLogin SHOULD set isLoading=true itself
        if (isLoading) return
        isLoading = true
        error = null

        scope.launch {
            try {
                val client = if (MOCK) {
                    //delay(3000.milliseconds)
                    PronoteClient.mock()
                } else {
                    PronoteClient.login(rootUrl.trim(), user.trim(), pwd.trim())
                }

                AppClient.instance = client
                AppClient.params = client.params()
                AppClient.userParams = client.userParams()

                if (profileId == null) {
                    store.save(
                        Credentials(
                            rootUrl = rootUrl.trim(),
                            username = user.trim(),
                            password = pwd.trim(),
                            navIdentifier = null,
                            uuid = null,
                            schoolName = client.params().schoolName,
                            studentName = client.userParams()!!.student.person.name,
                            color = client.params().collectivity.color,
                            isToken = false,
                            dirty = false,
                            autoconnect = false
                        )
                    )
                } else {
                    store.get(profileId)?.let { profile ->
                        store.save(
                            profile.copy(
                                rootUrl = rootUrl.trim(),
                                username = user.trim(),
                                password = pwd.trim(),
                                navIdentifier = null,
                                uuid = null,
                                schoolName = client.params().schoolName,
                                studentName = client.userParams()!!.student.person.name,
                                color = client.params().collectivity.color,
                                isToken = false,
                                dirty = false
                            )
                        )
                    }
                }

                profiles = store.getAll()
                showForm = false
                editingId = null

                onLoginSuccess(client.params().collectivity.color)
            } catch (e: PronoteException) {
                if (profileId != null) {
                    store.get(profileId)?.let { profile ->
                        // connection expired -> do not autoconnect
                        store.save(profile.copy(dirty = true, autoconnect = false))
                    }
                    profiles = store.getAll()
                }

                e.printStackTrace()
                error = "La connexion a échoué: $e"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(editingId) {
        if (editingId != null && !isLoading) {
            val profile = profiles.first { it.id == editingId }
            if (profile.dirty) return@LaunchedEffect
            doLogin(profile.rootUrl, profile.username, profile.password, profile.id)
        }
    }

    Scaffold(topBar = {
        if (!showForm) {
            TopAppBar(
                title = { Text("Comptes") })
        } else {
            TopAppBar(
                title = { Text("Connexion") })
        }
    }, floatingActionButton = {
        if (!showForm) {
            // TODO: fab menu w/ qr code, pwd, geolocation
            FloatingActionButton(
                onClick = {
                    editingId = null // we aren't opening an existing profile
                    showForm = true
                }) {
                Icon(
                    Icons.Filled.Add, contentDescription = "Ajouter un compte"
                )
            }
        }
    }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            if (!showForm) {
                if (profiles.isEmpty()) {
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
                        items(profiles, key = { it.id }) { profile ->
                            PralinoteTheme(
                                color = getPraliColor(profile.color)
                            ) {
                                Profile(profile, onConnect = {
                                    editingId = profile.id
                                    if (profile.dirty) {
                                        error = "La connexion à Pronote a expiré."
                                    }
                                    showForm = true
                                }, onDelete = {
                                    store.delete(profile.id)
                                    profiles = store.getAll()
                                })
                            }
                        }
                    }
                }
            } else {
                if (editingId == null) {
                    LoginView(
                        "",
                        "",
                        "",
                        isLoading,
                        error,
                        { url, user, pwd ->
                            doLogin(url, user, pwd, null)
                        },
                        {
                            showForm = false
                            error = null
                        }
                    )
                } else {
                    val profile = profiles.first { it.id == editingId }
                    LoginView(
                        profile.rootUrl,
                        profile.username,
                        profile.password,
                        isLoading,
                        error,
                        { url, user, pwd ->
                            doLogin(url, user, pwd, editingId!!)
                        },
                        {
                            editingId = null
                            showForm = false
                            error = null
                        }
                    )
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
    rootUrl: String,
    user: String,
    pwd: String,
    isLoading: Boolean,
    error: String?,
    onLogin: (String, String, String) -> Unit,
    onClose: () -> Unit,
) {
    // dialog that explains URL format
    var showUrlHelp by remember { mutableStateOf(false) }

    var url by rememberSaveable { mutableStateOf(rootUrl) }
    var user by rememberSaveable { mutableStateOf(user) }
    var pass by rememberSaveable { mutableStateOf(pwd) }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    val urlRegex = Regex("""^https://[a-zA-Z0-9-]+\.index-education\.net/pronote$""")
    val urlValid = urlRegex.matches(url.trim())
    val urlError = url.isNotBlank() && !urlValid
    val formValid = urlValid && user.isNotBlank() && pass.isNotEmpty()

    // idk why but otherwise won't work
    val supportingText: (@Composable () -> Unit)? = if (urlError) {
        { Text("Lien invalide") }
    } else {
        null
    }

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
            onLogin(rootUrl, user, pwd)
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
    TextButton(
        onClick = { onClose() },
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Annuler")
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