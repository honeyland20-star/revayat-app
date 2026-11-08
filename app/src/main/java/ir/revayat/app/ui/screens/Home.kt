package ir.revayat.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.revayat.app.ui.LocalAppState
import ir.revayat.app.ui.LocalContent
import ir.revayat.app.ui.LocalNav
import ir.revayat.app.ui.components.AssetBackdrop
import ir.revayat.app.ui.components.Chip
import ir.revayat.app.ui.components.PersonVisual
import ir.revayat.app.ui.components.PosterCard
import ir.revayat.app.ui.components.SectionTitle
import ir.revayat.app.ui.components.TulipLogo
import ir.revayat.app.ui.theme.Cream
import ir.revayat.app.ui.theme.Gold
import ir.revayat.app.ui.theme.Ink
import ir.revayat.app.ui.theme.Olive
import ir.revayat.app.ui.theme.Tulip
import ir.revayat.app.util.Jalali
import ir.revayat.app.util.fa

@Composable
fun HomeScreen(onOpenLibrary: () -> Unit) {
    val state = LocalAppState.current
    val content = LocalContent.current
    val nav = LocalNav.current
    val ctx = LocalContext.current
    val weekDay = remember { state.weekDay() }
    val daily = content.dailyPeople

    val featured = when {
        weekDay in 1..7 -> daily.firstOrNull { it.day == weekDay }
        weekDay == 8 || state.unlockAll -> daily.firstOrNull { it.id !in state.read } ?: daily.lastOrNull()
        else -> content.byId("basij")
    } ?: content.people.first()

    val quote = remember {
        val all = content.allQuotes.filter { state.isUnlocked(it.first) }
        all[Jalali.dayOfYear() % all.size]
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { Header() }
        item { WeekBanner(weekDay, state.unlockAll) }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(daily, key = { it.id }) { p ->
                    val unlocked = state.isUnlocked(p)
                    DayBubble(
                        day = p.day,
                        name = if (unlocked) p.name.split(" ").last() else "؟",
                        state = when {
                            p.id in state.read -> 2
                            unlocked -> 1
                            else -> 0
                        },
                        isToday = p.day == weekDay,
                    ) {
                        if (unlocked) nav.story(p.id)
                        else Toast.makeText(ctx, "این روایت ${Jalali.weekDayLabel(p.day)} باز می‌شود", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        item {
            Column {
                SectionTitle(if (weekDay in 1..7) "روایت امروز" else "پیشنهاد روایت")
                Spacer(Modifier.height(8.dp))
                HeroCard(featured) { nav.story(featured.id) }
            }
        }
        item {
            QuoteCard(quote.second.text, quote.second.source) { nav.share(quote.first.id, quote.first.quotes.indexOf(quote.second)) }
        }
        item { Column {
            SectionTitle("روایت‌های هفته", "همه", onOpenLibrary)
            Spacer(Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(content.people, key = { "c" + it.id }) { p ->
                    val unlocked = state.isUnlocked(p)
                    PosterCard(p, locked = !unlocked, modifier = Modifier.size(width = 200.dp, height = 280.dp)) {
                        if (unlocked) nav.detail(p.id)
                        else Toast.makeText(ctx, "این روایت ${Jalali.weekDayLabel(p.day)} باز می‌شود", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } }
        item { ProgressCard(read = state.read.size, total = content.people.size) }
    }
}

@Composable
private fun Header() {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TulipLogo(Modifier.size(44.dp), petal = Tulip, stroke = Olive, ring = Gold)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("روایت", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            Text(
                remember { Jalali.todayString() },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WeekBanner(weekDay: Int, unlockAll: Boolean) {
    val (title, sub) = when {
        weekDay in 1..7 -> "روز ${weekDay.fa()} از هفته بسیج" to "امروز روایت تازه‌ای باز شده است. وقت شنیدن است."
        weekDay == 8 -> "هفته بسیج به پایان رسید" to "همه روایت‌ها باز است. هر زمان که خواستید، دوباره بخوانید."
        unlockAll -> "حالت پیش‌نمایش فعال است" to "همه روایت‌ها باز شده‌اند. از تنظیمات قابل تغییر است."
        else -> "${Jalali.daysUntilWeek().fa()} روز تا هفته بسیج" to "از ۲۹ آبان تا ۵ آذر، هر روز یک روایت باز می‌شود. تا آن روز «داستان یک فرمان» را بخوانید."
    }
    val t = rememberInfiniteTransition(label = "banner")
    val shift by t.animateFloat(0f, 1f, infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Reverse), label = "s")
    Box(
        Modifier.padding(horizontal = 20.dp).fillMaxWidth().heightIn(min = 170.dp).clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF2A3B22), Color(0xFF1A241D), Color(0xFF3A2A16)),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(800f * (0.6f + shift), 600f),
                ),
            )
            .border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(26.dp)),
    ) {
        AssetBackdrop("banner", Modifier.matchParentSize()) {}
        // darken the right (text) side; the banner art sits on the left
        Box(
            Modifier.matchParentSize().background(
                Brush.horizontalGradient(0f to Color.Black.copy(alpha = 0.1f), 0.45f to Color.Black.copy(alpha = 0.55f), 1f to Color.Black.copy(alpha = 0.88f)),
            ),
        )
        Column(Modifier.padding(20.dp).fillMaxWidth(0.82f)) {
            Chip("۲۹ آبان تا ۵ آذر")
            Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall, color = Cream)
            Spacer(Modifier.height(4.dp))
            Text(sub, style = MaterialTheme.typography.bodyMedium, color = Cream.copy(alpha = 0.85f))
        }
    }
}

@Composable
private fun DayBubble(day: Int, name: String, state: Int, isToday: Boolean, onClick: () -> Unit) {
    val t = rememberInfiniteTransition(label = "pulse")
    val pulse by t.animateFloat(1f, 1.08f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "p")
    Column(
        Modifier.width(64.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val ring = when (state) { 2 -> Gold; 1 -> Olive; else -> MaterialTheme.colorScheme.outline }
        Box(
            Modifier.size(58.dp).scale(if (isToday) pulse else 1f).clip(CircleShape)
                .background(if (state == 0) MaterialTheme.colorScheme.surface else ring.copy(alpha = 0.18f))
                .border(if (isToday) 3.dp else 2.dp, ring, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                2 -> Icon(Icons.Filled.Check, null, tint = Gold)
                1 -> Text(day.fa(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                else -> Icon(Icons.Filled.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        Text(Jalali.weekDayLabel(day), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), maxLines = 1)
    }
}

@Composable
private fun HeroCard(p: ir.revayat.app.data.Person, onPlay: () -> Unit) {
    Box(
        Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(440.dp).clip(RoundedCornerShape(30.dp))
            .clickable(onClick = onPlay),
    ) {
        PersonVisual(p, Modifier.fillMaxSize(), animated = true, framed = true, frameTop = 22.dp, frameHeight = 0.44f)
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0.3f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.92f)),
            ),
        )
        Column(Modifier.align(Alignment.BottomStart).padding(22.dp)) {
            Chip(p.category, color = Gold)
            Spacer(Modifier.height(12.dp))
            Text(p.name, style = MaterialTheme.typography.displaySmall, color = Cream)
            Text(p.title, style = MaterialTheme.typography.titleMedium, color = Gold)
            Spacer(Modifier.height(8.dp))
            Text(p.summary, style = MaterialTheme.typography.bodyMedium, color = Cream.copy(alpha = 0.8f))
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onPlay,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
            ) {
                Icon(Icons.Filled.PlayArrow, null)
                Spacer(Modifier.width(6.dp))
                Text("شروع روایت", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun QuoteCard(text: String, source: String, onShare: () -> Unit) {
    Column(
        Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(RoundedCornerShape(26.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(26.dp))
            .padding(22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.FormatQuote, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(8.dp))
            Text("جمله ماندگار امروز", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            Icon(
                Icons.Outlined.IosShare, "ساخت کارت",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clip(CircleShape).clickable(onClick = onShare).padding(6.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(text, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(10.dp))
        Text("— $source", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ProgressCard(read: Int, total: Int) {
    val frac = if (total == 0) 0f else read.toFloat() / total
    Column(
        Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant).padding(18.dp),
    ) {
        Text("مسیر شما", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(4.dp))
        Text(
            "${read.fa()} روایت از ${total.fa()} روایت را دیده‌اید",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))) {
            Box(Modifier.fillMaxWidth(frac).height(10.dp).clip(CircleShape).background(Brush.horizontalGradient(listOf(Olive, Gold))))
        }
        if (read >= total && total > 0) {
            Spacer(Modifier.height(10.dp))
            Text(
                "همه روایت‌ها را دیده‌اید. حالا نوبت شماست که روایت کنید.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Start,
            )
        }
    }
}
