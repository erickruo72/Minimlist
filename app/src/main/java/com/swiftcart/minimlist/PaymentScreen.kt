package com.swiftcart.minimlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PaymentScreen(
    prefs: PreferenceManager,
    onPaymentSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedPlan by remember { mutableStateOf("monthly") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Premium Access Required",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "It looks like this device or email has already used the 7-day free trial. Subscribe now to continue staying focused.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(40.dp))

        // Monthly Plan
        PlanCard(
            title = "Monthly",
            price = "$4.99 / month",
            description = "Flexible, cancel anytime.",
            isSelected = selectedPlan == "monthly",
            onClick = { selectedPlan = "monthly" }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Annual Plan
        PlanCard(
            title = "Annual",
            price = "$39.99 / year",
            description = "Best value, save over 30%.",
            isSelected = selectedPlan == "annual",
            showTag = true,
            onClick = { selectedPlan = "annual" }
        )

        Spacer(modifier = Modifier.height(40.dp))
        
        Button(
            onClick = {
                isLoading = true
                // Simulate payment processing
                // In a real app, this would trigger a payment gateway
                val email = prefs.getUserEmail() ?: "user@example.com"
                val name = prefs.getUserName() ?: "User"
                
                // Update local preferences to reflect premium status
                prefs.setTrialInfo(
                    start = "2024-01-01 00:00:00", // Placeholder
                    end = "2099-12-31 23:59:59",   // Far future for "lifetime" or active sub
                    isPremium = true
                )
                
                onPaymentSuccess()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Pay & Continue", fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onNavigateBack, enabled = !isLoading) {
            Text("Go Back", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PlanCard(
    title: String,
    price: String,
    description: String,
    isSelected: Boolean,
    showTag: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    if (showTag) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "POPULAR",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(price, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}
