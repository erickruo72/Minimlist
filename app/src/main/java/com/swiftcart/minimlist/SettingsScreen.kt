package com.swiftcart.minimlist

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(prefs: PreferenceManager, onBack: () -> Unit, onLogout: () -> Unit) {
    var currentSubScreen by remember { mutableStateOf("main") }
    val context = LocalContext.current

    AnimatedContent(targetState = currentSubScreen, label = "settings_nav") { screen ->
        when (screen) {
            "main" -> MainSettingsList(
                prefs = prefs,
                onBack = onBack,
                onNavigate = { currentSubScreen = it }
            )
            "focus_blocking" -> FocusBlockingSettings(prefs) { currentSubScreen = "main" }
            "analytics" -> UsageAnalyticsScreen(prefs) { currentSubScreen = "main" }
            "focus_timer" -> FocusTimerSettings(prefs) { currentSubScreen = "main" }
            "goals" -> GoalsProgressScreen(prefs) { currentSubScreen = "main" }
            "system" -> SystemShortcutsScreen(context) { currentSubScreen = "main" }
            "account" -> AccountSettingsScreen(prefs, onLogout) { currentSubScreen = "main" }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSettingsList(
    prefs: PreferenceManager,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                SettingsSectionHeader("Productivity")
                SettingsRow(
                    title = "Focus & App Blocking",
                    description = "Manage which apps to restrict",
                    icon = Icons.Default.Block,
                    onClick = { onNavigate("focus_blocking") }
                )
                SettingsRow(
                    title = "Usage Analytics",
                    description = "Visual insights into your time",
                    icon = Icons.Default.BarChart,
                    onClick = { onNavigate("analytics") }
                )
            }

            item {
                SettingsSectionHeader("Preferences")
                SettingsRow(
                    title = "Focus Settings",
                    description = "Timer behavior and limits",
                    icon = Icons.Default.Timer,
                    onClick = { onNavigate("focus_timer") }
                )
                SettingsRow(
                    title = "Goals & Progress",
                    description = "Daily targets and streaks",
                    icon = Icons.Default.EmojiEvents,
                    onClick = { onNavigate("goals") }
                )
            }

            item {
                SettingsSectionHeader("Device")
                SettingsRow(
                    title = "System Settings",
                    description = "Notifications and accessibility",
                    icon = Icons.Default.SettingsSuggest,
                    onClick = { onNavigate("system") }
                )
            }

            item {
                SettingsSectionHeader("Personal")
                SettingsRow(
                    title = "Account",
                    description = prefs.getUserEmail() ?: "Profile and security",
                    icon = Icons.Default.Person,
                    onClick = { onNavigate("account") }
                )
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 8.dp),
        fontWeight = FontWeight.Bold
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRow(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color.LightGray
            )
        }
    }
}
