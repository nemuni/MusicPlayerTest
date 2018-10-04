package com.example.nemuni.mymusiclist.myviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author nemuni
 * @create 2018/9/29
 * @since 1.0.0
 */
public class MyRelativeLayout extends RelativeLayout {

    private boolean startDeepenGray = false;
    private boolean recoverGray = false;
    private boolean drawCircle = false;
    private static final int startRGB = 205;
    private static final int endRGB = 155;
    private int curRGB;
    private int curCircleRadius;
    private int endCircleRadius;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                log("ACTION_DOWN");
                startDeepenGray = true;
                recoverGray = false;
                curRGB = startRGB;
                postDelayed(deepenRunnable, 50);
                break;
            case MotionEvent.ACTION_UP:
                log("ACTION_UP");
            case MotionEvent.ACTION_CANCEL:
                log("ACTION_CANCEL");
                startDeepenGray = false;
                recoverGray = true;
                drawCircle = true;
                curCircleRadius = 0;
                endCircleRadius = getEndCircleRadius();
                postDelayed(deepenRunnable, 50);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private Paint paint = new Paint();
    @Override
    protected void onDraw(Canvas canvas) {
        log("onDraw");
        if (startDeepenGray && curRGB >= endRGB) {
            log("deepen: " + curRGB);
            paint.setARGB(255, curRGB, curRGB, curRGB);
            canvas.drawRect(getSelfRectF(), paint);
            if (curRGB == endRGB) {
                startDeepenGray = false;
            } else {
                curRGB -= 10;
                postDelayed(deepenRunnable, 50);
            }
        }
        if (recoverGray && curRGB <= 255) {
            log("recover: " + curRGB);
            paint.setARGB(255, curRGB, curRGB, curRGB);
            canvas.drawRect(getSelfRectF(), paint);
            if (curRGB == 255) {
                recoverGray = false;
            } else {
                curRGB += 40;
                if (curRGB > 255) curRGB = 255;
                postDelayed(deepenRunnable, 50);
            }
        }
        super.onDraw(canvas);
    }

    private RectF getSelfRectF() {
        return new RectF(0, 0, getWidth(), getHeight());
    }

    private int getEndCircleRadius() {
        return getWidth() / 4;
    }

    Runnable deepenRunnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    private void log(String msg) {
        Log.d("MyRelativeLayout", msg);
    }

    public MyRelativeLayout(Context context) {
        super(context);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
