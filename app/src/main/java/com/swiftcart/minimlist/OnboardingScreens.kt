package com.swiftcart.minimlist

import android.content.Context
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun OnboardingFlow(
    context: Context, 
    prefs: PreferenceManager, 
    onNoInternet: () -> Unit,
    onFinish: () -> Unit
) {
    val initialPage = remember { prefs.getOnboardingStep() }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 8 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        prefs.setOnboardingStep(pagerState.currentPage)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0C29))) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> WelcomeScreen { scope.launch { pagerState.animateScrollToPage(1) } }
                1 -> FeatureOnboardingScreen(
                    title = "Lock distracting apps instantly",
                    description = "Block apps and stay focused when it matters most",
                    content = { AppLockGraphic() },
                    onNext = { scope.launch { pagerState.animateScrollToPage(2) } },
                    onSkip = { scope.launch { pagerState.animateScrollToPage(4) } }
                )
                2 -> FeatureOnboardingScreen(
                    title = "Track your screen time",
                    description = "See where your time goes with beautiful insights",
                    content = { UsageStatsGraphic() },
                    onNext = { scope.launch { pagerState.animateScrollToPage(3) } },
                    onSkip = { scope.launch { pagerState.animateScrollToPage(4) } }
                )
                3 -> FeatureOnboardingScreen(
                    title = "Build better habits",
                    description = "Set limits and stay consistent every day",
                    content = { HabitsGraphic() },
                    buttonText = "Get Started",
                    onNext = { scope.launch { pagerState.animateScrollToPage(4) } },
                    onSkip = { scope.launch { pagerState.animateScrollToPage(4) } }
                )
                4 -> AuthFlowWrapper(context, prefs, onNoInternet) { scope.launch { pagerState.animateScrollToPage(5) } }
                5 -> PermissionsSetupScreen(context) { scope.launch { pagerState.animateScrollToPage(6) } }
                6 -> AppSelectionScreen(context, prefs) { scope.launch { pagerState.animateScrollToPage(7) } }
                7 -> CompletionScreen(onFinish)
            }
        }

        if (pagerState.currentPage > 0 && pagerState.currentPage < 4) {
            OnboardingPagerIndicator(pagerState, 3, 1)
        }
    }
}

@Composable
fun AuthFlowWrapper(
    context: Context, 
    prefs: PreferenceManager, 
    onNoInternet: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    var authMode by remember { mutableStateOf("login") } // "login", "signup", "forgot", "verify"
    var pendingEmail by remember { mutableStateOf("") }

    when (authMode) {
        "login" -> LoginScreen(
            context = context,
            prefs = prefs,
            onNoInternet = onNoInternet,
            onNavigateToSignup = { authMode = "signup" },
            onNavigateToForgotPassword = { authMode = "forgot" },
            onLoginSuccess = onAuthSuccess
        )
        "signup" -> SignupScreen(
            context = context,
            prefs = prefs,
            onNoInternet = onNoInternet,
            onNavigateToLogin = { authMode = "login" },
            onSignupOutcome = { outcome ->
                when (outcome) {
                    is SignupOutcome.VerificationRequired -> {
                        pendingEmail = outcome.email
                        authMode = "verify"
                    }
                }
            }
        )
        "verify" -> VerificationScreen(
            context = context,
            email = pendingEmail,
            prefs = prefs,
            onVerificationSuccess = onAuthSuccess,
            onNavigateBack = { authMode = "signup" }
        )
        "forgot" -> ForgotPasswordScreen(
            context = context,
            onNavigateBack = { authMode = "login" }
        )
    }
}

@Composable
fun OnboardingPagerIndicator(pagerState: PagerState, count: Int, offset: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 48.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(count) { index ->
            val isSelected = pagerState.currentPage - offset == index
            val color = if (isSelected) 
                Color.White 
            else Color.White.copy(alpha = 0.2f)
            val width by animateDpAsState(targetValue = if (isSelected) 24.dp else 8.dp, label = "dot_width")
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
