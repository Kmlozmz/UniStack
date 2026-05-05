package com.unistack.app.core.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.unistack.app.core.AppContainer
import com.unistack.app.core.design.components.UniStackLogoMarkWhite
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_setup.presentation.SetupFlow

@Composable
fun RootNavGraph(modifier: Modifier = Modifier) {
    // collectAsState with initial=null means "loading" until first emission
    val profile by AppContainer.userRepository.userProfile.collectAsState()
    var launchRoute by remember { mutableStateOf<String?>(null) }

    // userProfile StateFlow starts with null (initial value from stateIn).
    // DataStore emits immediately from disk, so this is only visible for a
    // single frame in practice. We treat null as "still loading".
    val isLoading = profile == null && !AppContainer.userRepository.didLoad

    when {
        isLoading -> UniStackLoadingScreen()
        profile?.setupCompleted == true -> MainNavGraph(
            modifier = modifier,
            initialRoute = AppRoutes.Home,
            launchRoute = launchRoute
        )
        else -> SetupFlow(
            modifier = modifier,
            onSetupFinished = { createFirstSubject ->
                launchRoute = if (createFirstSubject) AppRoutes.AddSubject else null
            }
        )
    }
}

@Composable
private fun UniStackLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UniStackColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(UniStackColors.Primary, UniStackColors.Blue)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            UniStackLogoMarkWhite(size = 38.dp)
        }
    }
}
