package com.libraryofmiao.membership.data.session

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Mirrors the web app's `sessionStorage.setItem("libraryLoggedIn","yes")` pattern, but stored in
 * Android's EncryptedSharedPreferences instead of a plain browser flag, and cleared on logout
 * or process death by design (call `login()` again each app open — matches "session" semantics).
 *
 * IMPORTANT (carried over from the security review of the live system): this is still a
 * client-side-only gate. The backend Worker has no server-verified token to check, so — exactly
 * as on the web — a determined user could bypass this screen by talking to the Worker API
 * directly. This class deliberately keeps the same shape as the web app so nothing is lost, but
 * see README.md "Security notes" for the recommended server-side fix (signed session token)
 * this app is already wired to support once the backend adds it (see AuthInterceptor below).
 */
class SessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "library_membership_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun setLoggedIn(value: Boolean) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, value).apply()
    }

    /** Optional: the Options-Admin-Key needed for the "Manage Options" panel, entered once. */
    fun getOptionsAdminKey(): String? = prefs.getString(KEY_OPTIONS_ADMIN_KEY, null)

    fun setOptionsAdminKey(key: String) {
        prefs.edit().putString(KEY_OPTIONS_ADMIN_KEY, key).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_LOGGED_IN = "library_logged_in"
        private const val KEY_OPTIONS_ADMIN_KEY = "options_admin_key"
    }
}
