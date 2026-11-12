package com.example.androidreminderapp.ui.navigation

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings as AndroidSettings
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.androidreminderapp.domain.model.Reminder
import com.example.androidreminderapp.ui.home.HomeViewModel
import com.example.androidreminderapp.ui.theme.NotifyNightBackground
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class DestinationTab(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun NotifyBottomBar(
    selectedRoute: String,
    onNavigate: (String) -> Unit
) {
    val tabs = listOf(
        DestinationTab("home", "Home", Icons.Default.Home),
        DestinationTab("reminder", "Reminder", Icons.Default.Alarm),
        DestinationTab("tabs", "Tabs", Icons.Default.Apps)
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp)
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = selectedRoute == tab.route
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                            } else {
                                Color.Transparent
                            }
                        )
                        .clickable { onNavigate(tab.route) }
                        .padding(horizontal = 18.dp, vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(21.dp)
                    )
                    Text(
                        text = tab.label,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun ReminderScreen(
    viewModel: HomeViewModel,
    onAddReminder: () -> Unit,
    onEditReminder: (Long) -> Unit,
    onBack: () -> Unit = {}
) {
    val reminders by viewModel.reminders.collectAsState()
    val dateFormat = remember { SimpleDateFormat("EEE, d MMM · h:mm a", Locale.getDefault()) }
    var snoozeEnabled by rememberSaveable { mutableStateOf(false) }
    var soundEnabled by rememberSaveable { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        NightBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PageHeader(title = "Reminder", onBack = onBack)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TimeOption(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Bedtime,
                    label = "Bedtime",
                    value = "23:00",
                    highlighted = true
                )
                TimeOption(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Schedule,
                    label = "Wake Up",
                    value = "07:00",
                    highlighted = false
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                SleepDurationDial()
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                PreferenceSwitchRow(
                    icon = Icons.Default.Schedule,
                    title = "Snooze",
                    checked = snoozeEnabled,
                    onCheckedChange = { snoozeEnabled = it }
                )
                PreferenceSwitchRow(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = "Alarm Sound",
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it }
                )
            }
            Button(
                onClick = onAddReminder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(50.dp)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Set Reminder")
            }
            SectionHeading("Your reminders", "${reminders.size} saved")
            if (reminders.isEmpty()) {
                EmptyCard(
                    message = "Your reminders will show up here.",
                    action = "Create a reminder",
                    onAction = onAddReminder
                )
            } else {
                reminders.sortedBy { it.timeInMillis }.forEach { reminder ->
                    ReminderRow(reminder = reminder, dateFormat = dateFormat) {
                        onEditReminder(reminder.id)
                    }
                }
            }
        }
    }
}

