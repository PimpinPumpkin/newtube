package com.newtube.mobile.ui.dialog;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * NEWTUBE(sheet-drag): the menu sheet's root. It hands a vertical drag on the sheet to
 * {@link Listener} when the drag belongs to the sheet rather than to its list: a drag that starts
 * on the sheet's header (handle and title), or a downward drag while the list is at its top. The
 * sheet then follows the finger 1:1, and the release velocity goes to {@link Listener#onRelease},
 * which decides with {@link SheetFling}. Taps, list scrolls and horizontal moves pass through.
 */
public class SheetDragFrame extends FrameLayout {
    public interface Listener {
        /** The finger moved the sheet: {@code dy} px of travel since the drag began (+ = down). */
        void onDrag(float dy);

        /** The finger let go, moving at {@code velocity} px/s (+ = down). */
        void onRelease(float velocity);
    }

    @Nullable private Listener mListener;
    @Nullable private View mSheet;
    @Nullable private View mList;
    private final int mTouchSlop;
    private final int mMaxFling;
    @Nullable private VelocityTracker mTracker;
    private float mDownX;
    private float mDownY;
    private boolean mDownOnSheet;
    private boolean mDownOnHeader;
    private boolean mDragging;
    private final Rect mRect = new Rect();

    public SheetDragFrame(@NonNull Context context) {
        this(context, null);
    }

    public SheetDragFrame(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        ViewConfiguration config = ViewConfiguration.get(context);
        mTouchSlop = config.getScaledTouchSlop();
        mMaxFling = config.getScaledMaximumFlingVelocity();
    }

    /** Turns the drag on for {@code sheet}, whose {@code list} keeps its own scrolling. Null turns it off. */
    public void setSheet(@Nullable View sheet, @Nullable View list, @Nullable Listener listener) {
        mSheet = sheet;
        mList = list;
        mListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mListener == null || mSheet == null) {
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                mDownOnSheet = contains(mSheet, event);
                mDownOnHeader = mDownOnSheet && (mList == null || event.getRawY() < top(mList));
                mDragging = false;
                startTracking(event);
                return false;
            case MotionEvent.ACTION_MOVE: {
                track(event);
                if (!mDownOnSheet || event.getPointerCount() > 1) {
                    return false;
                }
                float dy = event.getY() - mDownY;
                float dx = event.getX() - mDownX;
                if (Math.abs(dy) < mTouchSlop || Math.abs(dy) < Math.abs(dx)) {
                    return false;
                }
                boolean listAtTop = mList == null || !mList.canScrollVertically(-1);
                // The header drags both ways; the list hands over only a pull down from its top.
                if (mDownOnHeader || (dy > 0 && listAtTop)) {
                    mDragging = true;
                    // Start from here, not from the down point: no jump by the slop.
                    mDownY = event.getY();
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                return false;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                stopTracking();
                return false;
            default:
                return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mDragging || mListener == null) {
            return super.onTouchEvent(event);
        }
        track(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                mListener.onDrag(event.getY() - mDownY);
                return true;
            case MotionEvent.ACTION_UP: {
                float velocity = 0f;
                if (mTracker != null) {
                    mTracker.computeCurrentVelocity(1000, mMaxFling);
                    velocity = mTracker.getYVelocity();
                }
                mDragging = false;
                stopTracking();
                mListener.onRelease(velocity);
                return true;
            }
            case MotionEvent.ACTION_CANCEL:
                mDragging = false;
                stopTracking();
                mListener.onRelease(0f);
                return true;
            default:
                return true;
        }
    }

    private boolean contains(@NonNull View view, @NonNull MotionEvent event) {
        view.getGlobalVisibleRect(mRect);
        return mRect.contains((int) event.getRawX(), (int) event.getRawY());
    }

    private float top(@NonNull View view) {
        int[] at = new int[2];
        view.getLocationOnScreen(at);
        return at[1];
    }

    // Velocity is tracked in screen coordinates: the sheet moves under the finger as it is dragged,
    // and positions local to it would read that motion as the finger standing still.
    private void startTracking(@NonNull MotionEvent event) {
        stopTracking();
        mTracker = VelocityTracker.obtain();
        track(event);
    }

    private void track(@NonNull MotionEvent event) {
        if (mTracker == null) {
            return;
        }
        MotionEvent screen = MotionEvent.obtain(event);
        screen.setLocation(event.getRawX(), event.getRawY());
        mTracker.addMovement(screen);
        screen.recycle();
    }

    private void stopTracking() {
        if (mTracker != null) {
            mTracker.recycle();
            mTracker = null;
        }
    }
}
