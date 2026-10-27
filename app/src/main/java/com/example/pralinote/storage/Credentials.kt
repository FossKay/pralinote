package com.example.pralinote.storage

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.getStringOrNull

data class Credentials(
    val id: Long = 0,
    val rootUrl: String,
    val username: String,
    val password: String,
    val schoolName: String,
    val studentName: String,
    val color: String?,
    val isToken: Boolean = false,
    val dirty: Boolean = false,
    val autoconnect: Boolean = false
)

class CredentialsStore(context: Context) :
    SQLiteOpenHelper(context, "credentials.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE credentials (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                root_url TEXT NOT NULL,
                username TEXT NOT NULL,
                password TEXT NOT NULL,
                school_name TEXT NOT NULL,
                student_name TEXT NOT NULL,
                color TEXT,
                is_token INTEGER NOT NULL DEFAULT 0,
                dirty INTEGER NOT NULL DEFAULT 0,
                autoconnect INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) = Unit

    // store.getAll() -> list of profiles
    fun getAll(): List<Credentials> =
        readableDatabase.query(
            "credentials",
            null,
            null,
            null,
            null,
            null,
            "id ASC"
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        Credentials(
                            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                            rootUrl = cursor.getString(cursor.getColumnIndexOrThrow("root_url")),
                            username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
                            password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
                            schoolName = cursor.getString(cursor.getColumnIndexOrThrow("school_name")),
                            studentName = cursor.getString(cursor.getColumnIndexOrThrow("student_name")),
                            color = cursor.getStringOrNull(cursor.getColumnIndexOrThrow("color")),
                            isToken = cursor.getInt(cursor.getColumnIndexOrThrow("is_token")) != 0,
                            dirty = cursor.getInt(cursor.getColumnIndexOrThrow("dirty")) != 0,
                            autoconnect = cursor.getInt(cursor.getColumnIndexOrThrow("autoconnect")) != 0
                        )
                    )
                }
            }
        }

    // store.get(profile.id) -> returns profile
    fun get(id: Long): Credentials? =
        readableDatabase.query(
            "credentials",
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor ->
            if (!cursor.moveToFirst()) return@use null

            Credentials(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                rootUrl = cursor.getString(cursor.getColumnIndexOrThrow("root_url")),
                username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
                password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
                schoolName = cursor.getString(cursor.getColumnIndexOrThrow("school_name")),
                studentName = cursor.getString(cursor.getColumnIndexOrThrow("student_name")),
                color = cursor.getStringOrNull(cursor.getColumnIndexOrThrow("color")),
                isToken = cursor.getInt(cursor.getColumnIndexOrThrow("is_token")) != 0,
                dirty = cursor.getInt(cursor.getColumnIndexOrThrow("dirty")) != 0,
                autoconnect = cursor.getInt(cursor.getColumnIndexOrThrow("autoconnect")) != 0
            )
        }

    // store.save(profile.copy(username = "new_username"))
    // -> if copied object (id too), updates
    // -> if new object (id==0L), creates a new one
    fun save(credentials: Credentials): Long {
        val values = ContentValues().apply {
            put("root_url", credentials.rootUrl)
            put("username", credentials.username)
            put("password", credentials.password)
            put("school_name", credentials.schoolName)
            put("student_name", credentials.studentName)
            put("color", credentials.color)
            put("is_token", credentials.isToken.toInt())
            put("dirty", credentials.dirty.toInt())
            put("autoconnect", credentials.autoconnect.toInt())
        }

        return if (credentials.id == 0L) {
            writableDatabase.insert("credentials", null, values)
        } else {
            val updated = writableDatabase.update(
                "credentials",
                values,
                "id = ?",
                arrayOf(credentials.id.toString())
            )

            if (updated == 0) -1L else credentials.id
        }
    }

    // store.delete(profile.id)
    fun delete(id: Long) {
        writableDatabase.delete(
            "credentials",
            "id = ?",
            arrayOf(id.toString())
        )
    }

    fun clear() {
        writableDatabase.delete("credentials", null, null)
    }

    private fun Boolean.toInt(): Int = if (this) 1 else 0
}