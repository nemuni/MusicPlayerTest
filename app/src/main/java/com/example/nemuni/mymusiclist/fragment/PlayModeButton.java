package com.example.nemuni.mymusiclist.fragment;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.example.nemuni.mymusiclist.R;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author nemuni
 * @create 2018/9/29
 * @since 1.0.0
 */
public class PlayModeButton extends AppCompatTextView {

    private int density;
    private int paddingLeft;
    private Bitmap bitmap;
    private Paint paint = new Paint();

    private void init() {
        density = (int)getContext().getResources().getDisplayMetrics().density;
        paddingLeft = density * 10;
        Drawable drawable = getContext().getDrawable(R.drawable.ic_playmode_circulation_gray_45dp);
        bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        setText("列表循环");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int top = getHeight() - bitmap.getHeight();
        top /= 2;
        canvas.drawBitmap(bitmap, paddingLeft, top, paint);
    }

    public PlayModeButton(Context context) {
        super(context);
        init();
    }

    public PlayModeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayModeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
}
