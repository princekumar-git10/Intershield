package com.internshield.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Precision Cyber AI Color System (Exact User Specification) ──────────────
val BgDark = Color(0xFF08090D)                  // Primary Background (#08090D)
val BgSecondary = Color(0xFF0D0F15)             // Secondary Background (#0D0F15)
val CardSurface = Color(0xFF151820)             // Card Surface (#151820)
val CardElevated = Color(0xFF1A1D25)            // Elevated Card Surface (#1A1D25)
val CardSubtle = Color(0xFF10121A)              // Recessed Container Surface
val CardBorder = Color(0xFF292D38)              // Border (#292D38)
val CardBorderSubtle = Color(0xFF1F222B)        // Subtle inner border

// ── Accent Lighting & Signals ────────────────────────────────────────────────
val AccentOrange = Color(0xFFFF5E1E)            // Primary Accent: Bright Orange
val AccentOrangeBright = Color(0xFFFF7A00)      // Bright Highlight Orange
val AccentRedOrange = Color(0xFFFF3D00)         // Secondary Accent: Red-Orange
val AccentOrangeGlow = Color(0x33FF5E1E)        // Soft Orange Glow
val AccentOrangeBg = Color(0xFF2D1208)          // Subtle Orange Glass Tint

val SuccessNeonGreen = Color(0xFF00E599)        // Success: Neon Green
val SuccessGreenDark = Color(0xFF082D1F)        // Success Glass Tint
val SuccessGreenGlow = Color(0x3300E599)        // Green Glow

val DangerPinkRed = Color(0xFFFF2A55)           // Danger: Pink/Red
val DangerRedDark = Color(0xFF330C15)           // Danger Glass Tint
val DangerRedGlow = Color(0x33FF2A55)           // Red Glow

val WarningAmber = Color(0xFFFFB800)            // Caution Amber / Gold
val WarningAmberDark = Color(0xFF2E2006)        // Caution Glass Tint

// ── Typography ───────────────────────────────────────────────────────────────
val TextWhite = Color(0xFFFFFFFF)               // Pure White (#FFFFFF)
val TextNearWhite = Color(0xFFF1F5F9)           // Near-White Text
val TextMutedGray = Color(0xFF8E97A8)           // Secondary Text: Muted Gray
val TextDimGray = Color(0xFF5A6275)             // Dim Metadata Text

// ── Backward Compatibility Aliases ───────────────────────────────────────────
val CyberBlack = BgDark
val CyberCanvas = BgSecondary
val CyberSurface = CardSurface
val CyberSurfaceElevated = CardElevated
val CyberSurfaceSubtle = CardSubtle
val CyberBorder = CardBorder
val CyberBorderLuminous = Color(0xFF383E4E)
val CyberBorderOrange = Color(0x66FF5E1E)
val CyberBorderMint = Color(0x6600E599)

val SolarOrange = AccentOrange
val SolarOrangeLight = AccentOrangeBright
val SolarOrangeDark = AccentRedOrange
val SolarOrangeGlow = AccentOrangeGlow
val SolarOrangeBg = AccentOrangeBg
val SafeEmerald = SuccessNeonGreen
val SafeEmeraldDark = SuccessGreenDark
val SafeEmeraldGlow = SuccessGreenGlow
val AlertCrimson = DangerPinkRed
val AlertCrimsonDark = DangerRedDark
val WarningGold = WarningAmber
val CyberCyan = AccentOrangeBright
val TextSilver = TextMutedGray
val TextMuted = TextMutedGray
val TextDim = TextDimGray

val SpaceBlack = BgDark
val SpaceCanvas = BgSecondary
val SpaceSurface = CardSurface
val SpaceSurfaceElevated = CardElevated
val SpaceSurfaceSubtle = CardSubtle
val BorderSubtle = CardBorder
val BorderLuminous = Color(0xFF383E4E)
val NeonMint = SuccessNeonGreen
val NeonEmerald = SuccessNeonGreen
val NeonMintDark = SuccessGreenDark
val NeonMintGlow = SuccessGreenGlow
val NeonCrimson = DangerPinkRed
val NeonCrimsonDark = DangerRedDark
val NeonCrimsonGlow = DangerRedGlow
val NeonAmber = WarningAmber
val NeonAmberDark = WarningAmberDark
val NeonCyan = AccentOrangeBright
val TextSlate = TextMutedGray
val TextMutedSlate = TextMutedGray
val TextDimSlate = TextDimGray

val WhatsAppBg = BgDark
val WhatsAppSurface = BgSecondary
val WhatsAppCard = CardSurface
val WhatsAppCardElevated = CardElevated
val WhatsAppBorder = CardBorder
val WhatsAppBorderSubtle = CardBorderSubtle
val WhatsAppGreen = AccentOrange
val WhatsAppTeal = SuccessNeonGreen
val WhatsAppGreenDark = AccentOrangeBg
val WhatsAppGreenGlow = AccentOrangeGlow
val WhatsAppAlertRed = DangerPinkRed
val WhatsAppAlertRedMuted = DangerRedDark
val WhatsAppWarningAmber = WarningAmber
val WhatsAppWarningMuted = WarningAmberDark
val WhatsAppLinkBlue = AccentOrangeBright
val WhatsAppChatBubbleIn = CardSubtle
val WhatsAppChatBubbleOut = AccentOrangeBg
val WhatsAppTextPrimary = TextWhite
val WhatsAppTextSecondary = TextMutedGray
val WhatsAppTextMuted = TextMutedGray

val ObsidianBg = BgDark
val ObsidianCard = CardSurface
val ObsidianCardElevated = CardElevated
val ObsidianCardSubtle = CardSubtle
val BorderHairline = CardBorder
val BorderActive = CyberBorderOrange
val SignalEmerald = SuccessNeonGreen
val SignalEmeraldMuted = SuccessGreenDark
val SignalEmeraldGlow = SuccessGreenGlow
val SignalCrimson = DangerPinkRed
val SignalCrimsonMuted = DangerRedDark
val SignalCrimsonGlow = DangerRedGlow
val SignalAmber = WarningAmber
val SignalAmberMuted = WarningAmberDark
val AzureBlue = AccentOrange
val AzureSky = AccentOrangeBright
val TextPrimary = TextWhite
val TextSecondary = TextMutedGray
val BackgroundDark = BgDark
val BackgroundNavy = BgSecondary
val SurfaceDark = CardSurface
val GlassSurface = CardSurface
val GlassSurfaceHover = CardElevated
val GlassBorder = CardBorder
val GlassBorderActive = CyberBorderOrange
val ElectricViolet = AccentOrange
val ElectricIndigo = AccentRedOrange
val CyanBlue = AccentOrange
val CyanSky = AccentOrangeBright
val EmeraldGreen = SuccessNeonGreen
val SafeGreen = SuccessNeonGreen
val WarningYellow = WarningAmber
val HighRiskCoral = DangerPinkRed
val HighRiskRed = DangerPinkRed
