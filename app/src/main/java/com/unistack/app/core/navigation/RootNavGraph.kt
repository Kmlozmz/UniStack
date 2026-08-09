package com.unistack.app.core.navigation

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_setup.presentation.SetupFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

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

    // Con enableEdgeToEdge la ventana deja de redimensionarse sola, así que esquivar el
    // teclado pasa a ser responsabilidad de la app. Se excluye el inset de la barra de
    // navegación porque el del teclado ya lo incluye, y muchas pantallas aplican además
    // navigationBarsPadding(): sin la exclusión ese espacio se contaría dos veces.
    //
    // Va aquí y no en la raíz porque el onboarding lo gestiona por su cuenta: su paso del
    // nombre deja que el teclado se superponga en vez de encoger la pantalla.
    val imeInsets = WindowInsets.ime.exclude(WindowInsets.navigationBars)

    // El onboarding cierra con la pantalla cubierta por el color de marca. Ese velo se pinta
    // aquí y no allí porque tiene que sobrevivir al relevo: al completarse el setup esta
    // rama sustituye SetupFlow por MainNavGraph, y cualquier cosa dibujada dentro del
    // onboarding desaparecería de golpe. Manteniéndolo fuera, el cambio ocurre debajo del
    // color y solo entonces se desvanece, así que no hay corte visible.
    var cameFromSetup by remember { mutableStateOf(false) }
    val handoffAlpha = remember { Animatable(0f) }
    LaunchedEffect(setupCompleted) {
        when {
            setupCompleted == false -> cameFromSetup = true
            setupCompleted == true && cameFromSetup && !animationsDisabled -> {
                handoffAlpha.snapTo(1f)
                // Lo justo para que el inicio quede compuesto debajo antes de destapar.
                delay(80)
                // Corto a propósito. El velo es un color claro y el inicio es oscuro, así
                // que a media opacidad la mezcla da un azul más apagado que no es ninguno
                // de los dos; alargarlo lo convertía en un color propio que se leía como un
                // segundo destello. En 180ms el paso no da tiempo a leerse como tal.
                handoffAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 180, easing = LinearEasing)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            // Sin esto asomaba el fondo blanco de la ventana en cualquier hueco en que no
            // hubiera nada opaco encima: el tema hereda de Theme.Material.Light.
            .background(UniStackColors.Background)
    ) {
        if (!isLoading) {
            when {
                setupCompleted == true -> MainNavGraph(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(imeInsets),
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

        if (handoffAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = handoffAlpha.value }
                    .background(UniStackColors.Primary)
            )
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
