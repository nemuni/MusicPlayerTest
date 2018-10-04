package com.example.nemuni.mymusiclist.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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

    public static Bitmap zoomPic(Bitmap bitmap, int reqWidth, int reqHeight) {
        int height = bitmap.getHeight(), width = bitmap.getWidth();
        Matrix matrix = new Matrix();
        float scaleWidth = (float)reqWidth / width, scaleHeight = (float)reqHeight / height;
        float scale = scaleWidth > scaleHeight ? scaleWidth : scaleHeight;
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

}
