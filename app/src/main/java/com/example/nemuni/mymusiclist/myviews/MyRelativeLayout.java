package com.example.nemuni.mymusiclist.myviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author nemuni
 * @create 2018/9/29
 * @since 1.0.0
 */
public class MyRelativeLayout extends RelativeLayout {

    private int centerX, centerY, radius;

    private void init(Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        radius = (int)(130 * scale + 0.5f);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setARGB(130, 255, 255, 255);
        paint.setShader(shader);
        setWillNotDraw(false);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        centerX = getMeasuredWidth() / 2;
        centerY = getMeasuredHeight() / 2;
    }

    Paint paint;
    Shader shader;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(centerX, centerY, radius, paint);
    }

    public void setColor(int vibrantColor, int mutedColor) {
//        paint.setColor(color);
        shader = new LinearGradient((float)(centerX-radius*0.7), (float)(centerY-radius*0.7),
                (float)(centerX+radius*0.7), (float)(centerX+radius*0.7), vibrantColor, mutedColor, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        invalidate();
    }

    public MyRelativeLayout(Context context) {
        super(context);
        init(context);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void log(String msg) {
        Log.d("MyRelativeLayout", msg);
    }
}
