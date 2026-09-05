package com.internshield.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.internshield.app.ui.theme.AccentOrange
import com.internshield.app.ui.theme.CardElevated
import com.internshield.app.ui.theme.DangerPinkRed
import com.internshield.app.ui.theme.RiskScoreTextStyle
import com.internshield.app.ui.theme.SuccessNeonGreen
import com.internshield.app.ui.theme.TextMutedGray
import com.internshield.app.ui.theme.TextWhite
import com.internshield.app.ui.theme.WarningAmber

/**
 * Futuristic Semicircular Threat Risk Gauge with Orange -> Pink/Red progression.
 */
@Composable
fun RiskCircularScore(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    strokeWidth: Dp = 10.dp,
    showLabel: Boolean = true
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(score) {
        animatedProgress.animateTo(
            targetValue = (score.coerceIn(0, 100)) / 100f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    val (arcColors, riskLabel, labelColor) = when {
        score >= 61 -> Triple(
            listOf(AccentOrange, DangerPinkRed),
            "HIGH SCAM RISK",
            DangerPinkRed
        )
        score >= 31 -> Triple(
            listOf(AccentOrange, WarningAmber),
            "SUSPICIOUS",
            WarningAmber
        )
        else -> Triple(
            listOf(SuccessNeonGreen, SuccessNeonGreen),
            "SAFE MESSAGE",
            SuccessNeonGreen
        )
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(size.toPx() - strokePx, size.toPx() - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // Background Track
            drawArc(
                color = CardElevated,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Active Risk Arc with smooth gradient
            if (animatedProgress.value > 0.01f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = arcColors + arcColors.last(),
                        center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                    ),
                    startAngle = 135f,
                    sweepAngle = 270f * animatedProgress.value,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Center Score Metric
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val currentDisplayedScore = (animatedProgress.value * 100).toInt()
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$currentDisplayedScore",
                    style = RiskScoreTextStyle,
                    color = TextWhite
                )
                Text(
                    text = "/100",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMutedGray,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            if (showLabel) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = riskLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = labelColor,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}
