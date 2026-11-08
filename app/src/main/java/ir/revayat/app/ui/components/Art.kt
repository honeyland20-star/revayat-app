package ir.revayat.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import ir.revayat.app.data.Person
import ir.revayat.app.data.Repository
import kotlin.math.abs
import kotlin.math.sin

/**
 * Hero visual for a person: their real photo if the owner added one to assets/photos,
 * otherwise a generative "girih" artwork in the person's colours.
 * [animated] adds a slow Ken-Burns zoom. [slide] picks assets/photos/<id>_<n> when present.
 * [framed] shows small (low-resolution) photos as a framed portrait over a blurred copy,
 * instead of stretching them across the whole area.
 */
@Composable
fun PersonVisual(
    person: Person,
    modifier: Modifier = Modifier,
    animated: Boolean = false,
    showTulip: Boolean = true,
    slide: Int? = null,
    framed: Boolean = false,
    frameTop: Dp = 24.dp,
    frameHeight: Float = 0.58f,
) {
    val context = LocalContext.current
    val photo = remember(person.id, slide) {
        (slide?.let { Repository.photo(context, "${person.id}_${it + 1}") }) ?: Repository.photo(context, person.id)
    }
    val scale = if (animated) {
        val t = rememberInfiniteTransition(label = "kb")
        val s by t.animateFloat(
            1f, 1.12f,
            infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Reverse),
            label = "kbScale",
        )
        s
    } else 1f

    Box(modifier.background(person.colorDark)) {
        if (photo != null) {
            val img = remember(photo) { photo.asImageBitmap() }
            val small = photo.width < 700
            if (framed && small) {
                Image(
                    bitmap = img, contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().blur(28.dp).graphicsLayer { alpha = 0.55f; scaleX = 1.2f; scaleY = 1.2f },
                )
                Box(Modifier.fillMaxSize().background(person.colorDark.copy(alpha = 0.35f)))
                Image(
                    bitmap = img,
                    contentDescription = person.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = frameTop)
                        .fillMaxHeight(frameHeight)
                        .aspectRatio(photo.width.toFloat() / photo.height)
                        .graphicsLayer { scaleX = 0.96f + (scale - 1f) * 0.4f; scaleY = scaleX }
                        .clip(RoundedCornerShape(22.dp))
                        .border(1.5.dp, person.colorAccent.copy(alpha = 0.7f), RoundedCornerShape(22.dp)),
                )
            } else {
                Image(
                    bitmap = img,
                    contentDescription = person.name,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter,
                    modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale },
                )
            }
        } else {
            GirihArt(
                dark = person.colorDark,
                accent = person.colorAccent,
                seed = person.id.hashCode(),
                showTulip = showTulip,
                modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale },
            )
        }
    }
}

/** Full-bleed decorative image from assets/images/<name>, or [fallback] if it isn't there. */
@Composable
fun AssetBackdrop(name: String, modifier: Modifier = Modifier, animated: Boolean = true, fallback: @Composable () -> Unit) {
    val context = LocalContext.current
    val bmp = remember(name) { Repository.image(context, "images", name) }
    if (bmp == null) { Box(modifier) { fallback() }; return }
    val scale = if (animated) {
        val t = rememberInfiniteTransition(label = "bd")
        val s by t.animateFloat(1f, 1.08f, infiniteRepeatable(tween(16000, easing = LinearEasing), RepeatMode.Reverse), label = "bds")
        s
    } else 1f
    Image(
        bitmap = remember(bmp) { bmp.asImageBitmap() },
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
    )
}

@Composable
fun GirihArt(dark: Color, accent: Color, seed: Int, modifier: Modifier = Modifier, showTulip: Boolean = true) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        // base gradient
        drawRect(Brush.verticalGradient(listOf(dark, Color(0xFF07090A))))
        // soft glow, position derived from seed
        val gx = 0.25f + (abs(seed) % 50) / 100f
        val gy = 0.25f + (abs(seed / 7) % 30) / 100f
        drawCircle(
            Brush.radialGradient(
                listOf(accent.copy(alpha = 0.55f), accent.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(w * gx, h * gy),
                radius = maxOf(w, h) * 0.7f,
            ),
            radius = maxOf(w, h) * 0.7f,
            center = Offset(w * gx, h * gy),
        )
        // girih star lattice
        val cell = minOf(w, h) / 3.2f
        var y = -cell / 2
        var row = 0
        while (y < h + cell) {
            var x = if (row % 2 == 0) 0f else cell / 2
            while (x < w + cell) {
                drawStar(Offset(x, y), cell * 0.36f, accent.copy(alpha = 0.10f))
                x += cell
            }
            y += cell * 0.866f
            row++
        }
        if (showTulip) {
            drawTulip(
                center = Offset(w * 0.5f, h * 0.5f),
                s = minOf(w, h) * 0.42f,
                petal = accent.copy(alpha = 0.22f),
                stroke = accent.copy(alpha = 0.35f),
            )
        }
        // bottom vignette for legibility
        drawRect(
            Brush.verticalGradient(
                0f to Color.Transparent, 0.55f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.75f),
            ),
        )
    }
}

