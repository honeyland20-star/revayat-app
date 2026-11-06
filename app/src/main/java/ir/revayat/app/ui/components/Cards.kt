package ir.revayat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.revayat.app.data.Person
import ir.revayat.app.ui.theme.Cream
import ir.revayat.app.ui.theme.Gold
import ir.revayat.app.ui.theme.Ink
import ir.revayat.app.util.Jalali

@Composable
fun Chip(text: String, modifier: Modifier = Modifier, color: Color = Gold) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

/** Tall image card with title overlay, used in carousels and the library grid. */
@Composable
fun PosterCard(
    person: Person,
    locked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
    ) {
        PersonVisual(person, Modifier.fillMaxSize().then(if (locked) Modifier.blur(14.dp) else Modifier))
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0.35f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.85f)),
            ),
        )
        if (locked) {
            Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier.size(52.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f))
                        .border(1.dp, Gold.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Filled.Lock, null, tint = Gold) }
                Spacer(Modifier.height(8.dp))
                Text(Jalali.weekDayLabel(person.day), style = MaterialTheme.typography.labelLarge, color = Cream)
            }
        }
        Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
            Chip(person.category, color = person.colorAccent.lighten())
            Spacer(Modifier.height(8.dp))
            Text(
                if (locked) "روایتی در راه است" else person.name,
                style = MaterialTheme.typography.titleLarge,
                color = Cream,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!locked) {
                Text(
                    person.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Cream.copy(alpha = 0.75f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Horizontal list row used in library/bookmarks. */
@Composable
fun PersonRow(person: Person, locked: Boolean, trailing: @Composable (() -> Unit)? = null, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(84.dp).clip(RoundedCornerShape(16.dp))) {
            PersonVisual(person, Modifier.fillMaxSize().then(if (locked) Modifier.blur(10.dp) else Modifier))
            if (locked) Icon(Icons.Filled.Lock, null, tint = Gold, modifier = Modifier.align(Alignment.Center))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (locked) "روایت ${Jalali.weekDayLabel(person.day)}" else person.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                if (locked) "این روایت در روز خودش باز می‌شود" else person.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (locked) person.category else person.lifeSpan,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (trailing != null) trailing()
        else if (!locked) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.PlayArrow, "پخش", tint = MaterialTheme.colorScheme.primary) }
        }
    }
}

@Composable
fun SectionTitle(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 4.dp, height = 18.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.primary))
            Spacer(Modifier.width(10.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        }
        if (action != null && onAction != null) {
            Text(
                action,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onAction).padding(6.dp),
            )
        }
    }
}

fun Color.lighten(f: Float = 0.25f) = Color(
    red + (1 - red) * f, green + (1 - green) * f, blue + (1 - blue) * f, alpha,
)

val InkColor = Ink
