package com.maintask.app.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.action.clickable
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.maintask.app.MainActivity
import com.maintask.app.data.Task
import com.maintask.app.data.TaskDatabase
import com.maintask.app.data.daysRemaining
import com.maintask.app.data.effectiveDueAt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MaintaskWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val tasks = TaskDatabase.getInstance(context).taskDao()
            .getAllSortedByDueDateOnce()
            .take(3)
        val isDark = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        provideContent {
            WidgetBody(tasks, isDark)
        }
    }
}

@Composable
private fun WidgetBody(tasks: List<Task>, isDark: Boolean) {
    val bgColor   = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)
    val textColor = if (isDark) Color(0xFFEEEEEE) else Color(0xFF333333)
    val titleColor = if (isDark) Color(0xFF90CAF9) else Color(0xFF1565C0)
    val subColor  = if (isDark) Color(0xFFBBBBBB) else Color(0xFF555555)

    val openAppAction = actionStartActivity<MainActivity>()

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(bgColor))
            .padding(12.dp)
            .clickable(openAppAction)
    ) {
        Text(
            text = "MainTask",
            style = TextStyle(
                color = ColorProvider(titleColor),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        if (tasks.isEmpty()) {
            Text(
                text = "Aucune tâche à venir",
                style = TextStyle(color = ColorProvider(subColor), fontSize = 15.sp)
            )
        } else {
            tasks.forEach { task ->
                WidgetTaskRow(task, textColor, isDark)
            }
        }
    }
}

@Composable
private fun WidgetTaskRow(task: Task, textColor: Color, isDark: Boolean) {
    val days = task.daysRemaining
    val dueDateLabel: String = run {
        val cal = Calendar.getInstance().apply { timeInMillis = task.effectiveDueAt }
        val today = Calendar.getInstance()
        val fmt = if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR))
            SimpleDateFormat("d MMM", Locale.FRENCH)
        else
            SimpleDateFormat("d MMM yyyy", Locale.FRENCH)
        fmt.format(cal.time)
    }
    val label = when {
        days < 0  -> "En retard · $dueDateLabel"
        days == 0 -> "Aujourd'hui · $dueDateLabel"
        days == 1 -> "Demain · $dueDateLabel"
        else      -> "Dans $days jours · $dueDateLabel"
    }
    val labelColor = when {
        days < 0  -> if (isDark) Color(0xFFEF9A9A) else Color(0xFFD32F2F)
        days <= 3 -> if (isDark) Color(0xFFFFCC80) else Color(0xFFF57C00)
        else      -> if (isDark) Color(0xFFA5D6A7) else Color(0xFF388E3C)
    }
    Column(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 3.dp)
    ) {
        Text(
            text = task.title,
            style = TextStyle(color = ColorProvider(textColor), fontSize = 15.sp),
            maxLines = 1
        )
        Text(
            text = label,
            style = TextStyle(color = ColorProvider(labelColor), fontSize = 13.sp)
        )
    }
}

