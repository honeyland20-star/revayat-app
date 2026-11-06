package ir.revayat.app.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import ir.revayat.app.data.Person
import ir.revayat.app.ui.LocalContent
import ir.revayat.app.ui.LocalNav
import ir.revayat.app.ui.components.GirihArt
import ir.revayat.app.ui.components.PersonVisual
import ir.revayat.app.ui.components.TulipLogo
import ir.revayat.app.ui.theme.Cream
import ir.revayat.app.ui.theme.Gold
import ir.revayat.app.ui.theme.Ink
import ir.revayat.app.ui.theme.Olive
import ir.revayat.app.ui.theme.Tulip
import ir.revayat.app.ui.theme.Vazir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private val templates = listOf("تصویر", "شب طلایی", "لاله", "کاغذ")

@Composable
fun ShareCardScreen(id: String, quoteIndex: Int) {
    val content = LocalContent.current
    val nav = LocalNav.current
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val person = content.byId(id) ?: return
    var qi by remember { mutableIntStateOf(quoteIndex.coerceIn(0, (person.quotes.size - 1).coerceAtLeast(0))) }
    var template by remember { mutableIntStateOf(0) }
    val layer = rememberGraphicsLayer()

    val quoteText = person.quotes.getOrNull(qi)?.text?.trim('«', '»') ?: person.summary
    val source = person.quotes.getOrNull(qi)?.source ?: person.name

    fun capture(onBitmap: suspend (Bitmap) -> Unit) {
        scope.launch {
            val bmp = layer.toImageBitmap().asAndroidBitmap()
            onBitmap(bmp)
        }
    }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()),
    ) {
        Row(Modifier.statusBarsPadding().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).clickable { nav.back() },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت", tint = MaterialTheme.colorScheme.onBackground) }
            Text("ساخت کارت روایت", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        }

        // the card
        Box(
            Modifier.padding(horizontal = 24.dp, vertical = 8.dp).fillMaxWidth().aspectRatio(4f / 5f)
                .clip(RoundedCornerShape(24.dp))
                .drawWithContent {
                    layer.record { this@drawWithContent.drawContent() }
                    drawLayer(layer)
                },
        ) {
            CardContent(person, quoteText, source, template)
        }

        Spacer(Modifier.height(12.dp))
        Text("سبک کارت", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(Modifier.height(8.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(templates.indices.toList()) { i -> Pill(templates[i], i == template) { template = i } }
        }
        if (person.quotes.size > 1) {
            Spacer(Modifier.height(14.dp))
            Text("جمله", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(Modifier.height(8.dp))
            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(person.quotes.indices.toList()) { i ->
                    Pill(person.quotes[i].text.trim('«', '»').take(28) + "…", i == qi) { qi = i }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.padding(horizontal = 24.dp).navigationBarsPadding().padding(bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { capture { bmp -> shareBitmap(ctx, bmp, person) } },
                modifier = Modifier.weight(1f).height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
            ) {
                Icon(Icons.Outlined.IosShare, null); Spacer(Modifier.width(6.dp))
                Text("اشتراک‌گذاری", style = MaterialTheme.typography.titleSmall)
            }
            OutlinedButton(
                onClick = { capture { bmp -> saveBitmap(ctx, bmp, person) } },
                modifier = Modifier.weight(1f).height(54.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Outlined.Download, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(6.dp))
                Text("ذخیره در گالری", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun Pill(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        modifier = Modifier.clip(RoundedCornerShape(50))
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun CardContent(person: Person, quote: String, source: String, template: Int) {
    val paper = template == 3
    val textColor = if (paper) Color(0xFF2A2116) else Cream
    val accent = when (template) { 2 -> Color(0xFFFFD7C2); 3 -> Color(0xFF8C6A26); else -> Gold }
    Box(Modifier.fillMaxSize()) {
        when (template) {
            0 -> {
                PersonVisual(person, Modifier.fillMaxSize(), showTulip = false)
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.35f), Color.Black.copy(alpha = 0.85f)))))
            }
            1 -> GirihArt(Color(0xFF151A14), Gold, seed = 5, showTulip = false, modifier = Modifier.fillMaxSize())
            2 -> GirihArt(Color(0xFF3A0F12), Color(0xFFE0574F), seed = 13, showTulip = true, modifier = Modifier.fillMaxSize())
            else -> Box(
                Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF8F1E1), Color(0xFFEADBBB))))
                    .border(10.dp, Color(0xFFD9C394)),
            )
        }
        Column(Modifier.fillMaxSize().padding(26.dp)) {
            Text("❝", fontSize = 54.sp, color = accent, fontFamily = Vazir)
            Spacer(Modifier.weight(1f))
            Text(
                quote,
                fontFamily = Vazir,
                fontWeight = FontWeight.Black,
                fontSize = if (quote.length > 70) 22.sp else 27.sp,
                lineHeight = if (quote.length > 70) 38.sp else 44.sp,
                color = textColor,
            )
            Spacer(Modifier.height(14.dp))
            Box(Modifier.width(36.dp).height(3.dp).background(accent))
            Spacer(Modifier.height(10.dp))
            Text(source, fontFamily = Vazir, fontSize = 14.sp, color = textColor.copy(alpha = 0.8f))
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TulipLogo(Modifier.size(30.dp), petal = Tulip, stroke = Olive, ring = accent)
                Spacer(Modifier.width(8.dp))
                Text("روایت | هفته بسیج", fontFamily = Vazir, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor.copy(alpha = 0.85f), textAlign = TextAlign.Start)
            }
        }
    }
}

private suspend fun shareBitmap(ctx: Context, bmp: Bitmap, person: Person) {
    val uri = withContext(Dispatchers.IO) {
        val dir = File(ctx.cacheDir, "shared").apply { mkdirs() }
        val f = File(dir, "revayat_${person.id}.png")
        FileOutputStream(f).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", f)
    }
    val i = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "${person.name} — ${person.title}\nاز اپلیکیشن «روایت»")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    ctx.startActivity(Intent.createChooser(i, "اشتراک‌گذاری کارت"))
}

private suspend fun saveBitmap(ctx: Context, bmp: Bitmap, person: Person) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        // Older Android needs a storage permission to write to the gallery; offer sharing instead.
        shareBitmap(ctx, bmp, person)
        return
    }
    val ok = withContext(Dispatchers.IO) {
        runCatching {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "revayat_${person.id}_${System.currentTimeMillis()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Revayat")
            }
            val uri = ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
            ctx.contentResolver.openOutputStream(uri)!!.use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }.isSuccess
    }
    Toast.makeText(ctx, if (ok) "کارت در گالری ذخیره شد" else "ذخیره انجام نشد", Toast.LENGTH_SHORT).show()
}
