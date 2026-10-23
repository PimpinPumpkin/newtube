package com.newtube.mobile.ui.dialog;

/**
 * NEWTUBE(sheet-drag): what letting go of a dragged menu sheet does, from where it is and how fast
 * the finger was moving. The physics of Vela's place sheet: the throw is projected along an
 * exponential decay (the coast an inertial release would travel), a quick flick commits in its
 * direction however short the drag, and otherwise the sheet goes wherever the projected landing
 * point is nearer to - closed, or back open.
 *
 * <p>Pure arithmetic, in pixels and pixels per second (+ = down), so it is unit-tested.</p>
 */
final class SheetFling {
    /** Decay friction: the coast of a throw is {@code v / (4.2 * FRICTION)}. Vela's tuned value. */
    static final float FRICTION = 1.6f;
    /** dynamicanimation's and Compose's decay constant: velocity falls as e^(-4.2 * friction * t). */
    private static final float DECAY = 4.2f;
    /** A release faster than this (dp/s) is a flick and commits in its direction. Vela's value. */
    static final float FLICK_DPS = 180f;

    private SheetFling() {
    }

    /** Where a throw from {@code offset} at {@code velocity} would coast to on its own. */
    static float projectedEnd(float offset, float velocity) {
        return offset + velocity / (DECAY * FRICTION);
    }

    /**
     * True: the sheet should close. A downward flick closes, an upward one keeps it; a gentler
     * release closes once its coast would carry it past half the sheet's height.
     */
    static boolean closes(float offset, float velocity, float sheetHeight, float density) {
        float flick = FLICK_DPS * density;
        if (velocity > flick) {
            return true;
        }
        if (velocity < -flick) {
            return false;
        }
        return projectedEnd(offset, velocity) > sheetHeight / 2f;
    }

    /**
     * True when the throw's own coast reaches the target: the sheet then rides its inertia there
     * and the target simply stops it (no spring snap). False: it would fall short, or it is moving
     * the other way, so a spring carries it the rest.
     */
    static boolean coastsTo(float offset, float velocity, float target) {
        float end = projectedEnd(offset, velocity);
        return (target - offset) * (end - offset) > 0f && Math.abs(end - offset) >= Math.abs(target - offset);
    }

    /** Upward drag past open resists: the sheet moves a fraction of the finger, like a rubber band. */
    static float rubberBand(float overshoot, float sheetHeight) {
        if (overshoot >= 0f) {
            return overshoot;
        }
        float limit = sheetHeight * 0.1f;
        float pulled = -overshoot;
        return -(limit * pulled / (pulled + limit));
    }
}
