package com.internshield.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.internshield.app.ui.theme.CyberBorder
import com.internshield.app.ui.theme.CyberSurface
import com.internshield.app.ui.theme.CyberSurfaceElevated
import com.internshield.app.ui.theme.SolarOrange
import com.internshield.app.ui.theme.SolarOrangeDark
import com.internshield.app.ui.theme.SolarOrangeLight
import com.internshield.app.ui.theme.TextMuted
import com.internshield.app.ui.theme.TextSilver
import com.internshield.app.ui.theme.TextWhite
import com.internshield.app.viewmodel.AppScreen

/**
 * Modern Magma Floating Navigation Dock (matching reference design).
 *
 * Features a sleek rounded dark dock with a raised glowing Solar Orange center scan button.
 */
@Composable
fun GlassBottomBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Main Dark Dock Capsule
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .clip(RoundedCornerShape(32.dp))
                .border(
                    1.2.dp,
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF384258), CyberBorder)
                    ),
                    RoundedCornerShape(32.dp)
                ),
            color = Color(0xF5141722)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Shield
                DockTabItem(
                    icon = Icons.Default.Home,
                    label = "Shield",
                    isSelected = currentScreen is AppScreen.Dashboard,
                    onClick = { onNavigate(AppScreen.Dashboard) }
                )

                // Placeholder Space for the Raised Center Action
                Spacer(modifier = Modifier.size(54.dp))

                // Tab 3: Activity
                DockTabItem(
                    icon = Icons.AutoMirrored.Filled.List,
                    label = "Activity",
                    isSelected = currentScreen is AppScreen.RecentDetections,
                    onClick = { onNavigate(AppScreen.RecentDetections) }
                )
            }
        }

        // ── Raised Glowing Magma Center Scan Button ───────────────────────────────
        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SolarOrangeLight,
                            SolarOrange,
                            SolarOrangeDark
                        )
                    )
                )
                .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                .clickable { onNavigate(AppScreen.Scan) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Scan",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun DockTabItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = SolarOrange
    val inactiveColor = TextMuted

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        label = "tab_icon"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) TextWhite else inactiveColor,
        label = "tab_text"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}
