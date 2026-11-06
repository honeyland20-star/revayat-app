package ir.revayat.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.revayat.app.ui.LocalAppState
import ir.revayat.app.ui.LocalContent
import ir.revayat.app.ui.LocalNav
import ir.revayat.app.ui.components.Embers
import ir.revayat.app.ui.components.PersonVisual
import ir.revayat.app.ui.theme.Cream
import ir.revayat.app.ui.theme.Gold
import ir.revayat.app.ui.theme.Ink
import ir.revayat.app.util.fa

@Composable
fun StoryScreen(id: String) {
    val content = LocalContent.current
    val state = LocalAppState.current
    val nav = LocalNav.current
    val person = content.byId(id) ?: run { nav.back(); return }
    val slides = person.story
    var index by remember { mutableIntStateOf(0) }
    var paused by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }

    // Drive the progress bar of the current slide; resume where we left after a pause.
    val lastIdx = remember { intArrayOf(-1) }
    LaunchedEffect(index, paused, finished) {
        if (lastIdx[0] != index) { lastIdx[0] = index; progress.snapTo(0f) }
        if (paused || finished) { progress.stop(); return@LaunchedEffect }
        val total = (4500 + slides[index].length * 45).coerceIn(5000, 13000)
        val remaining = ((1f - progress.value) * total).toInt().coerceAtLeast(1)
        progress.animateTo(1f, tween(remaining, easing = LinearEasing))
        if (index < slides.lastIndex) {
            index++
        } else {
            finished = true
            state.markRead(person.id)
        }
    }

    fun go(delta: Int) {
        val target = index + delta
        when {
            target < 0 -> { index = 0 }
            target > slides.lastIndex -> { finished = true; state.markRead(person.id) }
            else -> { finished = false; index = target }
        }
    }

    Box(Modifier.fillMaxSize().background(Ink)) {
        PersonVisual(person, Modifier.fillMaxSize(), animated = true)
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.55f), 0.25f to Color.Transparent,
                    0.45f to Color.Black.copy(alpha = 0.25f), 1f to Color.Black.copy(alpha = 0.92f),
                ),
            ),
        )
        Embers(Modifier.fillMaxSize(), color = person.colorAccent, count = 18)

        // gesture layer: left half = next (RTL reading), right half = previous; hold = pause
        Box(
            Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        paused = true
                        tryAwaitRelease()
                        paused = false
                    },
                    onTap = { o ->
                        if (o.x < size.width / 2f) go(+1) else go(-1)
                    },
                )
            },
        )

        // top: progress + header
        Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                slides.indices.forEach { i ->
                    val f = when {
                        finished || i < index -> 1f
                        i == index -> progress.value
                        else -> 0f
                    }
                    Box(Modifier.weight(1f).height(3.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f))) {
                        Box(Modifier.fillMaxWidth(f).height(3.dp).background(Color.White))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).border(2.dp, Gold, CircleShape)) {
                    PersonVisual(person, Modifier.fillMaxSize(), showTulip = false)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(person.name, style = MaterialTheme.typography.titleSmall, color = Cream)
                    Text(person.lifeSpan, style = MaterialTheme.typography.labelSmall, color = Cream.copy(alpha = 0.7f))
                }
                if (paused) Icon(Icons.Filled.Pause, "مکث", tint = Cream, modifier = Modifier.padding(8.dp))
                IconButton(onClick = { nav.back() }) { Icon(Icons.Filled.Close, "بستن", tint = Cream) }
            }
        }

        // slide text
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding()
                .padding(horizontal = 26.dp).padding(bottom = if (finished) 170.dp else 64.dp),
        ) {
            AnimatedContent(
                targetState = index,
                transitionSpec = {
                    (fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 6 }) togetherWith fadeOut(tween(250))
                },
                label = "slide",
            ) { i ->
                val text = slides[i]
                val isQuote = text.startsWith("«")
                Column {
                    Text(
                        "${(i + 1).fa()} / ${slides.size.fa()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Gold,
                    )
                    Spacer(Modifier.height(10.dp))
                    if (isQuote) {
                        Box(Modifier.width(40.dp).height(3.dp).background(Gold))
                        Spacer(Modifier.height(14.dp))
                    }
                    Text(
                        text,
                        style = if (isQuote) MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                        else MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium, lineHeight = 38.sp),
                        color = if (isQuote) Gold else Cream,
                    )
                }
            }
        }

        // end actions
        AnimatedVisibility(
            finished,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 },
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { nav.back(); nav.detail(person.id) },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                ) {
                    Icon(Icons.Outlined.AutoStories, null); Spacer(Modifier.width(8.dp))
                    Text("زندگی‌نامه کامل", style = MaterialTheme.typography.titleSmall)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { nav.share(person.id) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Icon(Icons.Outlined.IosShare, null, tint = Cream); Spacer(Modifier.width(6.dp))
                        Text("ساخت کارت", color = Cream)
                    }
                    OutlinedButton(
                        onClick = {
                            finished = false; index = 0; lastIdx[0] = -1
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Icon(Icons.Outlined.Replay, null, tint = Cream); Spacer(Modifier.width(6.dp))
                        Text("از اول", color = Cream)
                    }
                }
            }
        }
    }
}
