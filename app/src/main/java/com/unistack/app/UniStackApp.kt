package com.unistack.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.unistack.app.core.AppContainer
import com.unistack.app.core.design.theme.UniStackTheme
import com.unistack.app.core.navigation.RootNavGraph
import com.unistack.app.feature_user.domain.VisualPreference

@Composable
fun UniStackApp(
    modifier: Modifier = Modifier,
    onDarkThemeChanged: (Boolean) -> Unit = {}
) {
    val profile by AppContainer.userRepository.userProfile.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (profile?.visualPreference ?: VisualPreference.LIGHT) {
        VisualPreference.SYSTEM -> systemDark
        VisualPreference.LIGHT -> false
        VisualPreference.DARK -> true
    }

    SideEffect {
        onDarkThemeChanged(darkTheme)
    }

    UniStackTheme(darkTheme = darkTheme) {
        RootNavGraph(modifier = modifier)
    }
}
