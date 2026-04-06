package com.swiftcart.minimlist

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun PermissionsSetupScreen(context: Context, onNext: () -> Unit) {
    var hasUsage by remember { mutableStateOf<Boolean>(checkUsageStatsPermission(context)) }
    var hasOverlay by remember { mutableStateOf<Boolean>(Settings.canDrawOverlays(context)) }
    var hasAccessibility by remember { mutableStateOf<Boolean>(isAccessibilityServiceEnabled(context)) }
    var isDefaultLauncher by remember { mutableStateOf<Boolean>(isDefaultLauncher(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            hasUsage = checkUsageStatsPermission(context)
            hasOverlay = Settings.canDrawOverlays(context)
            hasAccessibility = isAccessibilityServiceEnabled(context)
            isDefaultLauncher = isDefaultLauncher(context)
            delay(1000)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState())) {
        Text("Final Steps", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("To help you stay focused, we need a few system permissions.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OnboardingStepPermissionItem(
                label = "Usage Access", 
                description = "Used to track how much time you spend in other apps.",
                granted = hasUsage
            ) { 
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) 
            }
            
            OnboardingStepPermissionItem(
                label = "Overlay Permission", 
                description = "Allows us to show the focus timer over your other apps.",
                granted = hasOverlay
            ) { 
                context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)) 
            }
            
            OnboardingStepPermissionItem(
                label = "Accessibility", 
                description = "Required to block distracting apps when your timer runs out.",
                granted = hasAccessibility
            ) { 
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) 
            }
            
            OnboardingStepPermissionItem(
                label = "Default Launcher", 
                description = "Set Minimlist as your home screen to eliminate home screen distractions.",
                granted = isDefaultLauncher
            ) { 
                val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                context.startActivity(intent)
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNext, 
            enabled = hasUsage && hasOverlay && hasAccessibility && isDefaultLauncher, 
            modifier = Modifier.fillMaxWidth().height(56.dp), 
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Complete Setup")
        }
        
        if (!(hasUsage && hasOverlay && hasAccessibility && isDefaultLauncher)) {
            Text(
                "Please grant all permissions to continue.", 
                style = MaterialTheme.typography.labelSmall, 
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun OnboardingStepPermissionItem(label: String, description: String, granted: Boolean, onAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(16.dp), 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(if (granted) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f)), 
                        contentAlignment = Alignment.Center
                    ) {
                        if (granted) Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp)) 
                        else Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                if (!granted) {
                    Button(
                        onClick = onAction,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Grant", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}
