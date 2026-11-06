package ir.revayat.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.revayat.app.ui.LocalAppState
import ir.revayat.app.ui.LocalContent
import ir.revayat.app.ui.LocalNav
import ir.revayat.app.ui.components.PersonRow
import ir.revayat.app.ui.components.TulipLogo
import ir.revayat.app.ui.theme.Gold
import ir.revayat.app.ui.theme.Ink
import ir.revayat.app.ui.theme.Olive
import ir.revayat.app.ui.theme.Tulip
import ir.revayat.app.util.Jalali
import ir.revayat.app.util.fa

@Composable
private fun ScreenTitle(title: String, subtitle: String) {
    Column(Modifier.statusBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LibraryScreen() {
    val content = LocalContent.current
    val state = LocalAppState.current
    val nav = LocalNav.current
    val ctx = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var cat by rememberSaveable { mutableStateOf(content.categories.first()) }

    val list = content.people.filter { p ->
        (cat == content.categories.first() || p.category == cat) &&
            (query.isBlank() || (state.isUnlocked(p) && (p.name.contains(query) || p.title.contains(query) || p.summary.contains(query))))
    }

    Column(Modifier.fillMaxSize()) {
        ScreenTitle("کتابخانه روایت‌ها", "${content.people.size.fa()} روایت از مردان و نوجوانانی که ماندند")
        // search
        Row(
            Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(52.dp).clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text("جستجوی نام یا روایت…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (query.isNotEmpty()) {
                Icon(
                    Icons.Outlined.Close, "پاک کردن", tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clip(CircleShape).clickable { query = "" }.padding(4.dp),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(content.categories) { c ->
                val sel = c == cat
                Text(
                    c,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clip(RoundedCornerShape(50))
                        .background(if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .border(1.dp, if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
                        .clickable { cat = c }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.animateContentSize(),
        ) {
            if (list.isEmpty()) {
                item {
                    Text(
                        "روایتی پیدا نشد.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                    )
                }
            }
            items(list, key = { it.id }) { p ->
                val unlocked = state.isUnlocked(p)
                PersonRow(p, locked = !unlocked) {
                    if (unlocked) nav.detail(p.id)
                    else Toast.makeText(ctx, "این روایت ${Jalali.weekDayLabel(p.day)} باز می‌شود", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@Composable
fun BookmarksScreen(onBrowse: () -> Unit) {
    val content = LocalContent.current
    val state = LocalAppState.current
    val nav = LocalNav.current
    val list = content.people.filter { it.id in state.bookmarks }
    Column(Modifier.fillMaxSize()) {
        ScreenTitle("نشان‌شده‌ها", "روایت‌هایی که می‌خواهید دوباره بخوانید")
        if (list.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Outlined.BookmarkBorder, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text("هنوز روایتی نشان نکرده‌اید", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(6.dp))
                Text(
                    "در صفحه هر روایت، دکمه نشان را بزنید تا اینجا بماند.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onBrowse,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                ) { Text("دیدن روایت‌ها") }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(list, key = { it.id }) { p ->
                    PersonRow(p, locked = false, trailing = {
                        Icon(
                            Icons.Filled.Bookmark, "حذف نشان", tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clip(CircleShape).clickable { state.toggleBookmark(p.id) }.padding(8.dp),
                        )
                    }) { nav.detail(p.id) }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    val state = LocalAppState.current
    LazyColumn(contentPadding = PaddingValues(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenTitle("تنظیمات", "اپ را به سلیقه خودتان تنظیم کنید") }
        item {
            SettingCard(Icons.Outlined.Palette, "پوسته") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("تیره" to 0, "روشن" to 1, "سیستم" to 2).forEach { (label, v) ->
                        val sel = state.themeMode == v
                        Text(
                            label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                .background(if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { state.updateTheme(v) }.padding(vertical = 10.dp),
                        )
                    }
                }
            }
        }
        item {
            SettingCard(Icons.Outlined.TextFields, "اندازه متن") {
                Slider(
                    value = state.fontScale,
                    onValueChange = { state.updateFont(it) },
                    valueRange = 0.85f..1.3f,
                    steps = 8,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary),
                )
                Text("نمونه متن روایت با این اندازه نمایش داده می‌شود.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            SettingCard(Icons.Outlined.LockOpen, "باز کردن همه روایت‌ها") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "به‌طور پیش‌فرض هر روایت در روز خودش از هفته بسیج باز می‌شود. برای پیش‌نمایش یا مرور، همه را باز کنید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = state.unlockAll,
                        onCheckedChange = { state.updateUnlockAll(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                    )
                }
            }
        }
        item {
            SettingCard(Icons.Outlined.Info, "درباره روایت") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TulipLogo(Modifier.size(56.dp), petal = Tulip, stroke = Olive, ring = Gold)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("روایت — ویژه هفته بسیج", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("نسخه ۱.۰", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "روایت‌های این اپ بر پایه زندگی‌نامه‌های منتشرشده نوشته شده‌اند. لاله، نشان این اپ، در هنر ایرانی نماد شهادت است. فونت: وزیرمتن (مجوز OFL).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingCard(icon: ImageVector, title: String, body: @Composable () -> Unit) {
    Column(
        Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.height(12.dp))
        body()
    }
}
