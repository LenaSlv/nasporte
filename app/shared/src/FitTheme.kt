package com.example.testmmp

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.testmmp.resources.*
import org.jetbrains.compose.resources.Font

internal val FitScreenBrush = Brush.radialGradient(
    colors = listOf(Color(0xFF10351B), Color(0xFF06110B), Color(0xFF020B07)),
    center = Offset(1100f, 180f), radius = 1400f,
)

@Composable
internal fun fitInter() = FontFamily(
    Font(Res.font.inter_regular, FontWeight.Normal),
    Font(Res.font.inter_medium, FontWeight.Medium),
    Font(Res.font.inter_semibold, FontWeight.SemiBold),
    Font(Res.font.inter_bold, FontWeight.Bold),
)

@Composable
internal fun fitTypography(): Typography {
    val family = fitInter()
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = family, letterSpacing = 0.sp),
        displayMedium = base.displayMedium.copy(fontFamily = family, letterSpacing = 0.sp),
        displaySmall = base.displaySmall.copy(fontFamily = family, letterSpacing = 0.sp),
        headlineLarge = base.headlineLarge.copy(fontFamily = family, letterSpacing = 0.sp),
        headlineMedium = base.headlineMedium.copy(fontFamily = family, letterSpacing = 0.sp),
        headlineSmall = base.headlineSmall.copy(fontFamily = family, letterSpacing = 0.sp),
        titleLarge = base.titleLarge.copy(fontFamily = family, letterSpacing = 0.sp),
        titleMedium = base.titleMedium.copy(fontFamily = family, letterSpacing = 0.sp),
        titleSmall = base.titleSmall.copy(fontFamily = family, letterSpacing = 0.sp),
        bodyLarge = base.bodyLarge.copy(fontFamily = family, letterSpacing = 0.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = family, letterSpacing = 0.sp),
        bodySmall = base.bodySmall.copy(fontFamily = family, letterSpacing = 0.sp),
        labelLarge = base.labelLarge.copy(fontFamily = family, letterSpacing = 0.sp),
        labelMedium = base.labelMedium.copy(fontFamily = family, letterSpacing = 0.sp),
        labelSmall = base.labelSmall.copy(fontFamily = family, letterSpacing = 0.sp),
    )
}
