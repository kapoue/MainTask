package com.maintask.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import com.maintask.app.backup.BackupManager
import com.maintask.app.data.Task
import com.maintask.app.data.daysRemaining
import com.maintask.app.data.effectiveDueAt
import com.maintask.app.data.isSnoozed
import com.maintask.app.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TaskViewModel = viewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var snoozingTask by remember { mutableStateOf<Task?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var exportJson by remember { mutableStateOf("") }
    var pendingImportJson by remember { mutableStateOf<String?>(null) }
    var pendingImportCount by remember { mutableStateOf(0) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        try {
            context.contentResolver.openOutputStream(uri)?.use { it.write(exportJson.toByteArray()) }
            Toast.makeText(context, "Sauvegarde exportée", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Erreur lors de l'export", Toast.LENGTH_SHORT).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { it.reader().readText() }
                ?: return@rememberLauncherForActivityResult
            val parsed = BackupManager.importFromJson(json)
            pendingImportJson = json
            pendingImportCount = parsed.size
        } catch (e: Exception) {
            Toast.makeText(context, "Fichier invalide", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MainTask") },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Exporter") },
                                leadingIcon = { Icon(Icons.Filled.Backup, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    exportJson = BackupManager.exportToJson(tasks)
                                    val sdf = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
                                    exportLauncher.launch("maintask_${sdf.format(Date())}.json")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Importer") },
                                leadingIcon = { Icon(Icons.Filled.Restore, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    importLauncher.launch(arrayOf("*/*"))
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Nouvelle tâche")
            }
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onMarkDone = { viewModel.markDone(task) },
                        onEdit = { editingTask = task },
                        onSnooze = { snoozingTask = task }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        TaskFormDialog(
            task = null,
            onConfirm = { title, interval, icon ->
                viewModel.addTask(title, interval, icon)
                showAddDialog = false
            },
            onDelete = null,
            onDismiss = { showAddDialog = false }
        )
    }

    editingTask?.let { task ->
        TaskFormDialog(
            task = task,
            onConfirm = { title, interval, icon ->
                viewModel.updateTask(task.copy(title = title, intervalDays = interval, iconKey = icon))
                editingTask = null
            },
            onDelete = {
                viewModel.deleteTask(task)
                editingTask = null
            },
            onDismiss = { editingTask = null }
        )
    }

    snoozingTask?.let { task ->
        SnoozeDialog(
            onSnooze = { days ->
                viewModel.snooze(task, days)
                snoozingTask = null
            },
            onDismiss = { snoozingTask = null }
        )
    }

    pendingImportJson?.let { json ->
        AlertDialog(
            onDismissRequest = { pendingImportJson = null },
            title = { Text("Importer la sauvegarde") },
            text = { Text("Ce fichier contient $pendingImportCount tâche(s).\nLes tâches actuelles seront remplacées.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.importTasks(json)
                    pendingImportJson = null
                    Toast.makeText(context, "Import réussi", Toast.LENGTH_SHORT).show()
                }) { Text("Importer") }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportJson = null }) { Text("Annuler") }
            }
        )
    }
}

@Composable
fun TaskCard(
    task: Task,
    onMarkDone: () -> Unit,
    onEdit: () -> Unit,
    onSnooze: () -> Unit
) {
    val days = task.daysRemaining
    val snoozed = task.isSnoozed
    val statusColor = when {
        snoozed   -> Color(0xFF607D8B)  // Gris bleu – reporté
        days < 0  -> Color(0xFFD32F2F)  // Rouge     – en retard
        days <= 3 -> Color(0xFFF57C00)  // Orange    – à venir
        else      -> Color(0xFF388E3C)  // Vert      – ok
    }
    val dueDateLabel: String = run {
        val cal = Calendar.getInstance().apply { timeInMillis = task.effectiveDueAt }
        val today = Calendar.getInstance()
        val fmt = if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR))
            SimpleDateFormat("d MMM", Locale.FRENCH)
        else
            SimpleDateFormat("d MMM yyyy", Locale.FRENCH)
        fmt.format(cal.time)
    }
    val subtitle = when {
        snoozed    -> if (days <= 1) "Reporté — demain · $dueDateLabel" else "Reporté — dans $days jours · $dueDateLabel"
        days < -1  -> "En retard de ${-days} jours · $dueDateLabel"
        days == -1 -> "En retard d'1 jour · $dueDateLabel"
        days == 0  -> "À faire aujourd'hui · $dueDateLabel"
        days == 1  -> "Demain · $dueDateLabel"
        else       -> "Dans $days jours · $dueDateLabel"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable(onClick = onEdit),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(72.dp)
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Icon(
                imageVector = taskIcon(task.iconKey),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
            }

            // Bouton snooze
            IconButton(onClick = onSnooze) {
                Icon(
                    imageVector = Icons.Filled.Alarm,
                    contentDescription = "Reporter",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Bouton check
            IconButton(onClick = onMarkDone) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Marquer comme fait",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}
