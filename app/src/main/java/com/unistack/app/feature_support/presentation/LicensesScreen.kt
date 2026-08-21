@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_support.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.UniCard

/**
 * Una licencia y quién la lleva.
 *
 * `url` apunta al proyecto, no al texto legal: quien toca una fila quiere ver qué es esa
 * biblioteca, y desde el proyecto se llega a su licencia en un clic. Enlazar el texto plano de
 * la Apache 2.0 seis veces seguidas no informa de nada.
 */
private data class OpenSourceItem(
    val name: String,
    val author: String,
    val license: String,
    val url: String
)

private data class LicenseGroup(val label: String, val items: List<OpenSourceItem>)

private const val APACHE = "Apache 2.0"
private const val MIT = "MIT"

/*
 * Lo que UniStack usa y no escribió.
 *
 * La pantalla no existía: la app se apoya en una docena de proyectos libres y no acreditaba
 * ninguno, cuando la Apache 2.0 —la de casi todos— pide justamente eso. La lista se mantiene a
 * mano y a propósito: generarla del árbol de dependencias saca ciento y pico entradas
 * transitivas que nadie reconoce, y esconde las cinco que de verdad importan.
 */
private val LicenseGroups = listOf(
    LicenseGroup(
        "LA BASE",
        listOf(
            OpenSourceItem("Kotlin", "JetBrains", APACHE, "https://kotlinlang.org"),
            OpenSourceItem(
                "Kotlin Coroutines", "JetBrains", APACHE,
                "https://github.com/Kotlin/kotlinx.coroutines"
            ),
            OpenSourceItem(
                "Jetpack Compose", "Google", APACHE,
                "https://developer.android.com/jetpack/compose"
            ),
            OpenSourceItem(
                "Material 3 y Material Icons", "Google", APACHE,
                "https://m3.material.io"
            )
        )
    ),
    LicenseGroup(
        "ANDROID JETPACK",
        listOf(
            OpenSourceItem(
                "Room", "Google", APACHE,
                "https://developer.android.com/jetpack/androidx/releases/room"
            ),
            OpenSourceItem(
                "DataStore", "Google", APACHE,
                "https://developer.android.com/topic/libraries/architecture/datastore"
            ),
            OpenSourceItem(
                "Navigation Compose", "Google", APACHE,
                "https://developer.android.com/guide/navigation"
            ),
            OpenSourceItem(
                "WorkManager", "Google", APACHE,
                "https://developer.android.com/topic/libraries/architecture/workmanager"
            ),
            OpenSourceItem(
                "Lifecycle", "Google", APACHE,
                "https://developer.android.com/jetpack/androidx/releases/lifecycle"
            ),
            OpenSourceItem(
                "Credentials", "Google", APACHE,
                "https://developer.android.com/jetpack/androidx/releases/credentials"
            ),
            OpenSourceItem(
                "Graphics Shapes", "Google", APACHE,
                "https://developer.android.com/jetpack/androidx/releases/graphics"
            )
        )
    ),
    LicenseGroup(
        "EL RESTO",
        listOf(
            OpenSourceItem("Hilt y Dagger", "Google", APACHE, "https://dagger.dev/hilt"),
            OpenSourceItem("Coil", "Coil Contributors", APACHE, "https://coil-kt.github.io/coil"),
            OpenSourceItem("Firebase SDK", "Google", APACHE, "https://firebase.google.com"),
            OpenSourceItem(
                "Google Play Billing", "Google", APACHE,
                "https://developer.android.com/google/play/billing"
            )
        )
    )
)

/**
 * Licencias: a quién le debemos qué.
 *
 * Cada licencia se explica una sola vez, arriba, en vez de repetir el mismo párrafo legal en
 * cada fila. Abajo van los proyectos agrupados por para qué sirven, con su autor y su licencia,
 * y cada uno abre su web en el navegador.
 */
@Composable
fun LicensesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var explained by rememberSaveable { mutableStateOf(false) }

    SupportScaffold(
        title = "Licencias",
        subtitle = "El software libre que usa UniStack",
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        item(key = "que-significa") {
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                onClick = { explained = !explained }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "¿Qué significa esto?",
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Icon(
                            if (explained) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AnimatedVisibility(visible = explained) {
                        Text(
                            "UniStack está hecha sobre trabajo que otras personas publicaron " +
                                "gratis y con permiso para usarlo. La $APACHE y la $MIT dejan " +
                                "usar ese código incluso en apps que no son libres, con una " +
                                "condición: decir de quién es. Esta pantalla es esa condición.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }
        LicenseGroups.forEach { group ->
            item(key = "lbl-" + group.label) {
                Text(
                    text = group.label,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 2.dp, top = 6.dp)
                )
            }
            item(key = "grp-" + group.label) {
                UniCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Column {
                        group.items.forEach { item ->
                            LicenseRow(
                                item = item,
                                onClick = {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LicenseRow(
    item: OpenSourceItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Text(
                item.author,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
        }
        Spacer(Modifier.width(10.dp))
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Text(
                text = item.license,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Icon(
            Icons.AutoMirrored.Rounded.OpenInNew,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(17.dp)
        )
    }
}
