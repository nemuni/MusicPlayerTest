package com.example.nemuni.mymusiclist.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class DecodeBitmapUtil {

    public static Bitmap zoomImg(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inTargetDensity = options.inDensity;
        Bitmap bm = BitmapFactory.decodeResource(res, resId, options);
        final int height = bm.getHeight(), width = bm.getWidth();
        Matrix matrix = new Matrix();
        float scaleWidth = (float)reqWidth / width, scaleHeight = (float)reqHeight / height;
        float scale = scaleWidth > scaleHeight ? scaleWidth : scaleHeight;
        matrix.postScale(scale, scale);
        Bitmap result = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        bm.recycle();
        return result;
    }

    //压缩10倍资源图片
    public static Bitmap compressImg(Resources res, int resId) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inTargetDensity = options.inDensity;
        Bitmap bm = BitmapFactory.decodeResource(res, resId, options);
        final int height = bm.getHeight(), width = bm.getWidth();
        Matrix matrix = new Matrix();
        matrix.postScale(0.1f, 0.1f);
        Bitmap result = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        bm.recycle();
        return result;
    }

    public static Bitmap zoomPic(Bitmap bitmap, int reqWidth, int reqHeight) {
        int height = bitmap.getHeight(), width = bitmap.getWidth();
        Matrix matrix = new Matrix();
        float scaleWidth = (float)reqWidth / width, scaleHeight = (float)reqHeight / height;
        float scale = scaleWidth > scaleHeight ? scaleWidth : scaleHeight;
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    public static Bitmap zoomSquarePic(Bitmap bitmap, int reqRadius) {
        int height = bitmap.getHeight(), width = bitmap.getWidth();
        int radius = width < height ? width : height;
        radius /= 2;
        Matrix matrix = new Matrix();
        float scaleWidth = (float)reqRadius / width, scaleHeight = (float)reqRadius / height;
        float scale = scaleWidth > scaleHeight ? scaleWidth : scaleHeight;
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, width/2-radius, height/2-radius, width, height, matrix, true);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888: Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
