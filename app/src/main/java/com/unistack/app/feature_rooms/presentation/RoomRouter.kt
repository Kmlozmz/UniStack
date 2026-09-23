package com.unistack.app.feature_rooms.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Lo de dentro de una sala. Cada sección es su propia entrada de la pila (el atrás del sistema
 * vuelve a la anterior), pero todas leen la misma sala del mismo repositorio.
 */
@Composable
fun RoomRouter(
    roomId: String,
    section: String,
    item: String,
    onBack: () -> Unit,
    onNavigate: (roomId: String, section: String, item: String) -> Unit,
    onReplace: (roomId: String, section: String, item: String) -> Unit,
    viewModel: RoomsViewModel = hiltViewModel()
) {
    val rooms by viewModel.rooms.collectAsState()
    val room = rooms.firstOrNull { it.id == roomId }
    if (room == null) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        // Si la sala se borró (o aún no ha cargado tras borrarla), atrás.
        LaunchedEffect(rooms.isNotEmpty()) { if (rooms.isNotEmpty()) onBack() }
        return
    }
    val go: (String, String) -> Unit = { s, i -> onNavigate(roomId, s, i) }
    when (section) {
        "invitar" -> InviteScreen(room, viewModel, onBack = onBack, onGoRoom = { onReplace(roomId, "sala", "-") })
        "parte" -> PartScreen(room, item, viewModel, onBack = onBack, go = go)
        "chat" -> ChatScreen(room, viewModel, onBack = onBack, go = go, prefill = item.takeIf { it.startsWith("@") }.orEmpty())
        "nov" -> NewsScreen(room, viewModel, onBack = onBack, go = go)
        "grupo" -> GroupScreen(room, viewModel, onBack = onBack, go = go)
        "gest" -> ManageScreen(room, viewModel, onBack = onBack, go = go)
        "material" -> MaterialScreen(room, viewModel, onBack = onBack, go = go)
        "crear" -> CreateWorkScreen(room, viewModel, onBack = onBack)
        else -> SalaScreen(room, viewModel, onBack = onBack, go = go)
    }
}
