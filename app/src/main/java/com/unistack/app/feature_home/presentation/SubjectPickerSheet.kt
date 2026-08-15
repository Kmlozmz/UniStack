package com.unistack.app.feature_home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_grades.presentation.subjectAccent
import com.unistack.app.feature_home.domain.SubjectSummary

/**
 * Elegir la materia de la nota que se va a registrar.
 *
 * Sale solo cuando hay más de una: con una sola, preguntar sobra, y sin ninguna la pregunta no
 * tiene respuesta posible —ahí el botón manda a crear la primera—.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SubjectPickerSheet(
    subjects: List<SubjectSummary>,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = UniStackColors.Background,
        shape = AppShapes.LargeCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "¿En qué materia?",
                color = UniStackColors.TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "La nota se registra dentro de una materia.",
                color = UniStackColors.TextSecondary,
                fontSize = 12.sp
            )
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(subjects, key = { it.id }) { subject ->
                    UniCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShapes.MediumCard,
                        onClick = { onSelected(subject.id) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(subjectAccent(subject.type))
                            )
                            Spacer(Modifier.width(11.dp))
                            Text(
                                subject.name,
                                modifier = Modifier.weight(1f),
                                color = UniStackColors.TextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = UniStackColors.TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
