package com.example.nemuni.mymusiclist.myviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author nemuni
 * @create 2018/9/29
 * @since 1.0.0
 */
public class MyView extends View {

    private int[] length = new int[120];
    private Point[] points = new Point[120];
    private boolean initPoint = false;
    private byte[] waveform = new byte[720];
    private ExecutorService executorService;
    private Paint paint = new Paint();
    private PathEffect pathEffect = new CornerPathEffect(20);
    public void init() {
        for (int i = 0; i < length.length; i++) {
            length[i] = 300;
        }
        executorService = Executors.newSingleThreadExecutor();
        paint.setPathEffect(pathEffect);
    }

    private int centerX, centerY;
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        centerX = getMeasuredWidth() / 2;
        centerY = getMeasuredHeight() / 2;
    }

    Path path = new Path();
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!initPoint) {
            return;
        }

        path.reset();
        path.moveTo(points[0].x, points[0].y);
        for (int i = 1; i < points.length; i++) {
            path.lineTo(points[i].x, points[i].y);
        }
        path.close();
        canvas.drawPath(path, paint);
    }

    public void setWaveData(byte[] waveform) {
        executorService.execute(new WaveRunnable(waveform));
    }

    private class WaveRunnable implements Runnable {
        private byte[] wave;

        public WaveRunnable(byte[] wave) {
            this.wave = wave;
        }

        @Override
        public void run() {
//            for (int i = 0; i < 360; i++) {
//                if (wave[i*2] > waveform[i*2]) {
//                    length[i] = 200 + wave[i*2];
//                } else if (wave[i*2] < waveform[i*2]) {
//                    length[i] = 200 + wave[i*2];
//                }
//                if (points[i] == null) {
//                    points[i] = new Point();
//                }
//                getPoint(points[i], i, length[i]);
//                waveform[i*2] = wave[i*2];
//            }
            for (int i = 0; i < 120; i++) {
                length[i] += (wave[i]-waveform[i]) / 5;
                if (points[i] == null) {
                    points[i] = new Point();
                }
                getPoint(points[i], i*3, length[i]);
                waveform[i] = wave[i];
            }
            initPoint = true;
            postInvalidate();
        }


    }

    private void getPoint(Point point, double angle, int length) {
        double radian = Math.toRadians(angle);
        if (length < 300) {
            length = 300;
        }
        point.x = (int)(centerX + Math.cos(radian)*length);
        point.y = (int)(centerY + Math.sin(radian)*length);
    }

    public MyView(Context context) {
        super(context);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
