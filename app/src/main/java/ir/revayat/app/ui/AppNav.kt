package ir.revayat.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ir.revayat.app.data.AppState
import ir.revayat.app.data.Content
import ir.revayat.app.ui.screens.BookmarksScreen
import ir.revayat.app.ui.screens.DetailScreen
import ir.revayat.app.ui.screens.HomeScreen
import ir.revayat.app.ui.screens.LibraryScreen
import ir.revayat.app.ui.screens.OnboardingScreen
import ir.revayat.app.ui.screens.SettingsScreen
import ir.revayat.app.ui.screens.ShareCardScreen
import ir.revayat.app.ui.screens.SplashScreen
import ir.revayat.app.ui.screens.StoryScreen

val LocalAppState = staticCompositionLocalOf<AppState> { error("no state") }
val LocalContent = staticCompositionLocalOf<Content> { error("no content") }
val LocalNav = compositionLocalOf<Nav> { error("no nav") }

class Nav(private val c: NavHostController) {
    fun story(id: String) = c.navigate("story/$id")
    fun detail(id: String) = c.navigate("detail/$id")
    fun share(id: String, quote: Int = 0) = c.navigate("share/$id/$quote")
    fun back() { c.popBackStack() }
}

@Composable
fun AppNav() {
    val controller = rememberNavController()
    val state = LocalAppState.current
    val nav = Nav(controller)
    androidx.compose.runtime.CompositionLocalProvider(LocalNav provides nav) {
        NavHost(
            navController = controller,
            startDestination = "splash",
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            enterTransition = { fadeIn(tween(350)) + scaleIn(tween(350), initialScale = 0.97f) },
            exitTransition = { fadeOut(tween(250)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(250)) + scaleOut(tween(250), targetScale = 0.97f) },
        ) {
            composable("splash") {
                SplashScreen {
                    val next = if (state.onboarded) "main" else "onboarding"
                    controller.navigate(next) { popUpTo("splash") { inclusive = true } }
                }
            }
            composable("onboarding") {
                OnboardingScreen {
                    state.finishOnboarding()
                    controller.navigate("main") { popUpTo("onboarding") { inclusive = true } }
                }
            }
            composable("main") { MainTabs() }
            composable("story/{id}") { e -> StoryScreen(e.arguments?.getString("id") ?: "") }
            composable("detail/{id}") { e -> DetailScreen(e.arguments?.getString("id") ?: "") }
            composable("share/{id}/{q}") { e ->
                ShareCardScreen(
                    e.arguments?.getString("id") ?: "",
                    e.arguments?.getString("q")?.toIntOrNull() ?: 0,
                )
            }
        }
    }
}

private data class Tab(val label: String, val icon: ImageVector)

@Composable
private fun MainTabs() {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf(
        Tab("خانه", Icons.Outlined.Home),
        Tab("روایت‌ها", Icons.Outlined.AutoStories),
        Tab("نشان‌شده", Icons.Outlined.BookmarkBorder),
        Tab("تنظیمات", Icons.Outlined.Settings),
    )
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                tabs.forEachIndexed { i, t ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label, style = MaterialTheme.typography.labelMedium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }
        },
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(bottom = pad.calculateBottomPadding())) {
            AnimatedContent(
                targetState = tab,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it / 12 }) togetherWith
                        (fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { it / 12 })
                },
                label = "tabs",
            ) { t ->
                when (t) {
                    0 -> HomeScreen(onOpenLibrary = { tab = 1 })
                    1 -> LibraryScreen()
                    2 -> BookmarksScreen(onBrowse = { tab = 1 })
                    else -> SettingsScreen()
                }
            }
        }
    }
}

