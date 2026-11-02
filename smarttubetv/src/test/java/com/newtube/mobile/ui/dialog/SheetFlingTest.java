package com.newtube.mobile.ui.dialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SheetFlingTest {
    private static final float DENSITY = 2.625f; // a Pixel 9
    private static final float HEIGHT = 1200f;

    @Test
    public void aFlickDownClosesHoweverShortTheDrag() {
        assertTrue(SheetFling.closes(10f, 200f * DENSITY, HEIGHT, DENSITY));
    }

    @Test
    public void aFlickUpKeepsItOpenEvenFarDown() {
        assertFalse(SheetFling.closes(HEIGHT * 0.8f, -200f * DENSITY, HEIGHT, DENSITY));
    }

    @Test
    public void aSlowReleaseGoesToTheNearerEnd() {
        assertFalse(SheetFling.closes(HEIGHT * 0.4f, 0f, HEIGHT, DENSITY));
        assertTrue(SheetFling.closes(HEIGHT * 0.6f, 0f, HEIGHT, DENSITY));
    }

    @Test
    public void aGentleThrowCountsItsCoast() {
        // 400 px down, moving 400 px/s: the coast adds 400 / (4.2 * 1.6) ~ 60 px. Short of half.
        assertFalse(SheetFling.closes(400f, 400f, HEIGHT, DENSITY));
        // From 560 px the same coast crosses half.
        assertTrue(SheetFling.closes(560f, 400f, HEIGHT, DENSITY));
    }

    @Test
    public void ridesItsInertiaOnlyWhenTheCoastGetsThere() {
        assertTrue(SheetFling.coastsTo(600f, 6000f, HEIGHT));   // coast ~893 px, 600 to go
        assertFalse(SheetFling.coastsTo(100f, 1000f, HEIGHT));  // coast ~149 px, 1100 to go
        assertFalse(SheetFling.coastsTo(600f, -6000f, HEIGHT)); // moving away
    }

    @Test
    public void aDragFollowsTheFingerExactly() {
        // Every move counts from where the drag began, so the sheet never gets ahead of the finger.
        assertEquals(30f, SheetFling.dragOffset(0f, 30f, HEIGHT), 0.001f);
        assertEquals(90f, SheetFling.dragOffset(0f, 90f, HEIGHT), 0.001f);
        // Caught mid-slide 200 px down: from there, 1:1 both ways.
        assertEquals(350f, SheetFling.dragOffset(200f, 150f, HEIGHT), 0.001f);
        assertEquals(120f, SheetFling.dragOffset(200f, -80f, HEIGHT), 0.001f);
    }

    @Test
    public void aDragStopsAtClosed() {
        assertEquals(HEIGHT, SheetFling.dragOffset(900f, 800f, HEIGHT), 0.001f);
    }

    @Test
    public void pullingUpPastOpenResists() {
        assertEquals(50f, SheetFling.rubberBand(50f, HEIGHT), 0.001f);
        float up = SheetFling.rubberBand(-400f, HEIGHT);
        assertTrue(up < 0f && up > -120f); // never more than a tenth of the sheet
    }
}
