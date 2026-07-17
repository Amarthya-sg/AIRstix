package io.github.amarthyasg.airstix.data

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import io.github.amarthyasg.airstix.R

enum class MinimalistPalette(
    @param:StringRes val nameRes: Int,
    val background: Color,
    val surface: Color,
    val muted: Color,
    val text: Color,
    val accent: Color
) {
    CRIMSON_ARCADE(
        nameRes = R.string.palette_crimson_arcade,
        background = Color(0xFF120808),
        surface = Color(0xFF2B1717),
        muted = Color(0xFF854747),
        text = Color(0xFFFAFAFA),
        accent = Color(0xFFEA3E3E)
    ),
    MOLTEN_AMBER(
        nameRes = R.string.palette_molten_amber,
        background = Color(0xFF120D08),
        surface = Color(0xFF2B2117),
        muted = Color(0xFF856647),
        text = Color(0xFFFAFAFA),
        accent = Color(0xFFEA943E)
    ),
    GOLDEN_CIRCUIT(
        nameRes = R.string.palette_golden_circuit,
        background = Color(0xFF121208),
        surface = Color(0xFF2B2B17),
        muted = Color(0xFF858547),
        text = Color(0xFFFAFAFA),
        accent = Color(0xFFEAEA3E)
    ),
    TERMINAL_PHOSPHOR(
        nameRes = R.string.palette_terminal_phosphor,
        background = Color(0xFF08120D),
        surface = Color(0xFF172B21),
        muted = Color(0xFF478566),
        text = Color(0xFFFAFAFA),
        accent = Color(0xFF3EEA94)
    ),
    GLACIER_CYAN(
        nameRes = R.string.palette_glacier_cyan,
        background = Color(0xFF081012),
        surface = Color(0xFF17282B),
        muted = Color(0xFF477A85),
        text = Color(0xFFFAFAFA),
        accent = Color(0xFF3ECDEA)
    ),
    SLATE_BLUE(
        nameRes = R.string.palette_slate_blue,
        background = Color(0xFF080A12),
        surface = Color(0xFF171C2B),
        muted = Color(0xFF475785),
        text = Color(0xFFFAFAFA),
        accent = Color(0xFF3E69EA)
    ),
    DEEP_VIOLET(
        nameRes = R.string.palette_deep_violet,
        background = Color(0xFF0B0812),
        surface = Color(0xFF1E172B),
        muted = Color(0xFF5C4785),
        text = Color(0xFFFAFAFA),
        accent = Color(0xFF773EEA)
    ),
    NEON_ORCHID(
        nameRes = R.string.palette_neon_orchid,
        background = Color(0xFF110812),
        surface = Color(0xFF29172B),
        muted = Color(0xFF7F4785),
        text = Color(0xFFFAFAFA),
        accent = Color(0xFFDB3EEA)
    ),
    BUBBLEGUM_PULSE(
        nameRes = R.string.palette_bubblegum_pulse,
        background = Color(0xFF12080D),
        surface = Color(0xFF2B1721),
        muted = Color(0xFF854766),
        text = Color(0xFFFAFAFA),
        accent = Color(0xFFEA3E94)
    ),
    MONO_STEEL(
        nameRes = R.string.palette_mono_steel,
        background = Color(0xFF0A0A0A),
        surface = Color(0xFF242424),
        muted = Color(0xFF6B6B6B),
        text = Color(0xFFFAFAFA),
        accent = Color(0xFFE0E0E0)
    );

    companion object {
        fun fromInt(i: Int): MinimalistPalette {
            return when (i) {
                0 -> CRIMSON_ARCADE
                1 -> MOLTEN_AMBER
                2 -> GOLDEN_CIRCUIT
                3 -> TERMINAL_PHOSPHOR
                4 -> GLACIER_CYAN
                5 -> SLATE_BLUE
                6 -> DEEP_VIOLET
                7 -> NEON_ORCHID
                8 -> BUBBLEGUM_PULSE
                9 -> MONO_STEEL
                else -> CRIMSON_ARCADE
            }
        }
    }
}