private fun DrawScope.drawStar(c: Offset, r: Float, color: Color) {
    val stroke = Stroke(width = r * 0.06f)
    for (angle in listOf(0f, 45f)) {
        rotate(angle, c) {
            drawRect(color, topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2, r * 2), style = stroke)
        }
    }
    drawCircle(color, radius = r * 0.35f, center = c, style = stroke)
}

/** Stylised tulip (laleh) — symbol of martyrdom in Persian art. */
fun DrawScope.drawTulip(center: Offset, s: Float, petal: Color, stroke: Color) {
    val cx = center.x
    val top = center.y - s * 0.5f
    val base = center.y + s * 0.05f
    val p = Path().apply {
        moveTo(cx, base)
        cubicTo(cx - s * 0.36f, base, cx - s * 0.42f, top + s * 0.25f, cx - s * 0.36f, top + s * 0.1f)
        lineTo(cx - s * 0.17f, top + s * 0.26f)
        lineTo(cx, top)
        lineTo(cx + s * 0.17f, top + s * 0.26f)
        lineTo(cx + s * 0.36f, top + s * 0.1f)
        cubicTo(cx + s * 0.42f, top + s * 0.25f, cx + s * 0.36f, base, cx, base)
        close()
    }
    drawPath(p, petal)
    drawPath(p, stroke, style = Stroke(width = s * 0.012f))
    // stem
    drawLine(stroke, Offset(cx, base), Offset(cx, center.y + s * 0.5f), strokeWidth = s * 0.02f)
    // leaf
    val leaf = Path().apply {
        moveTo(cx, center.y + s * 0.42f)
        quadraticBezierTo(cx - s * 0.3f, center.y + s * 0.3f, cx - s * 0.28f, center.y + s * 0.12f)
        quadraticBezierTo(cx - s * 0.08f, center.y + s * 0.22f, cx, center.y + s * 0.42f)
        close()
    }
    drawPath(leaf, stroke.copy(alpha = stroke.alpha * 0.7f))
}

/** Small tulip mark used as the logo. */
@Composable
fun TulipLogo(modifier: Modifier, petal: Color, stroke: Color, ring: Color? = null) {
    Canvas(modifier) {
        val s = minOf(size.width, size.height)
        if (ring != null) {
            drawCircle(ring, radius = s * 0.48f, style = Stroke(width = s * 0.02f))
        }
        drawTulip(Offset(size.width / 2, size.height / 2), s * 0.7f, petal, stroke)
    }
}

/** Animated drifting particles (embers / stars) overlay. */
@Composable
fun Embers(modifier: Modifier = Modifier, color: Color, count: Int = 26) {
    val t = rememberInfiniteTransition(label = "embers")
    val phase by t.animateFloat(0f, 1f, infiniteRepeatable(tween(18000, easing = LinearEasing)), label = "p")
    val seeds = remember { List(count) { i -> Triple((i * 73 % 100) / 100f, (i * 37 % 100) / 100f, 0.4f + (i * 17 % 60) / 100f) } }
    Canvas(modifier) {
        seeds.forEachIndexed { i, (sx, sy, sp) ->
            val yy = ((sy - phase * sp) % 1f + 1f) % 1f
            val xx = sx + 0.02f * sin((phase * 6.28f * 2) + i)
            val a = (0.15f + 0.5f * (1 - abs(yy - 0.5f) * 2)).coerceIn(0f, 0.65f)
            drawCircle(color.copy(alpha = a), radius = 1.5f + (i % 4), center = Offset(xx * size.width, yy * size.height))
        }
    }
}
