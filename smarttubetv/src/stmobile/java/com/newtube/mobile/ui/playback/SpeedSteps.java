package com.newtube.mobile.ui.playback;

/**
 * NEWTUBE(speed-sheet): the speed sheet's arithmetic. Its sliders only take values on their step
 * grid (Material's Slider throws on anything else), while a speed or pitch can come from anywhere
 * - the TV dialog's lists, a per-channel memory, a press-and-hold boost - so every value is
 * clamped and snapped before it reaches a slider, and every value a slider or button gives back is
 * rounded so 0.25 + 20 * 0.05 reads 1.25, not 1.2500001.
 */
final class SpeedSteps {
    static final float SPEED_MIN = 0.25f;
    static final float SPEED_MAX = 4.0f;
    static final float SPEED_STEP = 0.05f;
    static final int PITCH_MIN = 50;
    static final int PITCH_MAX = 200;
    static final int PITCH_STEP = 5;

    private SpeedSteps() {
    }

    /** {@code speed} on the slider's grid: clamped to 0.25x..4x, nearest 0.05, two decimals. */
    static float snapSpeed(float speed) {
        if (Float.isNaN(speed) || speed <= 0f) {
            return 1f;
        }
        float clamped = Math.max(SPEED_MIN, Math.min(SPEED_MAX, speed));
        int steps = Math.round((clamped - SPEED_MIN) / SPEED_STEP);
        return round2(SPEED_MIN + steps * SPEED_STEP);
    }

    /** One -/+ press from {@code speed}: the next grid speed in {@code direction} (+1 or -1). */
    static float stepSpeed(float speed, int direction) {
        float snapped = snapSpeed(speed);
        // Off-grid (1.33x from elsewhere): the first press lands on the grid speed that way.
        if (direction > 0 && snapped > speed + 1e-4f || direction < 0 && snapped < speed - 1e-4f) {
            return snapped;
        }
        return snapSpeed(snapped + direction * SPEED_STEP);
    }

    /** Pitch (1.0 = unchanged) as the slider's percent: 50..200, nearest 5. */
    static int pitchPercent(float pitch) {
        if (Float.isNaN(pitch) || pitch <= 0f) {
            return 100;
        }
        int percent = Math.round(pitch * 100f / PITCH_STEP) * PITCH_STEP;
        return Math.max(PITCH_MIN, Math.min(PITCH_MAX, percent));
    }

    /** One -/+ press from {@code percent}. */
    static int stepPitch(int percent, int direction) {
        return Math.max(PITCH_MIN, Math.min(PITCH_MAX, percent + direction * PITCH_STEP));
    }

    static float pitchOf(int percent) {
        return percent / 100f;
    }

    static float round2(float value) {
        return Math.round(value * 100f) / 100f;
    }
}
