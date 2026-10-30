package com.example.pralinote.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.pralinote.model.AppClient
import com.example.pralinote.model.MOCK
import com.example.pralinote.storage.Credentials
import com.example.pralinote.storage.CredentialsStore
import com.example.pralinote.ui.theme.PralinoteTheme
import com.example.pralinote.ui.theme.PrimaryHex
import kotlinx.coroutines.launch
import uniffi.pronote_rs.PronoteClient
import uniffi.pronote_rs.Exception as PronoteException

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginScreen(onLoginSuccess: (Color) -> Unit) {
    val context = LocalContext.current
    val store = remember { CredentialsStore(context) }
    val scope = rememberCoroutineScope()

    var profiles by remember { mutableStateOf(store.getAll()) }
    var showForm by rememberSaveable { mutableStateOf(false) }

    // id of the profile whose creds are loaded (in login screen)
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    // if we click on the options icon of a profile
    var menuProfileId by remember { mutableStateOf<Long?>(null) }
    // dialog that explains URL format
    var showUrlHelp by remember { mutableStateOf(false) }

    var url by rememberSaveable { mutableStateOf("") }
    var user by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    val urlRegex = Regex("""^https://[a-zA-Z0-9-]+\.index-education\.net/pronote$""")
    val urlValid = urlRegex.matches(url.trim())
    val urlError = url.isNotBlank() && !urlValid
    val formValid = urlValid && user.isNotBlank() && pass.isNotEmpty()

    fun openForm(profile: Credentials? = null, message: String? = null) {
        editingId = profile?.id
        url = profile?.rootUrl.orEmpty()
        user = profile?.username.orEmpty()
        pass = profile?.password.orEmpty()
        error = message
        showPassword = false
        showForm = true

        if (profile != null) {
            onLoginSuccess(Color((profile.color ?: PrimaryHex).toColorInt()))
        }
    }

    fun closeForm() {
        showForm = false
        editingId = null
        error = null
        profiles = store.getAll()
    }

    fun doLogin() {
        // already logging in...
        // doLogin SHOULD set isLoading=true itself
        if (isLoading) return

        val rootUrl = url.trim()
        val username = user.trim()
        val password = pass
        val profileId = editingId

        isLoading = true
        error = null

        scope.launch {
            try {
                val client = if (MOCK) {
                    PronoteClient.mock()
                } else {
                    PronoteClient.login(rootUrl, username, password)
                }

                AppClient.instance = client
                AppClient.params = client.params()
                AppClient.userParams = client.userParams()

                if (profileId == null) {
                    store.save(
                        Credentials(
                            rootUrl = rootUrl,
                            username = username,
                            password = password,
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
                                rootUrl = rootUrl,
                                username = username,
                                password = password,
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

                onLoginSuccess(
                    Color(
                        (client.params().collectivity.color ?: PrimaryHex).toColorInt()
                    )
                )
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

    fun connect(profile: Credentials) {
        if (isLoading) return
        isLoading = false

        if (profile.dirty) {
            openForm(profile, "La connexion à Pronote a expiré.")
            return
        }

        openForm(profile)
        doLogin()
    }

    Scaffold(
        topBar = {
            if (!showForm) {
                TopAppBar(
                    title = { Text("Comptes") }
                )
            } else {
                TopAppBar(
                    title = { Text("Connexion") }
                )
            }
        },
        floatingActionButton = {
            if (!showForm) {
                // TODO: fab menu w/ qr code, pwd, geolocation
                FloatingActionButton(
                    onClick = { openForm() }
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Ajouter un compte"
                    )
                }
            }
        }
    ) { padding ->
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
                                color = Color((profile.color ?: PrimaryHex).toColorInt()),
                            ) {
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
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                        )

                                        // name + school name (+ dirty)
                                        Column(
                                            modifier = Modifier
                                                .weight(1f) // so that the options icon is at top right
                                                .clickable { connect(profile) }
                                                .padding(start = 16.dp)
                                                .padding(vertical = 16.dp)
                                        ) {
                                            Text(
                                                text = profile.studentName,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Text(
                                                text = profile.schoolName,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (profile.dirty) {
                                                Text(
                                                    text = "Connexion expirée",
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }

                                        // options icon
                                        Box {
                                            IconButton(
                                                onClick = { menuProfileId = profile.id }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Options"
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = menuProfileId == profile.id,
                                                onDismissRequest = {
                                                    menuProfileId = null
                                                }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Supprimer") },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Default.Delete,
                                                            contentDescription = null
                                                        )
                                                    },
                                                    onClick = {
                                                        store.delete(profile.id)
                                                        profiles = store.getAll()
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // idk why but otherwise won't work
                val supportingText: (@Composable () -> Unit)? =
                    if (urlError) {
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
                            onClick = { showUrlHelp = true }
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = "Aide"
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
                            onClick = { showPassword = !showPassword }
                        ) {
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
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Button(
                    onClick = { doLogin() },
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
                    onClick = { closeForm() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Annuler")
                }
            }
        }

        if (showUrlHelp) {
            AlertDialog(
                onDismissRequest = { showUrlHelp = false },
                title = { Text("Lien du site") },
                text = {
                    Text(
                        "Entrez le lien Pronote de votre établissement sous la forme: " +
                                "https://XXX.index-education.net/pronote"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { showUrlHelp = false }
                    ) {
                        Text("Fermer")
                    }
                }
            )
        }
    }
}