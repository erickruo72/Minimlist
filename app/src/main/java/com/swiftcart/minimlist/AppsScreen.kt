package com.swiftcart.minimlist

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AppsScreen(context: Context, prefs: PreferenceManager, onBack: () -> Unit) {
    val allApps = remember { getInstalledApps(context) }
    val trackedPackages = remember { prefs.getTrackedPackages() }

    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(48.dp))
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search apps...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            val filtered = if (searchQuery.isBlank()) {
                allApps
            } else {
                allApps.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
            
            items(filtered) { app -> 
                AppItem(
                    context = context, 
                    app = app, 
                    isTracked = trackedPackages.contains(app.packageName)
                )
            }
        }
    }
}

@Composable
fun AppItem(context: Context, app: AppInfo, isTracked: Boolean) {
    var showDialog by remember { mutableStateOf<Boolean>(false) }
    var selectedMinutes by remember { mutableStateOf<Int>(15) }
    var customMinutes by remember { mutableStateOf("") }
    var useCustom by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                if (isTracked) showDialog = true 
                else {
                    val launch = context.packageManager.getLaunchIntentForPackage(app.packageName)
                    if (launch != null) context.startActivity(launch)
                }
            }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(if (isTracked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text(app.name.first().toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (isTracked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(app.name, style = MaterialTheme.typography.bodyLarge, fontWeight = if (isTracked) FontWeight.SemiBold else FontWeight.Normal)
        if (isTracked) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("How long?") },
            text = {
                Column {
                    Text("Select time for ${app.name}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        listOf(5, 15, 30, 60).forEach { mins ->
                            FilterChip(
                                selected = !useCustom && selectedMinutes == mins,
                                onClick = { 
                                    selectedMinutes = mins
                                    useCustom = false
                                },
                                label = { Text("${mins}m") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { 
                            customMinutes = it
                            useCustom = true
                        },
                        label = { Text("Custom minutes") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val finalMinutes = if (useCustom) {
                        customMinutes.toIntOrNull() ?: selectedMinutes
                    } else {
                        selectedMinutes
                    }
                    launchAppWithTimer(context, app, finalMinutes)
                    showDialog = false
                }) { Text("Launch") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}
