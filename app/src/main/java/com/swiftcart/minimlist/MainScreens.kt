package com.swiftcart.minimlist

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SetupGuard(context: Context, content: @Composable () -> Unit) {
    var hasUsage by remember { mutableStateOf<Boolean>(checkUsageStatsPermission(context)) }
    var hasOverlay by remember { mutableStateOf<Boolean>(Settings.canDrawOverlays(context)) }
    var hasAccessibility by remember { mutableStateOf<Boolean>(isAccessibilityServiceEnabled(context)) }
    var isDefaultLauncher by remember { mutableStateOf<Boolean>(isDefaultLauncher(context)) }

    val allGranted = hasUsage && hasOverlay && hasAccessibility && isDefaultLauncher

    LaunchedEffect(Unit) {
        while (true) {
            hasUsage = checkUsageStatsPermission(context)
            hasOverlay = Settings.canDrawOverlays(context)
            hasAccessibility = isAccessibilityServiceEnabled(context)
            isDefaultLauncher = isDefaultLauncher(context)
            delay(2000)
        }
    }

    if (allGranted) {
        content()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Icon(
                imageVector = Icons.Default.Warning, 
                contentDescription = null, 
                modifier = Modifier.size(64.dp), 
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            @Suppress("DEPRECATION")
            Text(
                text = "Permissions Required", 
                style = MaterialTheme.typography.headlineMedium, 
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Minimlist needs these permissions to help you stay focused. Please grant them to continue.", 
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            PermissionFixItem("Usage Access", "Required to track app usage.", hasUsage) {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            PermissionFixItem("Overlay Permission", "Required to show the focus timer.", hasOverlay) {
                context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            PermissionFixItem("Accessibility", "Required to block distracting apps.", hasAccessibility) {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            PermissionFixItem("Default Launcher", "Required to simplify your home screen.", isDefaultLauncher) {
                context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
            }

            Spacer(modifier = Modifier.height(48.dp))
            
            @Suppress("DEPRECATION")
            Text(
                "Once all items are green, the app will unlock automatically.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun PermissionFixItem(label: String, description: String, granted: Boolean, onGrant: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (granted) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) 
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (granted) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (granted) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                @Suppress("DEPRECATION")
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            
            if (!granted) {
                IconButton(onClick = onGrant) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "Grant", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun NoInternetScreen(onRetry: () -> Unit) {
    val purpleAccent = Color(0xFF6A11CB)
    val blueAccent = Color(0xFF2575FC)
    val mainGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF24243E))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(mainGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SignalWifiOff,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(100.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "No Internet Connection",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Please check your network settings and try again.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(purpleAccent, blueAccent)),
                            RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Retry", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun LauncherNavigation(
    context: Context, 
    prefs: PreferenceManager, 
    initialScreen: String, 
    onLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(initialScreen) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            "home" -> HomeScreen(
                prefs = prefs, 
                onOpenApps = { currentScreen = "apps" },
                onOpenSettings = { currentScreen = "settings" }
            )
            "apps" -> AppsScreen(
                context = context, 
                prefs = prefs, 
                onBack = { currentScreen = "home" }
            )
            "settings" -> SettingsScreen(
                prefs = prefs, 
                onBack = { currentScreen = "home" }, 
                onLogout = onLogout
            )
        }
    }

    BackHandler(enabled = currentScreen != "home") {
        currentScreen = "home"
    }
}
