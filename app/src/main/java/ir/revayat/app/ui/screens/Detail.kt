package ir.revayat.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.revayat.app.data.Person
import ir.revayat.app.ui.LocalAppState
import ir.revayat.app.ui.LocalContent
import ir.revayat.app.ui.LocalNav
import ir.revayat.app.ui.components.Chip
import ir.revayat.app.ui.components.PersonVisual
import ir.revayat.app.ui.components.PosterCard
import ir.revayat.app.ui.components.SectionTitle
import ir.revayat.app.ui.theme.Cream
import ir.revayat.app.ui.theme.Gold
import ir.revayat.app.ui.theme.Ink

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(id: String) {
    val content = LocalContent.current
    val state = LocalAppState.current
    val nav = LocalNav.current
    val ctx = LocalContext.current
    val p = content.byId(id) ?: return
    val list = rememberLazyListState()
    val heroOffset by remember {
        derivedStateOf { if (list.firstVisibleItemIndex == 0) list.firstVisibleItemScrollOffset.toFloat() else 1000f }
    }
    val related = content.people.filter { it.id != p.id && state.isUnlocked(it) }
        .sortedByDescending { it.category == p.category }.take(6)

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(state = list, contentPadding = PaddingValues(bottom = 40.dp)) {
            item {
                Box(Modifier.fillMaxWidth().height(460.dp)) {
                    PersonVisual(
                        p,
                        Modifier.fillMaxSize().graphicsLayer { translationY = heroOffset * 0.5f; alpha = 1f - (heroOffset / 900f).coerceIn(0f, 0.6f) },
                        animated = true,
                    )
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(0.4f to Color.Transparent, 1f to MaterialTheme.colorScheme.background),
                        ),
                    )
                    Column(Modifier.align(Alignment.BottomStart).padding(horizontal = 22.dp)) {
                        Chip(p.category)
                        Spacer(Modifier.height(10.dp))
                        Text(p.name, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
                        Text(p.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            item {
                FlowRow(
                    Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (p.birth.isNotBlank()) Fact(Icons.Outlined.CalendarMonth, if (p.isMartyr) "تولد: ${p.birth}" else p.birth)
                    if (p.isMartyr) Fact(Icons.Outlined.LocalFlorist, "شهادت: ${p.martyrdom}")
                    if (p.place.isNotBlank()) Fact(Icons.Outlined.Place, p.place)
                }
            }
            item {
                Row(Modifier.padding(horizontal = 22.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { nav.story(p.id) },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                    ) {
                        Icon(Icons.Filled.PlayArrow, null); Spacer(Modifier.width(6.dp))
                        Text("دیدن روایت", style = MaterialTheme.typography.titleSmall)
                    }
                    RoundAction(if (p.id in state.bookmarks) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder, "نشان کردن") {
                        state.toggleBookmark(p.id)
                    }
                    RoundAction(Icons.Outlined.Share, "اشتراک متن") {
                        val text = buildString {
                            append(p.name).append(" — ").append(p.title).append("\n\n")
                            append(p.summary).append("\n\n")
                            p.quotes.firstOrNull()?.let { append("«").append(it.text.trim('«', '»')).append("»\n— ").append(it.source).append("\n\n") }
                            append("از اپلیکیشن «روایت» — ویژه هفته بسیج")
                        }
                        val i = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
                        ctx.startActivity(Intent.createChooser(i, "اشتراک‌گذاری"))
                    }
                }
            }
            item { Column { Spacer(Modifier.height(24.dp)); SectionTitle("زندگی‌نامه") } }
            items(p.bio) { para ->
                Text(
                    para,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.88f),
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp),
                )
            }
            item { Column { Spacer(Modifier.height(18.dp)); SectionTitle("خط زمان") } }
            items(p.timeline.size) { i ->
                TimelineRow(p.timeline[i].year, p.timeline[i].text, first = i == 0, last = i == p.timeline.lastIndex, accent = MaterialTheme.colorScheme.primary)
            }
            if (p.quotes.isNotEmpty()) {
                item { Column { Spacer(Modifier.height(18.dp)); SectionTitle("جمله‌های ماندگار") } }
                items(p.quotes.size) { i ->
                    val q = p.quotes[i]
                    Column(
                        Modifier.padding(horizontal = 22.dp, vertical = 6.dp).fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Brush.linearGradient(listOf(p.colorDark, MaterialTheme.colorScheme.surface)))
                            .border(1.dp, p.colorAccent.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                            .padding(18.dp),
                    ) {
                        Icon(Icons.Filled.FormatQuote, null, tint = Gold)
                        Text(q.text.trim('«', '»'), style = MaterialTheme.typography.titleMedium, color = Cream)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("— ${q.source}", style = MaterialTheme.typography.bodySmall, color = Cream.copy(alpha = 0.7f), modifier = Modifier.weight(1f))
                            Row(
                                Modifier.clip(RoundedCornerShape(12.dp)).clickable { nav.share(p.id, i) }
                                    .background(Gold.copy(alpha = 0.15f)).padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Outlined.IosShare, null, tint = Gold, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("ساخت کارت", style = MaterialTheme.typography.labelMedium, color = Gold)
                            }
                        }
                    }
                }
            }
            if (related.isNotEmpty()) {
                item { Column {
                    Spacer(Modifier.height(18.dp))
                    SectionTitle("روایت‌های دیگر")
                    Spacer(Modifier.height(8.dp))
                    LazyRow(contentPadding = PaddingValues(horizontal = 22.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(related, key = { it.id }) { r ->
                            PosterCard(r, locked = false, modifier = Modifier.size(width = 160.dp, height = 220.dp)) { nav.detail(r.id) }
                        }
                    }
                } }
            }
            item { Spacer(Modifier.navigationBarsPadding()) }
        }

        // floating back button
        Box(
            Modifier.statusBarsPadding().padding(12.dp).size(44.dp).clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f)).clickable { nav.back() },
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت", tint = Cream) }
    }
}

@Composable
private fun Fact(icon: ImageVector, text: String) {
    Row(
        Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun RoundAction(icon: ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        Modifier.size(54.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, desc, tint = MaterialTheme.colorScheme.primary) }
}

@Composable
private fun TimelineRow(year: String, text: String, first: Boolean, last: Boolean, accent: Color) {
    Row(Modifier.padding(horizontal = 22.dp).fillMaxWidth().height(68.dp)) {
        Box(Modifier.width(24.dp).fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                Box(Modifier.width(2.dp).height(10.dp).background(if (first) Color.Transparent else accent.copy(alpha = 0.4f)))
                Box(Modifier.size(14.dp).clip(CircleShape).background(accent).border(3.dp, MaterialTheme.colorScheme.background, CircleShape))
                Box(Modifier.width(2.dp).weight(1f).background(if (last) Color.Transparent else accent.copy(alpha = 0.4f)))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.padding(top = 4.dp)) {
            Text(year, style = MaterialTheme.typography.labelLarge, color = accent)
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Suppress("unused")
private fun Person.shortName() = name.split(" ").last()
