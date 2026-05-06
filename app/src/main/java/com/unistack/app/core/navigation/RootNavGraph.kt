package com.unistack.app.core.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.unistack.app.core.AppContainer
import com.unistack.app.core.design.components.UniStackAnimatedLaunchScreen
import com.unistack.app.feature_setup.presentation.SetupFlow
import kotlinx.coroutines.delay

@Composable
fun RootNavGraph(modifier: Modifier = Modifier) {
    val profile by AppContainer.userRepository.userProfile.collectAsState()
    var launchRoute by remember { mutableStateOf<String?>(null) }
    var launchAnimationFinished by rememberSaveable { mutableStateOf(false) }
    var repositoryDidLoad by remember { mutableStateOf(AppContainer.userRepository.didLoad) }

    LaunchedEffect(Unit) {
        while (!repositoryDidLoad) {
            repositoryDidLoad = AppContainer.userRepository.didLoad
            if (!repositoryDidLoad) {
                delay(16)
            }
        }
    }

    val isLoading = profile == null && !repositoryDidLoad
    val showLaunchScreen = !launchAnimationFinished || isLoading

    Box(modifier = modifier.fillMaxSize()) {
        if (!isLoading) {
            when {
                profile?.setupCompleted == true -> MainNavGraph(
                    modifier = Modifier.fillMaxSize(),
                    initialRoute = AppRoutes.Home,
                    launchRoute = launchRoute
                )
                else -> SetupFlow(
                    modifier = Modifier.fillMaxSize(),
                    onSetupFinished = { createFirstSubject ->
                        launchRoute = if (createFirstSubject) AppRoutes.AddSubject else null
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = showLaunchScreen,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(animationSpec = tween(durationMillis = 0)),
            exit = fadeOut(
                animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
            ),
            label = "launchFade"
        ) {
            UniStackAnimatedLaunchScreen(
                modifier = Modifier.fillMaxSize(),
                onAnimationFinished = { launchAnimationFinished = true }
            )
        }
    }
}
