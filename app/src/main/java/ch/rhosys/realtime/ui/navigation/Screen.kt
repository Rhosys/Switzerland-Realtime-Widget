package ch.rhosys.realtime.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
}
