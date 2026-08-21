package com.unistack.app.core.navigation

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.di.rememberUniStackEntryPoint
import com.unistack.app.core.design.components.UniStackAnimatedLaunchScreen
import com.unistack.app.feature_setup.presentation.SetupFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

import androidx.compose.material3.MaterialTheme
@Composable
fun RootNavGraph(
    modifier: Modifier = Modifier,
    launchRoute: String? = null,
    onLaunchRouteConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val animationsDisabled = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
    val entryPoint = rememberUniStackEntryPoint()
    val userRepository = remember { entryPoint.userRepository() }
    val setupCompleted by remember {
        userRepository.userProfile
            .map { it?.setupCompleted }
            .distinctUntilChanged()
    }.collectAsStateWithLifecycle(initialValue = null)
    var setupLaunchRoute by remember { mutableStateOf<String?>(null) }
    var launchAnimationFinished by rememberSaveable { mutableStateOf(animationsDisabled) }
    var repositoryDidLoad by remember { mutableStateOf(userRepository.didLoad) }

    LaunchedEffect(Unit) {
        while (!repositoryDidLoad) {
            repositoryDidLoad = userRepository.didLoad
            if (!repositoryDidLoad) {
                delay(16)
            }
        }
    }

    val isLoading = setupCompleted == null && !repositoryDidLoad
    val showLaunchScreen = !launchAnimationFinished || isLoading

    // En el primer arranque la animación se ve entera: es la presentación de la marca y el
    // usuario aún no la conoce. Cumplido ese papel pasa a estorbar, así que a partir de
    // entonces corre más. No se le recorta ninguna fase; solo transcurre más deprisa.
    //
    // La decisión hay que tomarla en el primer fotograma, cuando el perfil todavía no ha
    // cargado, y por eso se consulta la pista de arranque en vez del perfil.
    val launchTimeScale = remember { if (LaunchHints.setupCompleted(context)) 0.6f else 1f }
    LaunchedEffect(setupCompleted) {
        val completed = setupCompleted ?: return@LaunchedEffect
        LaunchHints.setSetupCompleted(context, completed)
    }

    // El inicio entra con su propio fundido desde el fondo de la app.
    //
    // No hay velo de color aquí a propósito: la onda del onboarding se queda dentro del
    // onboarding. Fundir aquel color claro sobre el inicio, que es oscuro, obligaba a pasar
    // por mezclas intermedias que no son ninguno de los dos y se leían como un segundo
    // destello. Al no coincidir nunca en pantalla, ese problema no puede darse.
    val mainAlpha = remember { Animatable(if (animationsDisabled) 1f else 0f) }
    LaunchedEffect(setupCompleted, animationsDisabled) {
        if (setupCompleted != true) return@LaunchedEffect
        if (animationsDisabled) {
            mainAlpha.snapTo(1f)
        } else {
            mainAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            // Sin esto asomaba el fondo blanco de la ventana en cualquier hueco en que no
            // hubiera nada opaco encima: el tema hereda de Theme.Material.Light.
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (!isLoading) {
            when {
                setupCompleted == true -> MainNavGraph(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = mainAlpha.value },
                    initialRoute = AppRoutes.Home,
                    launchRoute = launchRoute ?: setupLaunchRoute,
                    onLaunchRouteConsumed = {
                        if (launchRoute != null) {
                            onLaunchRouteConsumed()
                        } else {
                            setupLaunchRoute = null
                        }
                    }
                )
                else -> SetupFlow(
                    modifier = Modifier.fillMaxSize(),
                    onSetupFinished = { createFirstSubject ->
                        setupLaunchRoute = if (createFirstSubject) AppRoutes.AddSubject else null
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
                timeScale = launchTimeScale,
                onAnimationFinished = { launchAnimationFinished = true }
            )
        }
    }
}
