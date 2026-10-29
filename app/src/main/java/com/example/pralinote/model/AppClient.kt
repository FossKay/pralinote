package com.example.pralinote.model

import uniffi.pronote_rs.Params
import uniffi.pronote_rs.PronoteClient
import uniffi.pronote_rs.UserParams

const val MOCK = true

object AppClient {
    var instance: PronoteClient? = null
    var params: Params? = null
    var userParams: UserParams? = null
}