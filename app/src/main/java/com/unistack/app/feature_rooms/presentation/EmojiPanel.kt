package com.unistack.app.feature_rooms.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.EmojiNature
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.components.cleanClickable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * El icono de stickers de la barra de WhatsApp (captura del 24 sep): una cara en un cuadrado
 * redondeado cuya sonrisa termina en la esquina de abajo, doblada como una pegatina.
 */
val StickerSmile: ImageVector by lazy {
    ImageVector.Builder("StickerSmile", 24.dp, 24.dp, 24f, 24f).apply {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.9f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(13.5f, 21f)
            horizontalLineTo(8f)
            arcToRelative(5f, 5f, 0f, false, true, -5f, -5f)
            verticalLineTo(8f)
            arcToRelative(5f, 5f, 0f, false, true, 5f, -5f)
            horizontalLineTo(16f)
            arcToRelative(5f, 5f, 0f, false, true, 5f, 5f)
            verticalLineTo(13.5f)
            close()
            moveTo(8f, 14.5f)
            horizontalLineTo(20f)
            moveTo(8f, 14.5f)
            curveTo(8.3f, 17.4f, 10.6f, 19.4f, 13.6f, 19.5f)
            moveTo(13.5f, 21f)
            curveTo(13.4f, 17.3f, 16f, 14.6f, 20f, 14.5f)
        }
        path(fill = SolidColor(Color.Black)) {
            listOf(9.2f, 14.8f).forEach { cx ->
                moveTo(cx + 1.3f, 9.4f)
                arcToRelative(1.3f, 1.3f, 0f, true, true, -2.6f, 0f)
                arcToRelative(1.3f, 1.3f, 0f, true, true, 2.6f, 0f)
                close()
            }
        }
    }.build()
}

private class EmojiGroup(val label: Int, val icon: ImageVector, val emojis: List<String>)

private val EMOJI_GROUPS by lazy {
    listOf(
        EmojiGroup(R.string.rooms_emoji_faces, Icons.Outlined.EmojiEmotions,
            ("😀 😃 😄 😁 😆 😅 😂 🤣 🥲 😊 😇 🙂 🙃 😉 😌 😍 🥰 😘 😗 😙 😚 😋 😛 😝 😜 🤪 🤨 🧐 🤓 😎 🥸 🤩 🥳 😏 😒 😞 😔 😟 " +
                "😕 🙁 ☹️ 😣 😖 😫 😩 🥺 😢 😭 😤 😠 😡 🤬 🤯 😳 🥵 🥶 😱 😨 😰 😥 😓 🤗 🤔 🤭 🤫 🤥 😶 😐 😑 😬 🙄 😯 😦 😧 😮 😲 " +
                "🥱 😴 🤤 😪 😵 🤐 🥴 🤢 🤮 🤧 😷 🤒 🤕 🤑 🤠 😈 👿 👻 💀 👽 🤖 💩 🙈 🙉 🙊").split(" ")),
        EmojiGroup(R.string.rooms_emoji_hands, Icons.Outlined.PanTool,
            ("👍 👎 👏 🙌 👐 🤲 🤝 🙏 ✌️ 🤞 🤟 🤘 👌 🤌 🤏 👈 👉 👆 👇 ☝️ ✋ 🤚 🖐️ 🖖 👋 🤙 💪 ✍️ 💅 🤳 👀 🧠 🗣️ 👤 👥 🫂 " +
                "🙋 🙆 🙅 🤷 🤦 🙇 💁 🧑‍💻 🧑‍🎓 🧑‍🏫 🧑‍🔬").split(" ")),
        EmojiGroup(R.string.rooms_emoji_hearts, Icons.Outlined.FavoriteBorder,
            ("❤️ 🧡 💛 💚 💙 💜 🖤 🤍 🤎 💔 ❣️ 💕 💞 💓 💗 💖 💘 💝 💯 💢 💥 💫 💦 💨 🔥 ✨ ⭐ 🌟 ⚡ 🎉 🎊 ✅ ❌ ⭕ ❗ ❓ " +
                "‼️ ⁉️ ⚠️ 🚫 💤 🆗 🆕 🔝 🔴 🟠 🟡 🟢 🔵 🟣").split(" ")),
        EmojiGroup(R.string.rooms_emoji_study, Icons.Outlined.School,
            ("📚 📖 📝 ✏️ 🖊️ 🖍️ 📌 📍 📎 🖇️ 📏 📐 ✂️ 🗂️ 📁 📂 🗒️ 🗓️ 📅 ⏰ ⏳ ⌛ 💻 🖥️ ⌨️ 🖱️ 📱 📷 🎥 🎤 🎧 🔍 💡 🔬 🔭 🧪 " +
                "🧫 🧬 🧮 🎓 🏫 📊 📈 📉 💼 🎒 ☕ 🍕 🍔 🍟 🌮 🍩 🍪 🎂 🍿 🥤").split(" ")),
        EmojiGroup(R.string.rooms_emoji_nature, Icons.Outlined.EmojiNature,
            ("🎮 🎯 🏆 🥇 ⚽ 🏀 🎸 🎨 🎬 🎵 🎶 🌈 ☀️ 🌙 ☁️ 🌧️ ❄️ 🌸 🌻 🍀 🌱 🌵 🐶 🐱 🐼 🦊 🐸 🐵 🦄 🐝 🦋 🐢 🐙 🐧").split(" "))
    )
}

