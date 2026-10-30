package com.example.pralinote.model

import com.example.pralinote.BuildConfig
import uniffi.pronote_rs.Params
import uniffi.pronote_rs.PronoteClient
import uniffi.pronote_rs.UserParams

// override to disable mock in debug builds
const val NO_MOCK = false

@Suppress("SimplifyBooleanWithConstants")
const val MOCK = BuildConfig.MOCK && !NO_MOCK

object AppClient {
    var instance: PronoteClient? = null
    var params: Params? = null
    var userParams: UserParams? = null
}