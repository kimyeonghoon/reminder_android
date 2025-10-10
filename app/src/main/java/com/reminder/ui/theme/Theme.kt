package com.reminder.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.reminder.data.preferences.FontSize
import com.reminder.data.preferences.ThemePreset

// Purple Theme (Default)
private val PurpleDarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val PurpleLightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// Blue Theme
private val BlueDarkColorScheme = darkColorScheme(
    primary = Blue80,
    secondary = BlueGrey80,
    tertiary = LightBlue80
)

private val BlueLightColorScheme = lightColorScheme(
    primary = Blue40,
    secondary = BlueGrey40,
    tertiary = LightBlue40
)

// Green Theme
private val GreenDarkColorScheme = darkColorScheme(
    primary = Green80,
    secondary = GreenGrey80,
    tertiary = LightGreen80
)

private val GreenLightColorScheme = lightColorScheme(
    primary = Green40,
    secondary = GreenGrey40,
    tertiary = LightGreen40
)

// Pink Theme
private val PinkDarkColorScheme = darkColorScheme(
    primary = Pink80New,
    secondary = PinkGrey80,
    tertiary = LightPink80
)

private val PinkLightColorScheme = lightColorScheme(
    primary = Pink40New,
    secondary = PinkGrey40,
    tertiary = LightPink40
)

// v1.31.0: Orange Theme
private val OrangeDarkColorScheme = darkColorScheme(
    primary = Orange80,
    secondary = OrangeGrey80,
    tertiary = LightOrange80
)

private val OrangeLightColorScheme = lightColorScheme(
    primary = Orange40,
    secondary = OrangeGrey40,
    tertiary = LightOrange40
)

// v1.31.0: Red Theme
private val RedDarkColorScheme = darkColorScheme(
    primary = Red80,
    secondary = RedGrey80,
    tertiary = LightRed80
)

private val RedLightColorScheme = lightColorScheme(
    primary = Red40,
    secondary = RedGrey40,
    tertiary = LightRed40
)

// v1.31.0: Teal Theme
private val TealDarkColorScheme = darkColorScheme(
    primary = Teal80,
    secondary = TealGrey80,
    tertiary = LightTeal80
)

private val TealLightColorScheme = lightColorScheme(
    primary = Teal40,
    secondary = TealGrey40,
    tertiary = LightTeal40
)

// v1.31.0: Amber Theme
private val AmberDarkColorScheme = darkColorScheme(
    primary = Amber80,
    secondary = AmberGrey80,
    tertiary = LightAmber80
)

private val AmberLightColorScheme = lightColorScheme(
    primary = Amber40,
    secondary = AmberGrey40,
    tertiary = LightAmber40
)

// v1.31.0: Indigo Theme
private val IndigoDarkColorScheme = darkColorScheme(
    primary = Indigo80,
    secondary = IndigoGrey80,
    tertiary = LightIndigo80
)

private val IndigoLightColorScheme = lightColorScheme(
    primary = Indigo40,
    secondary = IndigoGrey40,
    tertiary = LightIndigo40
)

// v1.31.0: Brown Theme
private val BrownDarkColorScheme = darkColorScheme(
    primary = Brown80,
    secondary = BrownGrey80,
    tertiary = LightBrown80
)

private val BrownLightColorScheme = lightColorScheme(
    primary = Brown40,
    secondary = BrownGrey40,
    tertiary = LightBrown40
)

// High Contrast Color Schemes
private val HighContrastDarkColorScheme = darkColorScheme(
    primary = HighContrastPrimary,
    onPrimary = HighContrastLight,
    secondary = HighContrastSecondary,
    onSecondary = HighContrastDark,
    tertiary = HighContrastSecondary,
    background = HighContrastDark,
    onBackground = HighContrastLight,
    surface = HighContrastDark,
    onSurface = HighContrastLight,
    error = HighContrastError,
    onError = HighContrastLight
)

private val HighContrastLightColorScheme = lightColorScheme(
    primary = HighContrastPrimary,
    onPrimary = HighContrastLight,
    secondary = HighContrastSecondary,
    onSecondary = HighContrastDark,
    tertiary = HighContrastSecondary,
    background = HighContrastLight,
    onBackground = HighContrastDark,
    surface = HighContrastLight,
    onSurface = HighContrastDark,
    error = HighContrastError,
    onError = HighContrastLight
)

@Composable
fun ReminderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themePreset: ThemePreset = ThemePreset.PURPLE,
    dynamicColor: Boolean = true,
    highContrastMode: Boolean = false,
    fontSize: FontSize = FontSize.NORMAL,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // 고대비 모드가 최우선
        highContrastMode -> {
            if (darkTheme) HighContrastDarkColorScheme else HighContrastLightColorScheme
        }

        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> when (themePreset) {
            ThemePreset.PURPLE -> PurpleDarkColorScheme
            ThemePreset.BLUE -> BlueDarkColorScheme
            ThemePreset.GREEN -> GreenDarkColorScheme
            ThemePreset.PINK -> PinkDarkColorScheme
            ThemePreset.ORANGE -> OrangeDarkColorScheme
            ThemePreset.RED -> RedDarkColorScheme
            ThemePreset.TEAL -> TealDarkColorScheme
            ThemePreset.AMBER -> AmberDarkColorScheme
            ThemePreset.INDIGO -> IndigoDarkColorScheme
            ThemePreset.BROWN -> BrownDarkColorScheme
        }

        else -> when (themePreset) {
            ThemePreset.PURPLE -> PurpleLightColorScheme
            ThemePreset.BLUE -> BlueLightColorScheme
            ThemePreset.GREEN -> GreenLightColorScheme
            ThemePreset.PINK -> PinkLightColorScheme
            ThemePreset.ORANGE -> OrangeLightColorScheme
            ThemePreset.RED -> RedLightColorScheme
            ThemePreset.TEAL -> TealLightColorScheme
            ThemePreset.AMBER -> AmberLightColorScheme
            ThemePreset.INDIGO -> IndigoLightColorScheme
            ThemePreset.BROWN -> BrownLightColorScheme
        }
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypography(fontSize),
        content = content
    )
}
