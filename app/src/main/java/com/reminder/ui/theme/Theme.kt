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

@Composable
fun ReminderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themePreset: ThemePreset = ThemePreset.PURPLE,
    dynamicColor: Boolean = true,
    fontSize: FontSize = FontSize.NORMAL,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> when (themePreset) {
            ThemePreset.PURPLE -> PurpleDarkColorScheme
            ThemePreset.BLUE -> BlueDarkColorScheme
            ThemePreset.GREEN -> GreenDarkColorScheme
            ThemePreset.PINK -> PinkDarkColorScheme
        }

        else -> when (themePreset) {
            ThemePreset.PURPLE -> PurpleLightColorScheme
            ThemePreset.BLUE -> BlueLightColorScheme
            ThemePreset.GREEN -> GreenLightColorScheme
            ThemePreset.PINK -> PinkLightColorScheme
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
