package com.maintask.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.ui.graphics.vector.ImageVector

data class TaskIconOption(val key: String, val icon: ImageVector, val label: String)

val taskIconOptions = listOf(
    TaskIconOption("moto",     Icons.Filled.TwoWheeler,                  "Scooter"),
    TaskIconOption("car",      Icons.Filled.DirectionsCar,               "Voiture"),
    TaskIconOption("bike",     Icons.AutoMirrored.Filled.DirectionsBike, "Vélo"),
    TaskIconOption("security", Icons.Filled.Security,                    "Sécurité"),
    TaskIconOption("home",     Icons.Filled.Home,                        "Maison"),
    TaskIconOption("task",     Icons.Filled.Task,                        "Autre"),
)

fun taskIcon(iconKey: String): ImageVector =
    taskIconOptions.find { it.key == iconKey }?.icon ?: Icons.Filled.Task
