package com.example.nemuni.mymusiclist.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.Type;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author nemuni
 * @create 2018/9/29
 * @since 1.0.0
 */
public class RenderScriptGaussianBlur {

    private RenderScript renderScript;

    public RenderScriptGaussianBlur(Context context) {
        this.renderScript = RenderScript.create(context);
    }

    public Bitmap blur(Bitmap bitmapOrigin, float radius) {
        Bitmap bmp = Bitmap.createBitmap(bitmapOrigin);

        final Allocation input = Allocation.createFromBitmap(renderScript, bmp);
        Type type = input.getType();
        final Allocation output = Allocation.createTyped(renderScript, type);

        final ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        if (radius > 25) {
            radius = 25;
        } else if (radius <= 0) {
            radius = 1;
        }
        scriptIntrinsicBlur.setRadius(radius);
        scriptIntrinsicBlur.setInput(input);
        scriptIntrinsicBlur.forEach(output);

        output.copyTo(bmp);

        input.destroy();
        output.destroy();
        scriptIntrinsicBlur.destroy();
//        type.destroy();
        return bmp;
    }

    public void destory() {
        this.renderScript.destroy();
    }
}
