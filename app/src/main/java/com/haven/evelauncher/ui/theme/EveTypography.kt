package com.haven.evelauncher.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.haven.evelauncher.R

val ManropeFamily = FontFamily(
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold)
)

object EveTypography {
    val Clock = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 110.sp, 
        letterSpacing = (-4).sp
    )

    val Greeting = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        letterSpacing = 0.sp
    )

    val OnboardingTitle = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Light,
        fontSize = 48.sp,
        letterSpacing = (-1).sp
    )

    val WidgetHeadline = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp
    )

    val WidgetPrimary = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = (-0.5).sp
    )

    val WidgetSecondary = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.5.sp, // Reduced by 10% from 15.sp
        letterSpacing = 0.sp
    )

    val AppLabel = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp, // Reduced from 12.sp
        letterSpacing = 0.sp
    )

    val Metadata = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.5.sp, // Reduced from 12.sp
        letterSpacing = 0.sp
    )
}
