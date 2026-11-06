package ir.revayat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import ir.revayat.app.data.AppState
import ir.revayat.app.data.Repository
import ir.revayat.app.ui.AppNav
import ir.revayat.app.ui.LocalAppState
import ir.revayat.app.ui.LocalContent
import ir.revayat.app.ui.theme.RevayatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent {
            val ctx = LocalContext.current
            val state = remember { AppState(ctx.applicationContext) }
            val content = remember { Repository.load(ctx.applicationContext) }
            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl,
                LocalAppState provides state,
                LocalContent provides content,
            ) {
                RevayatTheme(themeMode = state.themeMode, fontScale = state.fontScale) {
                    AppNav()
                }
            }
        }
    }
}
