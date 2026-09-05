package com.internshield.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.internshield.app.R
import com.internshield.app.ui.theme.CyberBlack
import com.internshield.app.ui.theme.CyberBorder
import com.internshield.app.ui.theme.CyberSurface
import com.internshield.app.ui.theme.SolarOrange
import com.internshield.app.ui.theme.SolarOrangeLight

/**
 * Cyber AI Holographic Security Core & Logo.
 */
@Composable
fun ShieldLogo(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    animatedGlow: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "core_pulse")
    val pulseGlow by if (animatedGlow) {
        infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        androidx.compose.runtime.mutableStateOf(0.6f)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF381408),
                        CyberSurface,
                        CyberBlack
                    ),
                    radius = size.value * 1.6f
                )
            )
            .border(
                1.5.dp,
                Brush.linearGradient(
                    colors = listOf(
                        SolarOrange.copy(alpha = pulseGlow),
                        SolarOrangeLight.copy(alpha = pulseGlow * 0.6f),
                        CyberBorder
                    )
                ),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "InternShield App Logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        )
    }
}

/**
 * Large Holographic AI Security Core Orb for Hero Section & Splash Screen.
 */
@Composable
fun HolographicAiCore(
    modifier: Modifier = Modifier,
    size: Dp = 130.dp,
    isActive: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hologram_core")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val ringRotateGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_glow"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer Radial Glowing Aura
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isActive) SolarOrange.copy(alpha = 0.45f * ringRotateGlow) else Color(0x22475569),
                        if (isActive) Color(0x18FF5E1E) else Color.Transparent,
                        Color.Transparent
                    ),
                    center = Offset(w / 2f, h / 2f),
                    radius = w * 0.52f
                )
            )

            // Orbital Concentric Dashed / Glowing Rings
            drawCircle(
                color = if (isActive) SolarOrange.copy(alpha = 0.4f * ringRotateGlow) else Color(0x33262B3B),
                radius = w * 0.47f * pulse,
                style = Stroke(
                    width = 1.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            drawCircle(
                color = if (isActive) SolarOrangeLight.copy(alpha = 0.3f) else Color(0x22262B3B),
                radius = w * 0.41f,
                style = Stroke(
                    width = 1.dp.toPx()
                )
            )
        }

        // Center 3D Glass Metallic Shield Emblem Core
        Box(
            modifier = Modifier
                .size(size * 0.76f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (isActive) Color(0xFF38150A) else Color(0xFF1D212E),
                            Color(0xFF141720),
                            CyberBlack
                        )
                    )
                )
                .border(
                    2.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            if (isActive) SolarOrange else Color(0xFF3B445D),
                            Color(0x66FF5E1E),
                            Color(0x33262B3B)
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "InternShield AI Guard Core",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
    }
}
