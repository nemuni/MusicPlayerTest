package com.example.nemuni.mymusiclist.myviews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.nemuni.mymusiclist.util.DecodeBitmapUtil;
import com.example.nemuni.mymusiclist.util.MusicUtil;

public class RotateCircleBitmapView extends View {

    public float rotation = 0;

    public RotateCircleBitmapView(Context context) {
        super(context);
    }

    public RotateCircleBitmapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RotateCircleBitmapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    boolean isFirstDraw = true;
    Bitmap bitmap = null;
    boolean isBitmapFix = false;
    int left, top, centerX, centerY, width, height;
    Path path = new Path();
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Shader shader;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        centerX = width / 2;
        centerY = height / 2;
        int radius = centerX < centerY ? centerX : centerY;
        path.reset();
        path.addCircle(centerX, centerY, radius, Path.Direction.CW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isFirstDraw) {
            isFirstDraw = false;
        }
        if (bitmap == null) {
            bitmap = DecodeBitmapUtil.zoomImg(getResources(), getResources().getIdentifier("girl",
                    "drawable", getContext().getPackageName()), width, height);
//            shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//            paint.setShader(shader);
            drawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
            drawable.setCircular(true);
            bitmap = DecodeBitmapUtil.drawableToBitmap(drawable);
            left = centerX - bitmap.getWidth() / 2;
            top = centerY - bitmap.getHeight() / 2;
            isBitmapFix = true;
        }
        if (!isBitmapFix) {
            bitmap = DecodeBitmapUtil.zoomSquarePic(bitmap, width);
//            shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//            paint.setShader(shader);
            drawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
            drawable.setCircular(true);
            bitmap = DecodeBitmapUtil.drawableToBitmap(drawable);
            left = centerX - bitmap.getWidth() / 2;
            top = centerY - bitmap.getHeight() / 2;
            isBitmapFix = true;
        }


        canvas.rotate(rotation, centerX, centerY);
//        canvas.drawPath(path, paint);
        canvas.drawBitmap(bitmap, 0, 0, paint);
    }

    RoundedBitmapDrawable drawable;
    public void setBitmap(@NonNull final String name,@NonNull final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                bitmap = MusicUtil.getOriginMusicCover(name, path);
                isBitmapFix = false;
            }
        }).start();
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
        invalidate();
    }
}
