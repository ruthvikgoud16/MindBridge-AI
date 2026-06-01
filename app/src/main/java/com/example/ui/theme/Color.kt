package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// --- Elegant Dark Design Palette ---
val ElegantBackground = Color(0xFF121212)       // pure deep black-gray background
val ElegantCardBg = Color(0xFF1C1B1F)           // normal card background
val ElegantCompanionBg = Color(0xFF2B2930)      // active chat / companion insight card background
val ElegantBorder = Color(0xFF2B2930)           // clean border line
val ElegantCompanionBorder = Color(0xFF332D41)  // deeper purple border
val ElegantPrimaryLavender = Color(0xFFD0BCFF)  // light vibrant purple accent / primary
val ElegantSecondaryPlum = Color(0xFFCCC2DC)    // soft secondary lavender text / title
val ElegantMutedText = Color(0xFFA1A1A1)        // neutral gray for helper/labels
val ElegantBodyText = Color(0xFFE1E1E1)         // clean reader body text
val ElegantLightIvory = Color(0xFFE6E1E5)       // light highlight text
val ElegantDeepPurple = Color(0xFF4F378B)       // solid primary accent colors
val ElegantSoftLavender = Color(0xFFEADDFF)     // light soft violet background/text
val ElegantDarkPurpleText = Color(0xFF21005D)   // core text on warm lavender highlights
val ElegantPlumDark = Color(0xFF381E72)         // labels on bright lavender backdrops
val BeautifulCrimson = Color(0xFFF43F5E)        // error alerts / crisis helpline Coral
val BeautifulAmber = Color(0xFFF59E0B)          // warn / stress orange states

// --- Backward Compatibility Mappings for Screens.kt ---
val DeepSlateBlue = ElegantBackground           // Maps root backgrounds to pure black-gray
val MutedNavy = ElegantCardBg                   // Maps standard cards to clean M3 dark background
val CalmingTeal = ElegantPrimaryLavender        // Maps primary highlights to elegant lavender
val CalmSage = ElegantSecondaryPlum             // Maps calm states to ElegantSecondaryPlum (#CCC2DC)
val SoothingIndigo = ElegantDeepPurple          // Solid dark indigo turns into premium plum/deep purple
val CaringIvory = ElegantBodyText               // Ivory text maps to elegant reader text
val WarningCoral = BeautifulCrimson             // Alert maps to Beautiful Crimson
val SoftOrange = BeautifulAmber                 // Stress orange maps to Amber

// --- Unresolved Visual Symbols from Screens.kt ---
val TextAltMuted = ElegantMutedText             // #A1A1A1
val HighAccentCard = ElegantCompanionBg         // #2B2930
val ElegantDarkBorder = ElegantBorder           // #2B2930
val TextAlphaMutedHelper = ElegantMutedText     // #A1A1A1
val ElegantPurpleText = ElegantPrimaryLavender  // #D0BCFF
val ElegantDarkBorderAccent = ElegantCompanionBorder // #332D41
val SoothingIndigoDark = ElegantCompanionBg     // #2B2930
val ElegantWhite = ElegantBodyText             // #E1E1E1
val TextMuted = ElegantMutedText                // #A1A1A1

// Light Mode Soothing Tones (Fallback)
val GentleLinen = Color(0xFF1C1B1F)
val ElegantCardWhite = Color(0xFF2B2930)
val QuietCharcoal = Color(0xFFE1E1E1)
val SoftLavender = Color(0xFF1C1B1F)