@Composable
fun TabsScreen(onNavigate: (String) -> Unit) {
    val features = listOf(
        Feature("Alarms", "Manage your alarms", Icons.Default.Alarm, "home"),
        Feature("Reminders", "Add daily reminders", Icons.Default.CalendarMonth, "reminder"),
        Feature("Sleep", "Track your sleep time", Icons.Default.Bedtime, "sleep"),
        Feature("Sounds", "Alarm and notification tones", Icons.Default.MusicNote, "sounds"),
        Feature("Reports", "Your alarm activity", Icons.Default.BarChart, "reports"),
        Feature("Themes", "Change app look", Icons.Default.Palette, "themes")
    )
    Box(modifier = Modifier.fillMaxSize()) {
        NightBackground(showWaves = true)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            PageHeader(
                title = "Tabs",
                actionIcon = Icons.Default.Search,
                onAction = {}
            )
            Text(
                "Explore",
                modifier = Modifier.padding(top = 10.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(top = 10.dp, bottom = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(features) { feature ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(112.dp)
                            .clickable { onNavigate(feature.route) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                feature.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    feature.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    feature.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }
    var vibrationEnabled by rememberSaveable { mutableStateOf(true) }
    var selectedSound by rememberSaveable { mutableStateOf("Morning Tune") }
    var soundDialogOpen by rememberSaveable { mutableStateOf(false) }
    var aboutDialogOpen by rememberSaveable { mutableStateOf(false) }

    if (soundDialogOpen) {
        AlertDialog(
            onDismissRequest = { soundDialogOpen = false },
            title = { Text("Alarm sound") },
            text = {
                Column {
                    listOf("Morning Tune", "Gentle Chime", "Soft Piano").forEach { sound ->
                        TextButton(
                            onClick = {
                                selectedSound = sound
                                soundDialogOpen = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                sound,
                                color = if (selectedSound == sound) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
    if (aboutDialogOpen) {
        AlertDialog(
            onDismissRequest = { aboutDialogOpen = false },
            title = { Text("About Notify") },
            text = { Text("Notify helps you remember the things that matter. Version 1.0.0") },
            confirmButton = {
                TextButton(onClick = { aboutDialogOpen = false }) {
                    Text("Done")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NightBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PageHeader("Settings", onBack = onBack)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                )
            ) {
                Column {
                    PreferenceSwitchRow(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                    PreferenceRow(
                        icon = Icons.Default.Notifications,
                        title = "Full-screen alerts",
                        value = if (
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                            notificationManager.canUseFullScreenIntent()
                        ) {
                            "Allowed"
                        } else {
                            "Manage"
                        },
                        onClick = {
                            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                Intent(
                                    AndroidSettings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                                    Uri.parse("package:${context.packageName}")
                                )
                            } else {
                                Intent(AndroidSettings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .putExtra(AndroidSettings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                    )
                    PreferenceRow(
                        icon = Icons.Default.Alarm,
                        title = "Exact alarm access",
                        value = if (
                            Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                            alarmManager.canScheduleExactAlarms()
                        ) {
                            "Allowed"
                        } else {
                            "Manage"
                        },
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                context.startActivity(
                                    Intent(
                                        AndroidSettings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                )
                            }
                        }
                    )
                    PreferenceRow(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = "Alarm Sound",
                        value = selectedSound,
                        onClick = { soundDialogOpen = true }
                    )
                    PreferenceSwitchRow(
                        icon = Icons.Default.Vibration,
                        title = "Vibration",
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it }
                    )
                    PreferenceRow(
                        icon = Icons.Default.Palette,
                        title = "Theme",
                        value = "Purple Blue",
                        onClick = {}
                    )
                    PreferenceRow(
                        icon = Icons.Default.Language,
                        title = "Language",
                        value = "English",
                        onClick = {}
                    )
                    PreferenceRow(
                        icon = Icons.Default.Info,
                        title = "About App",
                        value = "v1.0.0",
                        onClick = { aboutDialogOpen = true },
                        showDivider = false
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    displayInfo: String,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        NightBackground(showWaves = true)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PageHeader("Profile", onBack = onBack)
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(94.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFE448D1), Color(0xFF7435C4))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Profile picture",
                    tint = Color.White,
                    modifier = Modifier.size(82.dp)
                )
            }
            Text(
                displayInfo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                "Notify account",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                )
            ) {
                Column {
                    ProfileAction("Edit Profile", Icons.Default.Person) {
                        onNavigate("edit_profile")
                    }
                    ProfileAction("My Reminders", Icons.Default.Alarm) {
                        onNavigate("reminder")
                    }
                    ProfileAction("Activity & Reports", Icons.Default.BarChart) {
                        onNavigate("reports")
                    }
                    ProfileAction(
                        title = "Logout",
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        isDestructive = true,
                        onClick = onLogout
                    )
                }
            }
        }
    }
}

@Composable
fun SleepScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        NotifyNightBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenTitle("Sleep", "A calmer routine starts tonight.")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        "Sleep schedule",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ScheduleValue("Bedtime", "23:00")
                        ScheduleValue("Wake up", "07:00")
                    }
                    Text(
                        "Your sleep schedule is ready to help you build a steady routine.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SoundsScreen() {
    var selectedSound by rememberSaveable { mutableStateOf("Morning Tune") }
    Box(modifier = Modifier.fillMaxSize()) {
        NotifyNightBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenTitle("Sounds", "Choose a tone for your alarms.")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                )
            ) {
                Column {
                    listOf("Morning Tune", "Gentle Chime", "Soft Piano").forEach { sound ->
                        PreferenceRow(
                            icon = Icons.Default.MusicNote,
                            title = sound,
                            value = if (selectedSound == sound) "Selected" else "",
                            onClick = { selectedSound = sound },
                            showDivider = sound != "Soft Piano"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportsScreen(viewModel: HomeViewModel) {
    val reminders by viewModel.reminders.collectAsState()
    val activeCount = reminders.count { it.isActive && !it.isCompleted }
    val completedCount = reminders.count { it.isCompleted }
    Box(modifier = Modifier.fillMaxSize()) {
        NotifyNightBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenTitle("Reports", "A snapshot of your reminders.")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard("Active alarms", activeCount.toString(), Modifier.weight(1f))
                SummaryCard("Completed", completedCount.toString(), Modifier.weight(1f))
            }
            SummaryCard("Total reminders", reminders.size.toString(), Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun ShellFeatureScreen(
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        NotifyNightBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenTitle(title, description)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp)
                    )
                    Text(description, textAlign = TextAlign.Center)
                    Button(onClick = onAction) { Text(actionLabel) }
                }
            }
        }
    }
}

private data class Feature(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

@Composable
private fun PageHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    onAction: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            Spacer(Modifier.size(40.dp))
        }
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (actionIcon != null) {
            IconButton(onClick = onAction, modifier = Modifier.size(40.dp)) {
                Icon(
                    actionIcon,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            Spacer(Modifier.size(40.dp))
        }
    }
}

@Composable
private fun TimeOption(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    highlighted: Boolean
) {
    Card(
        modifier = modifier.height(58.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlighted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall)
                Text(value, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun SleepDurationDial() {
    Box(
        modifier = Modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 13.dp.toPx()
            drawCircle(
                color = Color(0xFF392642),
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = Color(0xFFE448D1),
                startAngle = -90f,
                sweepAngle = 265f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            val endAngle = Math.toRadians(175.0)
            val radius = size.minDimension / 2f
            drawCircle(
                color = Color(0xFFFFF7FF),
                radius = 5.dp.toPx(),
                center = Offset(
                    x = center.x + kotlin.math.cos(endAngle).toFloat() * radius,
                    y = center.y + kotlin.math.sin(endAngle).toFloat() * radius
                )
            )
        }
        Text(
            "12",
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "3",
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "6",
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "9",
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Bedtime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(19.dp)
            )
            Text(
                "Sleep duration",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "7h 30m",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun NightBackground(showWaves: Boolean = false) {
    NotifyNightBackground(showWaves)
}

@Composable
private fun ScreenTitle(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionHeading(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ReminderRow(
    reminder: Reminder,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    dateFormat.format(Date(reminder.timeInMillis)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Edit reminder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyCard(message: String, action: String, onAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
private fun PreferenceSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(14.dp))
        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PreferenceRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(14.dp))
            Text(
                title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (value.isNotEmpty()) {
                Text(
                    value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        if (showDivider) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 52.dp, end = 16.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
            )
        }
    }
}

@Composable
private fun ProfileAction(
    title: String,
    icon: ImageVector,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Spacer(Modifier.width(14.dp))
        Text(
            title,
            modifier = Modifier.weight(1f),
            color = tint,
            style = MaterialTheme.typography.bodyMedium
        )
        if (!isDestructive) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ScheduleValue(title: String, time: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            time,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}