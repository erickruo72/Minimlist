package com.swiftcart.minimlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FeatureOnboardingScreen(
    title: String,
    description: String,
    content: @Composable () -> Unit,
    buttonText: String = "Next",
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val purpleAccent = Color(0xFF6A11CB)
    val blueAccent = Color(0xFF2575FC)
    val mainGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF24243E))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(mainGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                TextButton(onClick = onSkip) {
                    Text("Skip", color = Color.White.copy(alpha = 0.5f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Graphic Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
                contentAlignment = Alignment.Center
            ) {
                content()
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onNext,
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
                    Text(buttonText, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AppLockGraphic() {
    Box(contentAlignment = Alignment.Center) {
        // Phone shape
        Surface(
            modifier = Modifier.size(width = 180.dp, height = 320.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppIconPlaceholder(Color(0xFFFF5252), Icons.Default.Lock)
                    AppIconPlaceholder(Color(0xFF7C4DFF), null)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppIconPlaceholder(Color(0xFF448AFF), null)
                    AppIconPlaceholder(Color(0xFFFFAB40), null)
                }
            }
        }
    }
}

@Composable
fun AppIconPlaceholder(color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector?) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(color.copy(alpha = 0.8f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun UsageStatsGraphic() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Usage History", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.height(150.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            Bar(0.4f, Color(0xFF6A11CB))
            Bar(0.7f, Color(0xFF2575FC))
            Bar(0.5f, Color(0xFFFF5252))
            Bar(0.9f, Color(0xFF4CAF50))
            Bar(0.6f, Color(0xFFFFAB40))
        }
    }
}

@Composable
fun Bar(fraction: Float, color: Color) {
    Box(
        modifier = Modifier
            .width(24.dp)
            .fillMaxHeight(fraction)
            .background(color, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
    )
}

@Composable
fun HabitsGraphic() {
    Box(contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.size(240.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.1f)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                HabitItem("No Social Media")
                HabitItem("Work Blocks")
                HabitItem("Reading Time")
            }
        }
        // Floating Clock
        Surface(
            modifier = Modifier.size(80.dp).align(Alignment.BottomEnd).offset(x = 10.dp, y = 10.dp),
            shape = CircleShape,
            color = Color(0xFF6A11CB),
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("🕒", fontSize = 32.sp)
            }
        }
    }
}

@Composable
fun HabitItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(24.dp).background(Color(0xFF6A11CB), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, color = Color.White, fontSize = 16.sp)
    }
}

// Keeping the original name for compatibility
@Composable
fun ValueExplanationScreen(onNext: () -> Unit) {
    // This is now replaced by the pager in OnboardingFlow using FeatureOnboardingScreen
}
