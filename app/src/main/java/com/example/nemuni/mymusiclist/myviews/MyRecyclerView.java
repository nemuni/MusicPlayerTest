package com.example.nemuni.mymusiclist.myviews;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author nemuni
 * @create 2018/9/29
 * @since 1.0.0
 */
public class MyRecyclerView extends RecyclerView {

    private boolean startDeepenGray = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
    }

    private View findTargetView(int x, int y) {
        ArrayList<View> touchables = getTouchables();
        View targetView = null;
        for (View child : touchables) {
            RectF rectF = getViewRectF(child);
            if (rectF.contains(x, y) && child.isClickable()) {
                targetView = child;
                break;
            }
        }
        return targetView;
    }

    private RectF getViewRectF(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getWidth();
        int bottom = top + view.getHeight();
        return new RectF(left, top, right, bottom);
    }

    public MyRecyclerView(@NonNull Context context) {
        super(context);
    }

    public MyRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
