package com.example.mydaibanapp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydaibanapp.R;

public class TaskGroupBoxDecoration extends RecyclerView.ItemDecoration {
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float radius;
    private final int horizontalInset;
    private int activeCount;
    private int completedCount;

    public TaskGroupBoxDecoration(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        radius = 8f * density;
        horizontalInset = Math.round(8f * density);
        fillPaint.setColor(ContextCompat.getColor(context, R.color.white));
        fillPaint.setStyle(Paint.Style.FILL);
        strokePaint.setColor(ContextCompat.getColor(context, R.color.search_stroke_light));
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(Math.max(1f, density));
    }

    public void setGroupCounts(int activeCount, int completedCount) {
        this.activeCount = activeCount;
        this.completedCount = completedCount;
    }

    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent,
                       @NonNull RecyclerView.State state) {
        super.onDraw(canvas, parent, state);
        int nextPosition = 0;
        if (activeCount > 0) {
            drawGroup(canvas, parent, nextPosition, nextPosition + activeCount);
            nextPosition += activeCount + 1;
        }
        if (completedCount > 0) {
            drawGroup(canvas, parent, nextPosition, nextPosition + completedCount);
        }
    }

    private void drawGroup(Canvas canvas, RecyclerView parent, int startPosition, int endPosition) {
        LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager == null) {
            return;
        }

        int top = Integer.MAX_VALUE;
        int bottom = Integer.MIN_VALUE;
        boolean hasVisibleChild = false;

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);
            if (position < startPosition || position > endPosition) {
                continue;
            }
            hasVisibleChild = true;
            top = Math.min(top, layoutManager.getDecoratedTop(child));
            bottom = Math.max(bottom, layoutManager.getDecoratedBottom(child));
        }

        if (!hasVisibleChild) {
            return;
        }

        if (findVisiblePosition(parent, startPosition) == null) {
            top = parent.getPaddingTop();
        }
        if (findVisiblePosition(parent, endPosition) == null) {
            bottom = parent.getHeight() - parent.getPaddingBottom();
        }

        rect.set(horizontalInset, top, parent.getWidth() - horizontalInset, bottom);
        canvas.drawRoundRect(rect, radius, radius, fillPaint);
        canvas.drawRoundRect(rect, radius, radius, strokePaint);
    }

    private View findVisiblePosition(RecyclerView parent, int adapterPosition) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (parent.getChildAdapterPosition(child) == adapterPosition) {
                return child;
            }
        }
        return null;
    }
}
