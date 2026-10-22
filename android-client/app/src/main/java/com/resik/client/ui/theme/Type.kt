package com.resik.client.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val ResikTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, color = TextPrimary),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextPrimary),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextPrimary),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextPrimary),
    bodyLarge = TextStyle(fontSize = 16.sp, color = TextPrimary),
    bodyMedium = TextStyle(fontSize = 14.sp, color = TextPrimary),
    bodySmall = TextStyle(fontSize = 12.sp, color = TextSecondary),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BgWhite),
    labelSmall = TextStyle(fontSize = 10.sp, color = TextSecondary),
)
