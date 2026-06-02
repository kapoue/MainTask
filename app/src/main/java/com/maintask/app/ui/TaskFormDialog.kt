package com.maintask.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.maintask.app.data.Task
import java.util.Calendar

// Jours de la semaine — (label, Calendar.DAY_OF_WEEK)
private val WEEK_DAYS = listOf(
    "Lun" to Calendar.MONDAY,
    "Mar" to Calendar.TUESDAY,
    "Mer" to Calendar.WEDNESDAY,
    "Jeu" to Calendar.THURSDAY,
    "Ven" to Calendar.FRIDAY,
    "Sam" to Calendar.SATURDAY,
    "Dim" to Calendar.SUNDAY
)

@Composable
fun TaskFormDialog(
    task: Task?,
    onConfirm: (title: String, intervalDays: Int, iconKey: String, note: String,
                recurrenceType: String, weekDays: Int, monthDay: Int) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    var title          by remember { mutableStateOf(task?.title ?: "") }
    var intervalText   by remember { mutableStateOf(
        if (task == null || task.recurrenceType == "DAYS") task?.intervalDays?.toString() ?: ""
        else ""
    ) }
    var selectedIcon   by remember { mutableStateOf(task?.iconKey ?: "") }
    var note           by remember { mutableStateOf(task?.note ?: "") }
    var recurrenceType by remember { mutableStateOf(task?.recurrenceType ?: "DAYS") }
    var weekDays       by remember { mutableIntStateOf(task?.weekDays ?: 0) }
    var monthDays      by remember { mutableIntStateOf(task?.monthDays ?: 0) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer la tâche") },
            text  = { Text("Supprimer \"${task?.title}\" définitivement ?") },
            confirmButton = {
                TextButton(
                    onClick = { onDelete?.invoke() },
                    colors  = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Annuler") }
            }
        )
        return
    }

    val isValid = title.isNotBlank() && selectedIcon.isNotBlank() && when (recurrenceType) {
        "DAYS"    -> intervalText.toIntOrNull()?.let { it > 0 } == true
        "WEEKLY"  -> weekDays != 0
        "MONTHLY" -> monthDays != 0
        else      -> false
    }

    val recurrencePreview: String = when (recurrenceType) {
        "DAYS" -> {
            val n = intervalText.toIntOrNull()
            when {
                n == null || n <= 0 -> ""
                n == 1 -> "Tous les jours"
                else   -> "Tous les $n jours"
            }
        }
        "WEEKLY" -> {
            if (weekDays == 0) ""
            else {
                val names = WEEK_DAYS
                    .filter { (_, cal) -> (weekDays shr cal) and 1 == 1 }
                    .map { (name, _) -> name.lowercase() }
                when (names.size) {
                    1    -> "Chaque ${names[0]}"
                    else -> "Chaque ${names.dropLast(1).joinToString(", ")} et ${names.last()}"
                }
            }
        }
        "MONTHLY" -> {
            if (monthDays == 0) ""
            else {
                val parts = mutableListOf<String>()
                for (d in 1..31) {
                    if ((monthDays shr d) and 1 == 1) parts.add(if (d == 1) "1er" else "$d")
                }
                if (monthDays and 1 == 1) parts.add("dernier jour")
                when (parts.size) {
                    0    -> ""
                    1    -> "Le ${parts[0]} de chaque mois"
                    else -> "Le ${parts.dropLast(1).joinToString(", ")} et ${parts.last()} de chaque mois"
                }
            }
        }
        else -> ""
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape          = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier            = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text  = if (task == null) "Nouvelle tâche" else "Modifier la tâche",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value         = title,
                    onValueChange = { title = it },
                    label         = { Text("Intitulé") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value         = note,
                    onValueChange = { note = it },
                    label         = { Text("Note (optionnel)") },
                    modifier      = Modifier.fillMaxWidth(),
                    minLines      = 2,
                    maxLines      = 4
                )

                // ── Récurrence ──────────────────────────────────────────────
                Text("Récurrence", style = MaterialTheme.typography.labelMedium)

                // Segmented control
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                ) {
                    listOf("DAYS" to "Jours", "WEEKLY" to "Hebdo", "MONTHLY" to "Mensuel")
                        .forEach { (key, label) ->
                            val isActive = recurrenceType == key
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isActive) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable {
                                        recurrenceType = key
                                        if (key == "WEEKLY")  weekDays  = 0
                                        if (key == "MONTHLY") monthDays = 0
                                    }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text  = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isActive) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                }

                when (recurrenceType) {
                    "DAYS" -> {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value           = intervalText,
                                onValueChange   = {
                                    if (it.length <= 4 && it.all(Char::isDigit)) intervalText = it
                                },
                                label           = { Text("Intervalle") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine      = true,
                                modifier        = Modifier.weight(1f)
                            )
                            Text(
                                text  = "jours",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    "WEEKLY" -> {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            WEEK_DAYS.forEach { (label, calDay) ->
                                val isSelected = (weekDays shr calDay) and 1 == 1
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { weekDays = weekDays xor (1 shl calDay) }
                                ) {
                                    Text(
                                        text      = label,
                                        style     = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center,
                                        color     = if (isSelected) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    "MONTHLY" -> {
                        MonthDayGrid(selected = monthDays, onToggle = { monthDays = monthDays xor it })
                    }
                }

                // Prévisualisation
                if (recurrencePreview.isNotEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        Text(
                            text  = recurrencePreview,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // ── Icône ────────────────────────────────────────────────────
                Text("Icône", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(taskIconOptions) { option ->
                        val isSelected = option.key == selectedIcon
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                )
                                .clickable { selectedIcon = option.key }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector        = option.icon,
                                contentDescription = option.label,
                                tint               = if (isSelected) MaterialTheme.colorScheme.primary
                                                     else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier           = Modifier.size(28.dp)
                            )
                            Text(
                                text  = option.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ── Actions ──────────────────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    if (onDelete != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                imageVector        = Icons.Filled.Delete,
                                contentDescription = "Supprimer",
                                tint               = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text("Annuler") }
                    Button(
                        onClick = {
                            val days = when (recurrenceType) {
                                "WEEKLY"  -> 7
                                "MONTHLY" -> 30
                                else      -> intervalText.toInt()
                            }
                            onConfirm(
                                title.trim(), days, selectedIcon, note.trim(),
                                recurrenceType, weekDays, monthDays
                            )
                        },
                        enabled = isValid
                    ) { Text("Confirmer") }
                }
            }
        }
    }
}

// ── Grille jours du mois ─────────────────────────────────────────────────────

// selected : bitmask (bit 0 = dernier jour, bits 1-31 = jours 1-31)
// onToggle : appelé avec le bit à XOR
@Composable
private fun MonthDayGrid(selected: Int, onToggle: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Lignes 1-28
        for (row in 0 until 4) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (col in 0 until 7) {
                    val day = row * 7 + col + 1
                    DayCell(
                        day        = day,
                        isSelected = (selected shr day) and 1 == 1,
                        isDimmed   = false,
                        modifier   = Modifier.weight(1f)
                    ) { onToggle(1 shl day) }
                }
            }
        }
        // Ligne 29-31 + "Dernier jour"
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(29, 30, 31).forEach { day ->
                DayCell(
                    day        = day,
                    isSelected = (selected shr day) and 1 == 1,
                    isDimmed   = true,
                    modifier   = Modifier.weight(1f)
                ) { onToggle(1 shl day) }
            }
            val isLastSelected = selected and 1 == 1
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(4f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isLastSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onToggle(1) }   // bit 0
            ) {
                Text(
                    text      = "Dernier jour",
                    style     = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color     = if (isLastSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    isDimmed: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
    ) {
        Text(
            text  = day.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isDimmed   -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                else       -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
