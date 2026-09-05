package com.internshield.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.internshield.app.ui.theme.CardBorder
import com.internshield.app.ui.theme.CardBorderSubtle
import com.internshield.app.ui.theme.CardElevated
import com.internshield.app.ui.theme.CardSubtle
import com.internshield.app.ui.theme.CardSurface

/**
 * Premium Glassmorphism Card Surface Tile.
 *
 * Dark futuristic surface (#151820) with subtle 1dp border (#292D38) and soft inner highlight.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    backgroundColor: Color = CardSurface,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    accentGlow: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    val cardModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .clickable(onClick = onClick)
    } else {
        modifier.clip(shape)
    }

    val borderStroke = when {
        accentGlow != null -> BorderStroke(
            borderWidth,
            Brush.verticalGradient(
                colors = listOf(
                    accentGlow.copy(alpha = 0.45f),
                    accentGlow.copy(alpha = 0.12f)
                )
            )
        )
        borderColor != null -> BorderStroke(borderWidth, borderColor)
        else -> BorderStroke(
            borderWidth,
            Brush.verticalGradient(
                colors = listOf(
                    CardBorder,
                    CardBorderSubtle
                )
            )
        )
    }

    Surface(
        modifier = cardModifier,
        shape = shape,
        color = Color.Transparent,
        border = borderStroke
    ) {
        Box(
            modifier = Modifier.background(
                if (accentGlow != null) {
                    Brush.verticalGradient(
                        colors = listOf(
                            accentGlow.copy(alpha = 0.08f),
                            backgroundColor,
                            CardSubtle
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            CardElevated,
                            backgroundColor,
                            CardSubtle
                        )
                    )
                }
            )
        ) {
            content()
        }
    }
}

/** Backward compatibility alias */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderColor: Color = CardBorder,
    borderWidth: Dp = 1.dp,
    glowColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    AppCard(
        modifier = modifier,
        cornerRadius = cornerRadius,
        borderColor = borderColor,
        borderWidth = borderWidth,
        accentGlow = glowColor,
        onClick = onClick,
        content = content
    )
}
