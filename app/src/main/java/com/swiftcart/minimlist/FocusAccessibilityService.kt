package com.swiftcart.minimlist

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.swiftcart.minimlist.ui.theme.MinimlistTheme

class FocusAccessibilityService : AccessibilityService(), LifecycleOwner, SavedStateRegistryOwner {

    private var targetPackage: String? = null
    private var timerHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private lateinit var prefs: PreferenceManager
    
    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    
    private val launchablePackages = mutableSetOf<String>()
    private var isSessionExpired = false

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    companion object {
        const val ACTION_START_TIMER = "com.swiftcart.minimlist.START_TIMER"
        const val ACTION_CLEAR_TARGET = "com.swiftcart.minimlist.CLEAR_TARGET"
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_DURATION_MINUTES = "extra_duration_minutes"
    }

    override fun onCreate() {
        super.onCreate()
        prefs = PreferenceManager(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        
        updateLaunchableApps()
    }

    private fun updateLaunchableApps() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        launchablePackages.clear()
        for (info in resolveInfos) {
            launchablePackages.add(info.activityInfo.packageName)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
                val minutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 0)
                if (packageName != null && minutes > 0) {
                    targetPackage = packageName
                    isSessionExpired = false
                    hideOverlay() 
                    startTimer(packageName, minutes)
                    
                    // Record stats
                    prefs.incrementAppOpens(packageName)
                    prefs.addUsage(packageName, minutes.toLong())
                }
            }
            ACTION_CLEAR_TARGET -> {
                targetPackage = null
                isSessionExpired = false
                hideOverlay()
                timerRunnable?.let { timerHandler.removeCallbacks(it) }
            }
        }
        return START_STICKY
    }

    private fun startTimer(packageName: String, minutes: Int) {
        timerRunnable?.let { timerHandler.removeCallbacks(it) }

        timerRunnable = Runnable {
            handleTimerExpiry(packageName)
        }
        timerHandler.postDelayed(timerRunnable!!, minutes * 60 * 1000L)
    }

    private fun handleTimerExpiry(packageName: String) {
        val behavior = prefs.getTimerExpiryBehavior()
        if (behavior == PreferenceManager.EXPIRY_BEHAVIOR_EXIT) {
            if (currentForegroundPackage == targetPackage) {
                exitTargetApp()
            } else if (isSystemPackage(currentForegroundPackage ?: "") || isInputMethod(currentForegroundPackage ?: "")) {
                isSessionExpired = true
            } else {
                targetPackage = null
            }
        } else {
            showOverlay(packageName)
        }
    }

    private fun exitTargetApp() {
        targetPackage = null
        isSessionExpired = false
        performGlobalAction(GLOBAL_ACTION_HOME)
        prefs.recordSessionExit()
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, "Session ended", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showOverlay(packageName: String) {
        if (overlayView != null) return

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER

        overlayView = ComposeView(this).apply {
            setContent {
                MinimlistTheme {
                    OverlayContent(
                        onExit = { 
                            exitTargetApp()
                            hideOverlay()
                        },
                        onAddTime = { minutes ->
                            prefs.recordSessionExtension()
                            prefs.addUsage(packageName, minutes.toLong())
                            targetPackage = packageName
                            isSessionExpired = false
                            hideOverlay()
                            startTimer(packageName, minutes)
                        }
                    )
                }
            }
        }

        overlayView!!.setViewTreeLifecycleOwner(this)
        overlayView!!.setViewTreeSavedStateRegistryOwner(this)
        val viewModelStore = ViewModelStore()
        overlayView!!.setViewTreeViewModelStoreOwner(object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = viewModelStore
        })

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        windowManager?.addView(overlayView, params)
    }

    private fun hideOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }
    }

    private var currentForegroundPackage: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            currentForegroundPackage = packageName

            // 1. Ignore our own app
            if (packageName == this.packageName) return

            // 2. Ignore system packages (SystemUI, Settings, Launchers)
            if (isSystemPackage(packageName)) return
            
            // 3. Ignore Input Methods (Keyboards)
            if (isInputMethod(packageName)) return

            // 4. If session is expired and we are back in target, exit now
            if (isSessionExpired && packageName == targetPackage) {
                exitTargetApp()
                return
            }

            // 5. Only track launchable apps that the user has enabled tracking for
            if (!launchablePackages.contains(packageName)) return
            if (!prefs.isAppTracked(packageName)) return

            // 6. Allow if it's the current target
            if (targetPackage != null && packageName == targetPackage) {
                return
            }

            // 7. Block unauthorized launch
            blockUnauthorizedApp(packageName, "Please use FocusTime Launcher")
        }
    }

    private fun isSystemPackage(packageName: String): Boolean {
        return packageName == "com.android.systemui" || 
               packageName == "com.android.settings" ||
               packageName == "android" ||
               packageName == "com.google.android.packageinstaller" ||
               packageName.contains("launcher", ignoreCase = true) ||
               packageName.contains("systemui", ignoreCase = true)
    }
    
    private fun isInputMethod(packageName: String): Boolean {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val imes = imm?.enabledInputMethodList
        return imes?.any { it.packageName == packageName } == true
    }

    private fun blockUnauthorizedApp(packageName: String, reason: String) {
        performGlobalAction(GLOBAL_ACTION_HOME)
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, reason, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayContent(
    onExit: () -> Unit,
    onAddTime: (Int) -> Unit
) {
    var customTime by remember { mutableStateOf("") }
    var isCustomMode by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f)
    ) {
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
                    onClick = onExit,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Exit App", fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExtensionButton("2m", Modifier.weight(1f)) { onAddTime(2) }
                    ExtensionButton("5m", Modifier.weight(1f)) { onAddTime(5) }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(
                    onClick = { isCustomMode = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Custom extension", color = MaterialTheme.colorScheme.primary)
                }
            } else {
                OutlinedTextField(
                    value = customTime,
                    onValueChange = { if (it.length <= 3) customTime = it.filter { it.isDigit() } },
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
                        if (time != null && time > 0) onAddTime(time)
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
