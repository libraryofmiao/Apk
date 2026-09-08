package com.libraryofmiao.membership.nav

/** Mirrors the live site's page set 1:1 so no screen/flow is lost: login, index, register
 *  (also used for edit), dashboard, member-card, verify. */
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object Dashboard : Screen("dashboard")
    data object Register : Screen("register?memberId={memberId}") {
        fun createRoute(memberId: String? = null) =
            "register?memberId=${memberId ?: ""}"
    }
    data object MemberCard : Screen("member_card/{memberId}") {
        fun createRoute(memberId: String) = "member_card/$memberId"
    }
    data object Verify : Screen("verify?code={code}") {
        fun createRoute(code: String? = null) = "verify?code=${code ?: ""}"
    }
}
