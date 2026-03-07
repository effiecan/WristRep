package com.fitnessrepcounter.wear.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Typography

val FitnessTypography = Typography(
    display1 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 46.sp,
        letterSpacing = (-0.5).sp,
    ),
    display2 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        letterSpacing = 0.sp,
    ),
    title1 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
    ),
    body1 = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
    ),
    body2 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
    ),
)
