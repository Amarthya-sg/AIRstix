package io.github.amarthyasg.airstix.data

import androidx.annotation.StringRes
import io.github.amarthyasg.airstix.R

enum class HapticIntensity(@StringRes val nameRes: Int) {
    SOFT(R.string.haptic_intensity_soft),
    MEDIUM(R.string.haptic_intensity_medium),
    STRONG(R.string.haptic_intensity_strong);

    companion object {
        fun fromInt(i: Int): HapticIntensity {
            return when (i) {
                0 -> SOFT
                1 -> MEDIUM
                2 -> STRONG
                else -> MEDIUM
            }
        }
    }
}
