package com.example.androidreminderapp.ui.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.androidreminderapp.ui.theme.NotifyNightBackground
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderScreen(
    reminderId: Long? = null,
    viewModel: AddEditReminderViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var exactAlarmDialogOpen by rememberSaveable { mutableStateOf(false) }
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val exactAlarmSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            viewModel.saveReminder(context, onNavigateBack)
        }
    }
    val audioPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.alarmSound = uri.toString()
                viewModel.errorMessage = null
            } catch (exception: SecurityException) {
                viewModel.errorMessage = "Notify could not keep access to that audio file. Choose another file."
            } catch (exception: IllegalArgumentException) {
                viewModel.errorMessage = "That audio file cannot be used. Choose another file."
            }
        }
    }

    if (exactAlarmDialogOpen) {
        AlertDialog(
            onDismissRequest = { exactAlarmDialogOpen = false },
            title = { Text("Allow exact alarms?") },
            text = {
                Text("Exact alarm access lets Notify ring at the time you choose. You can continue with less precise alarms instead.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        exactAlarmDialogOpen = false
                        exactAlarmSettingsLauncher.launch(
                            Intent(
                                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    }
                ) {
                    Text("Allow access")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        exactAlarmDialogOpen = false
                        viewModel.saveReminder(context, onNavigateBack)
                    }
                ) {
                    Text("Use approximate")
                }
            }
        )
    }

    LaunchedEffect(reminderId) {
        if (reminderId != null && reminderId > 0L) {
            viewModel.loadReminder(reminderId)
        }
    }

    val calendar = remember(viewModel.timeInMillis) {
        Calendar.getInstance().apply { timeInMillis = viewModel.timeInMillis }
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val updatedCal = Calendar.getInstance().apply {
                    timeInMillis = viewModel.timeInMillis
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                viewModel.timeInMillis = updatedCal.timeInMillis
                viewModel.startDate = updatedCal.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val updatedCal = Calendar.getInstance().apply {
                    timeInMillis = viewModel.timeInMillis
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }
                viewModel.timeInMillis = updatedCal.timeInMillis
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
    }

    val showDatePicker = {
        val selectedDate = Calendar.getInstance().apply {
            timeInMillis = viewModel.timeInMillis
        }
        datePickerDialog.updateDate(
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
    val showTimePicker = {
        val selectedTime = Calendar.getInstance().apply {
            timeInMillis = viewModel.timeInMillis
        }
        timePickerDialog.updateTime(
            selectedTime.get(Calendar.HOUR_OF_DAY),
            selectedTime.get(Calendar.MINUTE)
        )
        timePickerDialog.show()
    }

    val repeatUntilDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val updatedCal = Calendar.getInstance().apply {
                    timeInMillis = viewModel.repeatUntil ?: System.currentTimeMillis()
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                viewModel.repeatUntil = updatedCal.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (reminderId != null && reminderId > 0L) "Edit time" else "Time",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = showTimePicker) {
                        Text("Set")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Button(
                    onClick = {
                        if (
                            viewModel.isActive &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                            !alarmManager.canScheduleExactAlarms()
                        ) {
                            exactAlarmDialogOpen = true
                        } else {
                            viewModel.saveReminder(context, onNavigateBack)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .height(54.dp),
                    enabled = !viewModel.isSaving && viewModel.title.isNotBlank(),
                    shape = RoundedCornerShape(50)
                ) {
                    if (viewModel.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("◷   Set alarm")
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NotifyNightBackground()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TimeChoiceCard(
                        modifier = Modifier.weight(1f),
                        label = "Calendar date",
                        value = dateFormat.format(Date(viewModel.timeInMillis)),
                        selected = false,
                        onClick = showDatePicker
                    )
                    TimeChoiceCard(
                        modifier = Modifier.weight(1f),
                        label = "Alarm time",
                        value = timeFormat.format(Date(viewModel.timeInMillis)),
                        selected = true,
                        onClick = showTimePicker
                    )
                }

                TimeDial(
                    time = timeFormat.format(Date(viewModel.timeInMillis)),
                    onClick = showTimePicker
                )

                if (viewModel.errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = viewModel.errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Reminder details", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = viewModel.title,
                            onValueChange = { viewModel.title = it },
                            label = { Text("Reminder name *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        )

                        OutlinedTextField(
                            value = viewModel.description,
                            onValueChange = { viewModel.description = it },
                            label = { Text("Description (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        )

                        Text("Alarm sound", style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (viewModel.alarmSound == "default") {
                                "Default alarm sound"
                            } else {
                                "Custom audio file selected"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { audioPickerLauncher.launch(arrayOf("audio/*")) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Choose audio file")
                            }
                            if (viewModel.alarmSound != "default") {
                                TextButton(onClick = { viewModel.alarmSound = "default" }) {
                                    Text("Use default")
                                }
                            }
                        }
                    }
                }

            Text("Repeat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            val frequencies = listOf("NONE" to "None", "DAILY" to "Daily", "WEEKLY" to "Weekly", "MONTHLY" to "Monthly", "YEARLY" to "Yearly")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                frequencies.forEach { (code, label) ->
                    FilterChip(
                        selected = viewModel.repeatFrequency == code,
                        onClick = { viewModel.repeatFrequency = code },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            if (viewModel.repeatFrequency == "WEEKLY") {
                Text("Select Weekdays", style = MaterialTheme.typography.titleSmall)
                val days = listOf(1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun")
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    days.forEach { (dayCode, dayLabel) ->
                        FilterChip(
                            selected = viewModel.selectedWeekDays.contains(dayCode),
                            onClick = {
                                viewModel.selectedWeekDays = if (viewModel.selectedWeekDays.contains(dayCode)) {
                                    viewModel.selectedWeekDays - dayCode
                                } else {
                                    viewModel.selectedWeekDays + dayCode
                                }
                            },
                            label = { Text(dayLabel, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            Text("Repeat Until (Optional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { repeatUntilDialog.show() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (viewModel.repeatUntil != null) dateFormat.format(Date(viewModel.repeatUntil!!)) else "No End Date")
                }
                if (viewModel.repeatUntil != null) {
                    TextButton(onClick = { viewModel.repeatUntil = null }) {
                        Text("Clear")
                    }
                }
            }

            Text("Snooze Duration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            val snoozeOptions = listOf(5, 10, 15, 30)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                snoozeOptions.forEach { minutes ->
                    FilterChip(
                        selected = viewModel.snoozeMinutes == minutes,
                        onClick = { viewModel.snoozeMinutes = minutes },
                        label = { Text("$minutes mins") }
                    )
                }
            }
            }
        }
    }
}

@Composable
private fun TimeChoiceCard(
    modifier: Modifier,
    label: String,
    value: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TimeDial(time: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(276.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(238.dp)) {
            val stroke = 16.dp.toPx()
            drawArc(
                color = Color(0xFF34283A),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(Color(0xFFE956D6), Color(0xFFB91AA4), Color(0xFFE956D6))
                ),
                startAngle = 135f,
                sweepAngle = 228f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            val center = Offset(size.width / 2f, size.height / 2f)
            for (index in 0 until 12) {
                val angle = Math.toRadians((index * 30 - 90).toDouble())
                val innerRadius = size.minDimension * 0.36f
                val outerRadius = size.minDimension * 0.39f
                val start = Offset(
                    center.x + kotlin.math.cos(angle).toFloat() * innerRadius,
                    center.y + kotlin.math.sin(angle).toFloat() * innerRadius
                )
                val end = Offset(
                    center.x + kotlin.math.cos(angle).toFloat() * outerRadius,
                    center.y + kotlin.math.sin(angle).toFloat() * outerRadius
                )
                drawLine(Color(0xFFBFAFC5), start, end, strokeWidth = 1.5.dp.toPx())
            }
            val markerAngle = Math.toRadians(43.0)
            val markerRadius = size.minDimension * 0.5f
            drawCircle(
                color = Color(0xFFFF79E8),
                radius = 11.dp.toPx(),
                center = Offset(
                    center.x + kotlin.math.cos(markerAngle).toFloat() * markerRadius,
                    center.y + kotlin.math.sin(markerAngle).toFloat() * markerRadius
                )
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "REMINDER TIME",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(time, style = MaterialTheme.typography.headlineMedium)
            Text("Tap to change", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
