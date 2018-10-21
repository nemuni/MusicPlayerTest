package com.example.nemuni.mymusiclist.myviews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.nemuni.mymusiclist.R;
import com.example.nemuni.mymusiclist.activity.PlayMusicActivity;
import com.example.nemuni.mymusiclist.util.BlurUtil;
import com.example.nemuni.mymusiclist.util.DecodeBitmapUtil;
import com.example.nemuni.mymusiclist.util.MusicUtil;
import com.example.nemuni.mymusiclist.util.RenderScriptGaussianBlur;

import java.lang.ref.WeakReference;
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
public class MyImageView extends AppCompatImageView {

    RenderScriptGaussianBlur blur;
    private Bitmap blurBackground;
    private ExecutorService blurExecutor = Executors.newSingleThreadExecutor();

    private SetBackgroundHandler mHandler;
    private Handler activityHandler;

    public void init(Handler activityHandler) {
        setScaleType(ScaleType.CENTER_CROP);
//        setColorFilter(Color.parseColor("#AA333333"), PorterDuff.Mode.DARKEN);
        blur = new RenderScriptGaussianBlur(getContext());
        mHandler = new SetBackgroundHandler(this);
        this.activityHandler = activityHandler;
    }

    public void destory() {
        blurExecutor.shutdownNow();
        blur.destory();
    }

    private void setBlurBackground() {
        setImageBitmap(blurBackground);
    }

    public void setBitmap(final String name, final String path) {
        blurExecutor.execute(new BlurRunnable(this, name, path));
    }

//    private Runnable blurRunnable = new Runnable() {
//        @Override
//        public void run() {
//            if (readyBackground != null) {
//                readyBackground = Bitmap.createScaledBitmap(readyBackground, readyBackground.getWidth()/10, readyBackground.getHeight()/10, true);
//            } else {
//                readyBackground = DecodeBitmapUtil.zoomImg(getResources(), getResources().getIdentifier("girl",
//                        "drawable", getContext().getPackageName()), width/10, height/10);
//            }
//            blurBackground = blur.blur(readyBackground, 15);
//            blurBackground = readyBackground;
//            blurBackground = BlurUtil.doBlur(readyBackground, 25, false);
//
//        }
//    };


    private void log(String msg) {
        Log.d("MyRelativeLayout", msg);
    }

    public MyImageView(Context context) {
        super(context);
    }

    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private Palette.PaletteAsyncListener paletteAsyncListener = new Palette.PaletteAsyncListener() {
        @Override
        public void onGenerated(@Nullable Palette palette) {
            try {
                int vibrantColor = palette.getVibrantColor(Color.WHITE);
                int a = 80;
                int r = (vibrantColor >> 16) & 0xff;
                int g = (vibrantColor >> 8) & 0xff;
                int b = vibrantColor & 0xff;
                vibrantColor = a << 24 | r << 16 | g << 8 | b;
                int mutedColor = palette.getMutedColor(Color.BLACK);
                a = 80;
                r = (mutedColor >> 16) & 0xff;
                g = (mutedColor >> 8) & 0xff;
                b = mutedColor & 0xff;
                mutedColor = a << 24 | r << 16 | g << 8 | b;
                Message message = Message.obtain();
                message.what = PlayMusicActivity.MSG_BACKGROUNDCOLOR;
                message.arg1 = vibrantColor;
                message.arg2 = mutedColor;
                activityHandler.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private static class BlurRunnable implements Runnable {
        private WeakReference<MyImageView> mWeakReference;
        private String name, path;
        private Bitmap bitmap;

        BlurRunnable(MyImageView reference, String name, String path) {
            this.mWeakReference = new WeakReference<>(reference);
            this.name = name;
            this.path = path;
        }

        @Override
        public void run() {
            if (name != null && path != null) {
                bitmap = MusicUtil.getOriginMusicCover(name, path);
            } else {
                bitmap = null;
            }
            if (bitmap != null) {
                bitmap = Bitmap.createScaledBitmap(bitmap,
                        bitmap.getWidth()/10, bitmap.getHeight()/10, true);
            } else if (mWeakReference != null){
                bitmap = DecodeBitmapUtil.compressImg(mWeakReference.get().getContext().getResources(),
                        mWeakReference.get().getContext().getResources().getIdentifier("girl", "drawable",
                        mWeakReference.get().getContext().getPackageName()));
            }
            if (mWeakReference != null) {
                Palette.from(bitmap).generate(mWeakReference.get().paletteAsyncListener);
                bitmap = mWeakReference.get().blur.blur(bitmap, 5);
                mWeakReference.get().blurBackground = bitmap;
                mWeakReference.get().mHandler.sendEmptyMessage(MSG_SETBACKGROUND);
            }
        }
    }

    private static final int MSG_SETBACKGROUND = 11;
    private static class SetBackgroundHandler extends Handler {
        private WeakReference<MyImageView> mWeakReference;

        public SetBackgroundHandler(MyImageView reference) {
            this.mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SETBACKGROUND:
                    if (mWeakReference != null) {
                        mWeakReference.get().setBlurBackground();
                    }
                    break;
            }
        }
    }
}
