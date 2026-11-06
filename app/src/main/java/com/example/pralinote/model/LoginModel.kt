package com.example.pralinote.model

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pralinote.storage.Credentials
import com.example.pralinote.storage.CredentialsStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uniffi.pronote_rs.Exception
import uniffi.pronote_rs.PronoteClient
import kotlin.time.Duration.Companion.milliseconds

data class LoginState(
    val profiles: List<Credentials> = emptyList(),
    val showForm: Boolean = false,
    val editingId: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class LoginModel(
    application: Application
) : AndroidViewModel(application) {
    val context = getApplication<Application>()
    val store = CredentialsStore(context)

    private val _state = MutableStateFlow(
        LoginState(profiles = store.getAll())
    )
    val state = _state.asStateFlow()

    fun doLogin(
        rootUrl: String,
        user: String,
        pwd: String,
        profileId: Long?,
        onLoginSuccess: (String?) -> Unit
    ) {
        if (_state.value.isLoading) return

        _state.update {
            it.copy(isLoading = true, error = null)
        }

        viewModelScope.launch {
            try {
                val client = if (MOCK) {
                    delay(3000.milliseconds)
                    PronoteClient.mock()
                } else {
                    PronoteClient.login(
                        rootUrl.trim(), user.trim(), pwd.trim()
                    )
                }

                AppClient.instance = client
                AppClient.params = client.params()
                AppClient.userParams = client.userParams()

                val params = client.params()
                val userParams = client.userParams()!!

                if (profileId == null) {
                    store.save(
                        Credentials(
                            rootUrl = rootUrl.trim(),
                            username = user.trim(),
                            password = pwd.trim(),
                            navIdentifier = null,
                            uuid = null,
                            schoolName = params.schoolName,
                            studentName = userParams.student.person.name,
                            color = params.collectivity.color,
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
                                schoolName = params.schoolName,
                                studentName = userParams.student.person.name,
                                color = params.collectivity.color,
                                isToken = false,
                                dirty = false
                            )
                        )
                    }
                }

                _state.update {
                    it.copy(
                        profiles = store.getAll(), showForm = false, editingId = null
                    )
                }

                onLoginSuccess(params.collectivity.color)

            } catch (e: Exception) {
                if (profileId != null) {
                    store.get(profileId)?.let {
                        store.save(it.copy(dirty = true, autoconnect = false))
                    }
                    _state.update {
                        it.copy(profiles = store.getAll())
                    }
                }

                e.printStackTrace()

                _state.update {
                    it.copy(error = "La connexion a échoué: $e")
                }
            } finally {
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    fun deleteProfile(id: Long) {
        store.delete(id)
        _state.update {
            it.copy(profiles = store.getAll())
        }
    }

    fun newProfile() {
        _state.update {
            it.copy(
                showForm = true, editingId = null, error = null
            )
        }
    }

    fun openProfile(profile: Credentials, onLoginSuccess: (String?) -> Unit) {
        _state.update {
            it.copy(
                showForm = true, editingId = profile.id, error = if (profile.dirty) {
                    "La connexion à Pronote a expiré."
                } else {
                    null
                }
            )
        }
        if (!profile.dirty) {
            doLogin(
                profile.rootUrl,
                profile.username,
                profile.password,
                profile.id,
                onLoginSuccess
            )
        }
    }

    fun cancel() {
        _state.update {
            it.copy(
                showForm = false, editingId = null, error = null
            )
        }
    }
}