/** Quita el último carácter tal como se ve: un emoji con sus uniones cuenta como uno. */
fun String.dropLastGrapheme(): String {
    if (isEmpty()) return this
    val it = android.icu.text.BreakIterator.getCharacterInstance()
    it.setText(this)
    val start = it.preceding(length)
    return substring(0, if (start == android.icu.text.BreakIterator.DONE) 0 else start)
}

/**
 * El panel de emojis que ocupa el sitio del teclado, como en WhatsApp: la rejilla por grupos
 * arriba y, abajo, los grupos para saltar y borrar (mantenerlo sigue borrando).
 */
@Composable
fun EmojiPanel(height: Dp, onPick: (String) -> Unit, onBackspace: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val grid = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val back by rememberUpdatedState(onBackspace)
    // dónde empieza cada grupo en la rejilla: su encabezado y luego sus emojis
    val starts = remember { EMOJI_GROUPS.runningFold(0) { acc, g -> acc + 1 + g.emojis.size }.dropLast(1) }
    val current by remember { derivedStateOf { starts.indexOfLast { it <= grid.firstVisibleItemIndex }.coerceAtLeast(0) } }
    Column(Modifier.fillMaxWidth().height(height).background(cs.background)) {
        LazyVerticalGrid(GridCells.Fixed(8), Modifier.weight(1f).fillMaxWidth(), state = grid, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
            EMOJI_GROUPS.forEach { g ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(stringResource(g.label), fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurfaceVariant,
                        modifier = Modifier.padding(start = 6.dp, top = 10.dp, bottom = 4.dp))
                }
                items(g.emojis) { e ->
                    Box(Modifier.height(44.dp).clip(RoundedCornerShape(10.dp)).cleanClickable { onPick(e) }, contentAlignment = Alignment.Center) {
                        Text(e, fontSize = 25.sp)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            EMOJI_GROUPS.forEachIndexed { i, g ->
                val on = i == current
                Box(Modifier.size(40.dp).clip(CircleShape).background(if (on) cs.surfaceContainerHigh else Color.Transparent)
                    .cleanClickable { scope.launch { grid.scrollToItem(starts[i]) } }, contentAlignment = Alignment.Center) {
                    Icon(g.icon, stringResource(g.label), tint = if (on) cs.onSurface else cs.onSurfaceVariant, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(44.dp).clip(CircleShape).pointerInput(Unit) {
                detectTapGestures(onPress = {
                    back()
                    coroutineScope {
                        val repeat = launch { delay(420); while (true) { back(); delay(70) } }
                        tryAwaitRelease()
                        repeat.cancel()
                    }
                })
            }, contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Outlined.Backspace, stringResource(R.string.rooms_delete), tint = cs.onSurfaceVariant, modifier = Modifier.size(22.dp))
            }
        }
    }
}
