package ch.rhosys.realtime

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import ch.rhosys.realtime.ui.error.StartupErrorScreen
import ch.rhosys.realtime.ui.navigation.AppNavHost
import ch.rhosys.realtime.ui.theme.RealtimeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // targetSdk 35+ forces edge-to-edge, so the status bar is drawn over the app's own
        // background. The app is always light-themed (RealtimeTheme darkTheme=false),
        // so always ask for dark bar icons — the default would follow the phone's dark mode
        // and draw white icons on our light background.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        setContent {
            RealtimeTheme {
                val app = application as RealtimeApp
                if (app.startupError != null) {
                    StartupErrorScreen(app.startupError!!)
                    return@RealtimeTheme
                }
                AppNavHost(navController = rememberNavController())
            }
        }
    }
}
