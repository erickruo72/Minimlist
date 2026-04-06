package com.swiftcart.minimlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerSettings(prefs: PreferenceManager, onBack: () -> Unit) {
    var timerBehavior by remember { mutableStateOf(prefs.getTimerExpiryBehavior()) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Focus Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                "Timer Expiry Behavior",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = timerBehavior == PreferenceManager.EXPIRY_BEHAVIOR_ASK,
                            onClick = { 
                                timerBehavior = PreferenceManager.EXPIRY_BEHAVIOR_ASK
                                prefs.setTimerExpiryBehavior(timerBehavior)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Ask to extend", fontWeight = FontWeight.SemiBold)
                            Text("Show a dialog to add more time when finished.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = timerBehavior == PreferenceManager.EXPIRY_BEHAVIOR_EXIT,
                            onClick = { 
                                timerBehavior = PreferenceManager.EXPIRY_BEHAVIOR_EXIT
                                prefs.setTimerExpiryBehavior(timerBehavior)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Auto-exit app", fontWeight = FontWeight.SemiBold)
                            Text("Immediately close the app when time is up.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Default Session Duration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            var sliderPosition by remember { mutableStateOf(15f) }
            Text("${sliderPosition.toInt()} minutes", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                valueRange = 5f..120f,
                steps = 23
            )
            
            Text(
                "This sets the default time suggested when you open a restricted app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
