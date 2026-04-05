package com.maintask.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SnoozeDialog(
    onSnooze: (days: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var customMode by remember { mutableStateOf(false) }
    var customDays by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reporter la tâche") },
        text = {
            if (customMode) {
                OutlinedTextField(
                    value = customDays,
                    onValueChange = {
                        if (it.length <= 3 && it.all(Char::isDigit)) customDays = it
                    },
                    label = { Text("Nombre de jours") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1 to "1 jour", 3 to "3 jours", 7 to "7 jours", 14 to "14 jours")
                        .forEach { (days, label) ->
                            OutlinedButton(
                                onClick = { onSnooze(days) },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(label) }
                        }
                    OutlinedButton(
                        onClick = { customMode = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Autre…") }
                }
            }
        },
        confirmButton = {
            if (customMode) {
                TextButton(
                    onClick = {
                        val d = customDays.toIntOrNull()
                        if (d != null && d > 0) onSnooze(d)
                    },
                    enabled = customDays.toIntOrNull()?.let { it > 0 } == true
                ) { Text("Confirmer") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
