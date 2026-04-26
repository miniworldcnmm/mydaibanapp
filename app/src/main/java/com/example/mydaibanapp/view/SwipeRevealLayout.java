package com.example.mydaibanapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SwipeRevealLayout extends FrameLayout {
    private static final long ANIMATION_DURATION_MS = 160L;
    private static final float EXTRA_DRAG_DP = 88f;
    private static final float EXTRA_DRAG_RESISTANCE = 0.35f;

    private View foregroundView;
    private View revealView;
    private int touchSlop;
    private float maxExtraDrag;
    private float downX;
    private float downY;
    private float startTranslationX;
    private boolean dragging;
    private boolean dragStartNotified;
    private boolean open;
    private OnRevealStateChangeListener revealStateChangeListener;

    public interface OnRevealStateChangeListener {
        void onDragStarted(SwipeRevealLayout layout);
        void onOpened(SwipeRevealLayout layout);
        void onClosed(SwipeRevealLayout layout);
    }

    public SwipeRevealLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SwipeRevealLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SwipeRevealLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        maxExtraDrag = EXTRA_DRAG_DP * context.getResources().getDisplayMetrics().density;
        setClipChildren(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() >= 2) {
            revealView = getChildAt(0);
            foregroundView = getChildAt(1);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (foregroundView == null || getRevealWidth() == 0) {
            return super.onInterceptTouchEvent(ev);
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                startTranslationX = foregroundView.getTranslationX();
                dragging = false;
                dragStartNotified = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = ev.getX() - downX;
                float dy = ev.getY() - downY;
                if (Math.abs(dx) > touchSlop && Math.abs(dx) > Math.abs(dy)) {
                    startDragging();
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (foregroundView == null || getRevealWidth() == 0) {
            return super.onTouchEvent(event);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                startTranslationX = foregroundView.getTranslationX();
                dragging = false;
                dragStartNotified = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - downX;
                float dy = event.getY() - downY;
                if (!dragging && Math.abs(dx) > touchSlop && Math.abs(dx) > Math.abs(dy)) {
                    startDragging();
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (dragging) {
                    float nextTranslation = applyDragResistance(startTranslationX + dx);
                    foregroundView.setTranslationX(nextTranslation);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (dragging) {
                    boolean shouldOpen = foregroundView.getTranslationX() <= -getRevealWidth() / 2f;
                    if (event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                        shouldOpen = open;
                    }
                    settle(shouldOpen, true);
                    dragging = false;
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public boolean isOpen() {
        return open;
    }

    public void open(boolean animate) {
        settle(true, animate);
    }

    public void close(boolean animate) {
        settle(false, animate);
    }

    public void setOnRevealStateChangeListener(OnRevealStateChangeListener listener) {
        this.revealStateChangeListener = listener;
    }

    private void startDragging() {
        dragging = true;
        if (!dragStartNotified && revealStateChangeListener != null) {
            dragStartNotified = true;
            revealStateChangeListener.onDragStarted(this);
        }
    }

    private void settle(boolean shouldOpen, boolean animate) {
        float target = shouldOpen ? -getRevealWidth() : 0;
        if (animate) {
            foregroundView.animate()
                    .translationX(target)
                    .setDuration(ANIMATION_DURATION_MS)
                    .withEndAction(() -> updateOpenState(shouldOpen))
                    .start();
        } else {
            foregroundView.animate().cancel();
            foregroundView.setTranslationX(target);
            updateOpenState(shouldOpen);
        }
    }

    private void updateOpenState(boolean newOpenState) {
        if (open == newOpenState) {
            return;
        }
        open = newOpenState;
        if (revealStateChangeListener == null) {
            return;
        }
        if (open) {
            revealStateChangeListener.onOpened(this);
        } else {
            revealStateChangeListener.onClosed(this);
        }
    }

    private int getRevealWidth() {
        return revealView != null ? revealView.getWidth() : 0;
    }

    private float applyDragResistance(float value) {
        int revealWidth = getRevealWidth();
        if (value >= 0) {
            return 0;
        }
        if (value >= -revealWidth) {
            return value;
        }
        float extra = Math.min((-value - revealWidth) * EXTRA_DRAG_RESISTANCE, maxExtraDrag);
        return -revealWidth - extra;
    }
}
