package io.github.amarthyasg.airstix.data

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import io.github.amarthyasg.airstix.R
import io.github.amarthyasg.airstix.ui.theme.GlossyBlue
import io.github.amarthyasg.airstix.ui.theme.GlossyGreen
import io.github.amarthyasg.airstix.ui.theme.GlossyRed
import io.github.amarthyasg.airstix.ui.theme.NeonBlue
import io.github.amarthyasg.airstix.ui.theme.NeonGreen
import io.github.amarthyasg.airstix.ui.theme.NeonRed
import io.github.amarthyasg.airstix.ui.theme.NeonYellow
import io.github.amarthyasg.airstix.ui.theme.GlossyYellow
import io.github.amarthyasg.airstix.ui.theme.NeonPurple
import io.github.amarthyasg.airstix.ui.theme.GlossyPurple
import io.github.amarthyasg.airstix.ui.theme.NeonOrange
import io.github.amarthyasg.airstix.ui.theme.GlossyOrange
import io.github.amarthyasg.airstix.ui.theme.NeonPink
import io.github.amarthyasg.airstix.ui.theme.GlossyPink

enum class BaseColor(@StringRes val nameRes: Int) {
    RED(R.string.color_red),
    GREEN(R.string.color_green),
    BLUE(R.string.color_blue),
    YELLOW(R.string.color_yellow),
    PURPLE(R.string.color_purple),
    ORANGE(R.string.color_orange),
    PINK(R.string.color_pink);

    companion object {
        fun fromInt(i: Int): BaseColor {
            return when (i) {
                0 -> RED
                1 -> GREEN
                2 -> BLUE
                3 -> YELLOW
                4 -> PURPLE
                5 -> ORANGE
                6 -> PINK
                else -> throw IllegalArgumentException("Invalid BaseColor value")
            }
        }
    }
}

/**
 * Returns a color from a given base color name depending on the current color scheme.
 */
fun getColorFromBaseColor(baseColor: BaseColor, isDarkMode: Boolean): Color {
    return when (baseColor) {
        BaseColor.RED -> if (isDarkMode) NeonRed else GlossyRed
        BaseColor.GREEN -> if (isDarkMode) NeonGreen else GlossyGreen
        BaseColor.BLUE -> if (isDarkMode) NeonBlue else GlossyBlue
        BaseColor.YELLOW -> if (isDarkMode) NeonYellow else GlossyYellow
        BaseColor.PURPLE -> if (isDarkMode) NeonPurple else GlossyPurple
        BaseColor.ORANGE -> if (isDarkMode) NeonOrange else GlossyOrange
        BaseColor.PINK -> if (isDarkMode) NeonPink else GlossyPink
    }
}
