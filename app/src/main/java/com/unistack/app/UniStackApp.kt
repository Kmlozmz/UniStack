package com.unistack.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.unistack.app.core.design.theme.UniStackTheme
import com.unistack.app.core.navigation.RootNavGraph

@Composable
fun UniStackApp(modifier: Modifier = Modifier) {
    UniStackTheme {
        RootNavGraph(modifier = modifier)
    }
}
