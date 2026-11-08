package ir.revayat.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.revayat.app.ui.components.AssetBackdrop
import ir.revayat.app.ui.components.Embers
import ir.revayat.app.ui.components.GirihArt
import ir.revayat.app.ui.components.TulipLogo
import ir.revayat.app.ui.theme.Cream
import ir.revayat.app.ui.theme.Gold
import ir.revayat.app.ui.theme.Ink
import ir.revayat.app.ui.theme.Olive
import ir.revayat.app.ui.theme.Tulip
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onDone: () -> Unit) {
    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }
    var showText by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        launch { logoAlpha.animateTo(1f, tween(700)) }
        logoScale.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        showText = true
        delay(1500)
        onDone()
    }
    Box(Modifier.fillMaxSize().background(Ink), contentAlignment = Alignment.Center) {
        AssetBackdrop("splash", Modifier.fillMaxSize()) {
            GirihArt(Color(0xFF14201A), Gold, seed = 11, showTulip = false, modifier = Modifier.fillMaxSize())
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.35f), 0.5f to Color.Black.copy(alpha = 0.55f), 1f to Color.Black.copy(alpha = 0.25f),
                ),
            ),
        )
        Embers(Modifier.fillMaxSize(), color = Gold)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TulipLogo(
                Modifier.size(150.dp).graphicsLayer {
                    scaleX = logoScale.value; scaleY = logoScale.value; alpha = logoAlpha.value
                },
                petal = Tulip, stroke = Olive, ring = Gold,
            )
            Spacer(Modifier.height(24.dp))
            AnimatedVisibility(showText, enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 3 }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("روایت", style = MaterialTheme.typography.displayLarge, color = Cream)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "قصه‌هایی که باید شنید",
                        style = MaterialTheme.typography.titleMedium,
                        color = Gold,
                    )
                }
            }
        }
        Text(
            "ویژه هفته بسیج",
            style = MaterialTheme.typography.labelMedium,
            color = Cream.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 28.dp),
        )
    }
}

private data class OnbPage(val title: String, val body: String, val accent: Color, val seed: Int)

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val pages = listOf(
        OnbPage(
            "هر روز، یک روایت",
            "در هفت روز هفته بسیج، هر روز روایت تازه‌ای باز می‌شود؛ قصه آدم‌هایی که بی‌ادعا آمدند.",
            Gold, 3,
        ),
        OnbPage(
            "مثل یک استوری",
            "روایت‌ها را تمام‌صفحه و قدم‌به‌قدم ببینید. با لمس صفحه جلو و عقب بروید و برای مکث نگه دارید.",
            Tulip, 9,
        ),
        OnbPage(
            "روایت را برسانید",
            "از جمله‌های ماندگار، کارت‌های زیبا بسازید و با دیگران به اشتراک بگذارید.",
            Olive, 21,
        ),
    )
    val pager = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize().background(Ink)) {
        HorizontalPager(pager, Modifier.fillMaxSize()) { i ->
            val p = pages[i]
            Box(Modifier.fillMaxSize()) {
                AssetBackdrop("onboarding${i + 1}", Modifier.fillMaxSize()) {
                    GirihArt(Color(0xFF121A16), p.accent, seed = p.seed, modifier = Modifier.fillMaxSize())
                }
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(0f to Color.Black.copy(alpha = 0.25f), 0.4f to Color.Transparent, 0.62f to Color.Black.copy(alpha = 0.6f), 1f to Color.Black.copy(alpha = 0.95f)),
                    ),
                )
                Column(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 32.dp)
                        .padding(bottom = 170.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(p.title, style = MaterialTheme.typography.displaySmall, color = Cream, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        p.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Cream.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        TextButton(
            onClick = onDone,
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(8.dp),
        ) { Text("رد شدن", color = Cream.copy(alpha = 0.7f)) }

        Column(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pages.indices.forEach { i ->
                    val w by animateDpAsState(if (pager.currentPage == i) 28.dp else 8.dp, label = "dot")
                    Box(
                        Modifier.height(8.dp).width(w).clip(CircleShape)
                            .background(if (pager.currentPage == i) Gold else Cream.copy(alpha = 0.3f)),
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            val last = pager.currentPage == pages.lastIndex
            Button(
                onClick = { if (last) onDone() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
            ) {
                Text(if (last) "شروع روایت" else "بعدی", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
