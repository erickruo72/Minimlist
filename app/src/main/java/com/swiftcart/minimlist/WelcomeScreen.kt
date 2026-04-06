package com.swiftcart.minimlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(onNext: () -> Unit) {
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Graphic Area
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                // Outer glow effect
                Surface(
                    modifier = Modifier.size(160.dp),
                    shape = CircleShape,
                    color = purpleAccent.copy(alpha = 0.1f)
                ) {}
                
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = purpleAccent,
                    modifier = Modifier.size(120.dp)
                )
                
                // Small overlapping icon for depth
                Box(
                    modifier = Modifier
                        .offset(x = 40.dp, y = 20.dp)
                        .size(50.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFFF9A8B), Color(0xFFFF6A88))),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Welcome to FocusGuard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Stay in control of your apps and time",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
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
                    Text("Get Started", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val linkStyles = TextLinkStyles(
                style = SpanStyle(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            )

            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.6f))) {
                    append("By continuing, you agree to our ")
                }
                withLink(LinkAnnotation.Url("https://thehiringguide.com/terms-of-service", linkStyles)) {
                    append("Terms")
                }
                withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.6f))) {
                    append(" and ")
                }
                withLink(LinkAnnotation.Url("https://thehiringguide.com/privacy-policy", linkStyles)) {
                    append("Privacy Policy")
                }
            }

            Text(
                text = annotatedString,
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            )
        }
    }
}
