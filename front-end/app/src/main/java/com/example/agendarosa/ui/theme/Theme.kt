package com.example.agendarosa.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = RosaBotao,
    onPrimary = Color.White,
    background = FundoGradienteInicio,
    onBackground = TextoEscuro,
    surface = CardBackground,
    onSurface = TextoEscuro,
    onSurfaceVariant = TextoMedio,
    outline = BordaInputPadrao
)

@Composable
fun AgendaRosaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}