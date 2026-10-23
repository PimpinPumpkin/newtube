package com.newtube.mobile.ui.playback;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SpeedStepsTest {
    @Test
    public void snapsToTheSliderGrid() {
        assertEquals(1.25f, SpeedSteps.snapSpeed(1.25f), 0f);
        assertEquals(1.35f, SpeedSteps.snapSpeed(1.33f), 0f);
        assertEquals(0.25f, SpeedSteps.snapSpeed(0.1f), 0f);
        assertEquals(4f, SpeedSteps.snapSpeed(8f), 0f);
        assertEquals(1f, SpeedSteps.snapSpeed(-1f), 0f);
    }

    @Test
    public void everyGridSpeedIsExactToTwoDecimals() {
        for (int i = 0; i <= 75; i++) {
            float speed = SpeedSteps.snapSpeed(0.25f + i * 0.05f);
            assertEquals(Math.round(speed * 100f) / 100f, speed, 0f);
        }
    }

    @Test
    public void buttonsStepOneNotch() {
        assertEquals(1.05f, SpeedSteps.stepSpeed(1f, 1), 0f);
        assertEquals(0.95f, SpeedSteps.stepSpeed(1f, -1), 0f);
        assertEquals(4f, SpeedSteps.stepSpeed(4f, 1), 0f);
        assertEquals(0.25f, SpeedSteps.stepSpeed(0.25f, -1), 0f);
    }

    @Test
    public void offGridSpeedStepsOntoTheGrid() {
        assertEquals(1.35f, SpeedSteps.stepSpeed(1.33f, 1), 0f);
        assertEquals(1.3f, SpeedSteps.stepSpeed(1.33f, -1), 0f);
    }

    @Test
    public void pitchInPercent() {
        assertEquals(100, SpeedSteps.pitchPercent(1f));
        assertEquals(100, SpeedSteps.pitchPercent(0.985f));
        assertEquals(50, SpeedSteps.pitchPercent(0.025f));
        assertEquals(200, SpeedSteps.pitchPercent(3f));
        assertEquals(105, SpeedSteps.stepPitch(100, 1));
        assertEquals(200, SpeedSteps.stepPitch(200, 1));
        assertEquals(1.25f, SpeedSteps.pitchOf(125), 0f);
    }
}
