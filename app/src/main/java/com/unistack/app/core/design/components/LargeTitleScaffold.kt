@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement

import com.unistack.app.core.design.theme.scrollBottomRoom

/**
 * Pantalla con título grande que se recoge al desplazar.
 *
 * Ahora es un [Scaffold] con [LargeFlexibleTopAppBar], la barra de dos filas de Material 3
 * Expressive. Al hacer scroll hacia abajo, el título se comprime elásticamente en la barra
 * superior fija y vuelve a desplegarse al subir.
 */
@Composable
fun LargeTitleScaffold(
    title: String,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    horizontalPadding: Dp = 18.dp,
    topPadding: Dp = 8.dp,
    bottomPadding: Dp = scrollBottomRoom,
    itemSpacing: Dp = 12.dp,
    actions: @Composable () -> Unit = {},
    content: LazyListScope.() -> Unit
) {
    LargeTitleScaffoldLayout(
        title = title,
        onBackClick = onBackClick,
        modifier = modifier,
        subtitle = subtitle,
        actions = actions
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = topPadding,
                bottom = bottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            content = content
        )
    }
}

/**
 * Disposición base con [LargeFlexibleTopAppBar] para pantallas que manejan su propio contenedor.
 */
@Composable
fun LargeTitleScaffoldLayout(
    title: String,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                subtitle = subtitle?.let {
                    { Text(text = it, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                },
                actions = { actions() },
                scrollBehavior = scrollBehavior
            )
        },
        content = content
    )
}
