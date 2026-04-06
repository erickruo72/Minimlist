package com.swiftcart.minimlist

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftcart.minimlist.ui.theme.MinimlistTheme

class OverlayActivity : ComponentActivity() {
    
    private lateinit var prefs: PreferenceManager
    private var extensionCount = 0
    private val MAX_EXTENSIONS = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager(this)
        
        val packageName = intent.getStringExtra(FocusAccessibilityService.EXTRA_PACKAGE_NAME) ?: ""
        extensionCount = intent.getIntExtra("extension_count", 0)

        setContent {
            MinimlistTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f)
                ) {
                    var customTime by remember { mutableStateOf("") }
                    var isCustomMode by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Time is up",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.ExtraLight,
                            letterSpacing = (-1).sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your scheduled session has ended. To stay focused, it's best to exit now, but you can request a short extension if needed.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                        
                        Spacer(modifier = Modifier.height(64.dp))
                        
                        if (!isCustomMode) {
                            Button(
                                onClick = { closeTargetApp() },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Exit App", fontWeight = FontWeight.Bold)
                            }
                            
                            if (extensionCount < MAX_EXTENSIONS) {
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    ExtensionButton("2m", Modifier.weight(1f)) { addMoreTime(packageName, 2) }
                                    ExtensionButton("5m", Modifier.weight(1f)) { addMoreTime(packageName, 5) }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                TextButton(
                                    onClick = { isCustomMode = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Custom extension", color = MaterialTheme.colorScheme.primary)
                                }
                            } else {
                                Spacer(modifier = Modifier.height(32.dp))
                                Text(
                                    text = "Daily extensions limit reached.",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            OutlinedTextField(
                                value = customTime,
                                onValueChange = { if (it.length <= 3) customTime = it.filter { char -> char.isDigit() } },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Minutes", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                ),
                                suffix = { Text("min", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 24.sp)
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(
                                onClick = { 
                                    val time = customTime.toIntOrNull()
                                    if (time != null && time > 0) addMoreTime(packageName, time)
                                },
                                enabled = customTime.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Start Extension")
                            }
                            
                            TextButton(onClick = { isCustomMode = false }) {
                                Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ExtensionButton(label: String, modifier: Modifier, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = modifier.height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(label)
        }
    }

    private fun closeTargetApp() {
        val serviceIntent = Intent(this, FocusAccessibilityService::class.java).apply {
            action = FocusAccessibilityService.ACTION_CLEAR_TARGET
        }
        startService(serviceIntent)

        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun addMoreTime(packageName: String, minutes: Int) {
        val serviceIntent = Intent(this, FocusAccessibilityService::class.java).apply {
            action = FocusAccessibilityService.ACTION_START_TIMER
            putExtra(FocusAccessibilityService.EXTRA_PACKAGE_NAME, packageName)
            putExtra(FocusAccessibilityService.EXTRA_DURATION_MINUTES, minutes)
            putExtra("extension_count", extensionCount + 1)
        }
        startService(serviceIntent)
        finish()
    }
}
