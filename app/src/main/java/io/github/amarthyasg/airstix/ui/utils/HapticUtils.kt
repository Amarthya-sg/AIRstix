package io.github.amarthyasg.airstix.ui.utils

import android.os.Build
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import io.github.amarthyasg.airstix.data.HapticIntensity
import io.github.amarthyasg.airstix.data.defaultHapticFeedbackEnabled
import io.github.amarthyasg.airstix.data.defaultHapticIntensity

/**
 * Utility class for handling haptic feedback consistently across the app.
 * Follows Android's haptics design principles:
 * - Uses HapticFeedbackConstants for consistency across the system
 * - Avoids problematic one-shot vibrations and buzzy patterns
 * - Works on low-end devices
 */
object HapticUtils {
    private const val TAG = "HapticUtils"

    // Override flags to ensure haptic feedback is always triggered if enabled
    private const val OVERRIDE_FLAGS = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING or
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING

    // Static property to control haptic feedback globally
    var isEnabled: Boolean = defaultHapticFeedbackEnabled
        set(value) {
            field = value
            Log.d(TAG, "Haptic feedback enabled: $value")
        }

    // Static property to control haptic intensity globally
    var intensity: HapticIntensity = defaultHapticIntensity
        set(value) {
            field = value
            Log.d(TAG, "Haptic intensity set to: $value")
        }

    /**
     * Provides haptic feedback for gamepad button press events (buttons down).
     */
    fun performButtonPressFeedback(view: View) {
        if (!isEnabled) return
        try {
            val constant = when (intensity) {
                HapticIntensity.SOFT -> {
                    HapticFeedbackConstants.KEYBOARD_TAP
                }
                HapticIntensity.MEDIUM -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        HapticFeedbackConstants.KEYBOARD_PRESS
                    } else {
                        HapticFeedbackConstants.VIRTUAL_KEY
                    }
                }
                HapticIntensity.STRONG -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        HapticFeedbackConstants.CONFIRM
                    } else {
                        HapticFeedbackConstants.LONG_PRESS
                    }
                }
            }
            view.performHapticFeedback(constant, OVERRIDE_FLAGS)
        } catch (e: Exception) {
            Log.e(TAG, "Error performing button press haptic feedback: ${e.message}")
        }
    }

    /**
     * Provides haptic feedback for gamepad button release events (buttons up).
     */
    fun performButtonReleaseFeedback(view: View) {
        if (!isEnabled) return
        try {
            val constant = when (intensity) {
                HapticIntensity.SOFT -> {
                    HapticFeedbackConstants.KEYBOARD_TAP
                }
                HapticIntensity.MEDIUM -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        HapticFeedbackConstants.KEYBOARD_RELEASE
                    } else {
                        HapticFeedbackConstants.KEYBOARD_TAP
                    }
                }
                HapticIntensity.STRONG -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        HapticFeedbackConstants.CONFIRM
                    } else {
                        HapticFeedbackConstants.LONG_PRESS
                    }
                }
            }
            view.performHapticFeedback(constant, OVERRIDE_FLAGS)
        } catch (e: Exception) {
            Log.e(TAG, "Error performing button release haptic feedback: ${e.message}")
        }
    }

    /**
     * Provides haptic feedback when an analog stick gesture starts.
     * Uses GESTURE_START (API 30+) for modern devices, with fallback.
     */
    fun performGestureStartFeedback(view: View) {
        if (!isEnabled) return
        try {
            val constant = when (intensity) {
                HapticIntensity.SOFT -> {
                    HapticFeedbackConstants.KEYBOARD_TAP
                }
                HapticIntensity.MEDIUM -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        HapticFeedbackConstants.GESTURE_START
                    } else {
                        HapticFeedbackConstants.CLOCK_TICK
                    }
                }
                HapticIntensity.STRONG -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        HapticFeedbackConstants.CONFIRM
                    } else {
                        HapticFeedbackConstants.LONG_PRESS
                    }
                }
            }
            view.performHapticFeedback(constant, OVERRIDE_FLAGS)
        } catch (e: Exception) {
            Log.e(TAG, "Error performing gesture start haptic feedback: ${e.message}")
        }
    }

    /**
     * Provides haptic feedback when analog stick is released and returns to center.
     * Uses GESTURE_END (API 30+) for modern devices, with fallback.
     */
    fun performGestureEndFeedback(view: View) {
        if (!isEnabled) return
        try {
            val constant = when (intensity) {
                HapticIntensity.SOFT -> {
                    HapticFeedbackConstants.KEYBOARD_TAP
                }
                HapticIntensity.MEDIUM -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        HapticFeedbackConstants.GESTURE_END
                    } else {
                        HapticFeedbackConstants.KEYBOARD_TAP
                    }
                }
                HapticIntensity.STRONG -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        HapticFeedbackConstants.CONFIRM
                    } else {
                        HapticFeedbackConstants.LONG_PRESS
                    }
                }
            }
            view.performHapticFeedback(constant, OVERRIDE_FLAGS)
        } catch (e: Exception) {
            Log.e(TAG, "Error performing gesture end haptic feedback: ${e.message}")
        }
    }

    /**
     * Provides subtle haptic feedback for analog stick movement at discrete thresholds.
     * Only triggers when reaching significant displacement to avoid constant vibration.
     * Uses CLOCK_TICK for a very light touch sensation that doesn't overwhelm.
     *
     * @param normalizedDistance Distance from center (0-1 range)
     */
    fun performAnalogMovementFeedback(view: View, normalizedDistance: Float) {
        if (!isEnabled) return

        // Following haptics principles: correlate frequency with subtlety
        // Only provide feedback at significant movement thresholds to avoid annoyance
        when {
            normalizedDistance >= 0.95f -> {
                // At edge tick
                try {
                    val constant = when (intensity) {
                        HapticIntensity.SOFT -> {
                            HapticFeedbackConstants.KEYBOARD_TAP
                        }
                        HapticIntensity.MEDIUM -> {
                            HapticFeedbackConstants.CLOCK_TICK
                        }
                        HapticIntensity.STRONG -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                                HapticFeedbackConstants.CONFIRM
                            } else {
                                HapticFeedbackConstants.LONG_PRESS
                            }
                        }
                    }
                    view.performHapticFeedback(constant, OVERRIDE_FLAGS)
                } catch (e: Exception) {
                    Log.e(TAG, "Error performing analog movement haptic feedback: ${e.message}")
                }
            }
            // No feedback for lower movement to avoid constant buzzing
        }
    }
}
