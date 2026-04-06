package com.swiftcart.minimlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.swiftcart.minimlist.ui.theme.MinimlistTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var prefs: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager(this)
        enableEdgeToEdge()
        setContent {
            var onboardingCompleted by remember { mutableStateOf(prefs.isOnboardingCompleted()) }
            var startAtApps by remember { mutableStateOf(false) }
            var isOnline by remember { mutableStateOf(true) } // Assume online initially to avoid flicker if possible
            var isChecking by remember { mutableStateOf(true) }
            var retryCounter by remember { mutableIntStateOf(0) }

            // Periodic check for validated network capability
            LaunchedEffect(retryCounter) {
                isChecking = true
                while (true) {
                    // Check system's validated network status first
                    val systemOnline = isNetworkAvailable(this@MainActivity)
                    
                    if (systemOnline) {
                        // If system thinks we are online, do a quick socket probe to be 100% sure
                        val realAccess = withContext(Dispatchers.IO) {
                            hasRealInternetAccess()
                        }
                        isOnline = realAccess
                    } else {
                        isOnline = false
                    }
                    
                    isChecking = false
                    if (isOnline) break
                    
                    // If offline, check every 5 seconds instead of 3 to be less aggressive on battery
                    delay(5000)
                }
            }

            MinimlistTheme(prefs = prefs) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isOnline && !isChecking) {
                        NoInternetScreen {
                            retryCounter++
                        }
                    } else if (!onboardingCompleted) {
                        OnboardingFlow(this, prefs, { retryCounter++ }) {
                            prefs.setOnboardingCompleted(true)
                            startAtApps = true
                            onboardingCompleted = true
                        }
                    } else {
                        SetupGuard(this) {
                            LauncherNavigation(this, prefs, if (startAtApps) "apps" else "home") {
                                prefs.logout()
                                onboardingCompleted = false
                            }
                        }
                    }
                }
            }
        }
    }
}
