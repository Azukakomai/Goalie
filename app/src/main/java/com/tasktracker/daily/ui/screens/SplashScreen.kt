package com.tasktracker.daily.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tasktracker.daily.R
import com.tasktracker.daily.ui.theme.DarkBackground
import com.tasktracker.daily.ui.theme.PrimaryEmerald
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    // Logo scale + alpha animation
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = spring(stiffness = 100f),
        label = "logoAlpha"
    )

    // App name appears slightly after logo
    var showName by remember { mutableStateOf(false) }
    val nameAlpha by animateFloatAsState(
        targetValue = if (showName) 1f else 0f,
        animationSpec = spring(stiffness = 100f),
        label = "nameAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(200)
        showName = true
        delay(800)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated logo
            Icon(
                painter = painterResource(id = R.drawable.ic_logo_g),
                contentDescription = "App Logo G",
                tint = PrimaryEmerald,
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                        alpha = logoAlpha
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App name with fade-in
            Text(
                text = "Goalie",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = PrimaryEmerald,
                modifier = Modifier.graphicsLayer { alpha = nameAlpha }
            )
        }
    }
}
