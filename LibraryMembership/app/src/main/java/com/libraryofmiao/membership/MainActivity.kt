package com.libraryofmiao.membership

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.libraryofmiao.membership.data.session.SessionManager
import com.libraryofmiao.membership.nav.LibraryMembershipNavHost
import com.libraryofmiao.membership.ui.theme.LibraryMembershipTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val session = SessionManager(applicationContext)
        setContent {
            LibraryMembershipTheme {
                LibraryMembershipNavHost(session = session)
            }
        }
    }
}
