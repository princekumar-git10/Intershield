package com.internshield.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.internshield.app.ui.components.HolographicAiCore
import com.internshield.app.ui.components.ShieldLogo
import com.internshield.app.ui.theme.CyberBlack
import com.internshield.app.ui.theme.SolarOrangeLight
import com.internshield.app.ui.theme.TextMuted
import com.internshield.app.ui.theme.TextWhite
import com.internshield.app.viewmodel.AppScreen
import com.internshield.app.viewmodel.ShieldViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: ShieldViewModel,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(0.85f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
        alphaAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 500)
        )
        delay(900)
        viewModel.navigateTo(AppScreen.Dashboard)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scaleAnim.value)
                .alpha(alphaAnim.value)
                .padding(32.dp)
        ) {
            HolographicAiCore(size = 140.dp, isActive = true)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "InternShield",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Your Personal AI Cyber Shield",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = SolarOrangeLight,
                textAlign = TextAlign.Center
            )
        }
    }
}
