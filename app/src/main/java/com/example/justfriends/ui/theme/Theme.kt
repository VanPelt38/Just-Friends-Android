package com.example.justfriends.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.justfriends.R
import androidx.compose.material3.Typography


private val DarkColorScheme = darkColorScheme(
    primary = Color(0x13008E),
    secondary = Color(red = 255, green = 204, blue = 204, alpha = 255),
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Color(red = 19, green = 0, blue = 142),
    secondary = Color(red = 255, green = 204, blue = 204, alpha = 255)

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

private val GillSans = FontFamily(
    Font(R.font.gill_sans)
)


private val MyTypography = Typography(
   headlineSmall = TextStyle(
       fontFamily = GillSans
   ),
    titleLarge = TextStyle(
        fontFamily = GillSans
    ),
    bodyMedium = TextStyle(
        fontFamily = GillSans
    ),
    bodyLarge = TextStyle(
        fontFamily = GillSans
    )
)

@Composable
fun JustFriendsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = MyTypography,
        content = content
    )
}