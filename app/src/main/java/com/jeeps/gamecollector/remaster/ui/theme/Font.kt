package com.jeeps.gamecollector.remaster.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.jeeps.gamecollector.R

val varelaFamily = FontFamily(
    Font(R.font.varela_round),
)

private val defaultTypography = Typography()
val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = varelaFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = varelaFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = varelaFamily),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = varelaFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = varelaFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = varelaFamily),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = varelaFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = varelaFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = varelaFamily),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = varelaFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = varelaFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = varelaFamily),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = varelaFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = varelaFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = varelaFamily)
)