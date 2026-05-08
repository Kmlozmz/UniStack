package com.unistack.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.AppContainer
import com.unistack.app.core.design.theme.UniStackTheme
import com.unistack.app.core.navigation.RootNavGraph
import com.unistack.app.feature_user.domain.VisualPreference
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun UniStackApp(
    modifier: Modifier = Modifier,
    onDarkThemeChanged: (Boolean) -> Unit = {}
) {
    val visualPreference by remember {
        AppContainer.userRepository.userProfile
            .map { it?.visualPreference ?: VisualPreference.SYSTEM }
            .distinctUntilChanged()
    }.collectAsStateWithLifecycle(
        initialValue = AppContainer.userRepository.userProfile.value?.visualPreference ?: VisualPreference.SYSTEM
    )
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (visualPreference) {
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
