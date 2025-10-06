package com.synergeticsciences.qrcodegenerator.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.synergeticsciences.qrcodegenerator.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit
) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    // Animation specs
    val scaleAnimation = tween<Float>(
        durationMillis = 800,
        easing = EaseOutBack
    )

    val alphaAnimation = tween<Float>(
        durationMillis = 1000,
        easing = LinearEasing
    )

    // Launch animations
    LaunchedEffect(key1 = true) {
        // Scale animation
        scale.animateTo(
            targetValue = 1f,
            animationSpec = scaleAnimation
        )

        // Alpha animation with delay
        delay(200)
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = alphaAnimation
        )

        // Navigate after animation completes
        delay(1500)
        onNavigateToHome()
    }

    // Gradient background
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0F0F),
            Color(0xFF1A1A1A),
            Color(0xFF00FFFF).copy(alpha = 0.1f),
            Color(0xFF9D4EDD).copy(alpha = 0.1f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated QR Code Icon (placeholder for now)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Cyan.copy(alpha = 0.8f),
                                Color.Cyan.copy(alpha = 0.3f),
                                Color.Magenta.copy(alpha = 0.3f)
                            )
                        ),
                        shape = MaterialTheme.shapes.large
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "QR",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App title with fade-in animation
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = alpha.value)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle with fade-in animation
            Text(
                text = "Generate • Scan • Share",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    color = Color.Gray.copy(alpha = alpha.value * 0.8f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
