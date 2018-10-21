package com.example.nemuni.mymusiclist.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.util.Log;
import android.util.LruCache;

import com.example.nemuni.mymusiclist.entry.Data;
import com.example.nemuni.mymusiclist.bean.MusicMsg;

import java.util.ArrayList;
import java.util.List;

public class MusicUtil {

    public static List<MusicMsg> getMusicData(Context context) {
        List<MusicMsg> list = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                null, null, MediaStore.Audio.AudioColumns.IS_MUSIC);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                MusicMsg music = new MusicMsg();
                music.setMusic(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
                music.setSinger(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
                music.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
                music.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
                music.setSize(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)));
                if (music.getSize() > 1000 * 800) {
                    list.add(music);
                }
            }
            cursor.close();
        }
        return list;
    }

    public static Bitmap getFixMusicCover(String name, String path, int px) {
        LruCache<String, Bitmap> cache = Data.getBitmapCache();
        Bitmap bitmap;
        if ((bitmap = getSmallPic(cache, name)) != null) {
            return bitmap;
        } else if ((bitmap = getOriginPic(cache, name)) != null) {
            Bitmap result = DecodeBitmapUtil.zoomPic(bitmap, px, px);
            putSmallPic(cache, name, result);
            return result;
        }
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();
        if (picture != null) {
            bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
            Log.d("MusicUtil", "getFixMusicCover: " + bitmap.getWidth() + " " + bitmap.getHeight());
            putOriginPic(cache, name, bitmap);
            Bitmap result = DecodeBitmapUtil.zoomPic(bitmap, px, px);
            putSmallPic(cache, name, result);
            return result;
        }
        return null;
    }

    public static Bitmap getOriginMusicCover(String name, String path) {
        LruCache<String, Bitmap> cache = Data.getBitmapCache();
        Bitmap bitmap;
        if ((bitmap = getOriginPic(cache, name)) != null) {
            return bitmap;
        }
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();
        if (picture != null) {
            bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
            Log.d("MusicUtil", "getOriginMusicCover" + bitmap.getWidth() + " " + bitmap.getHeight());
            putOriginPic(cache, name, bitmap);
            return bitmap;
        }
        return null;
    }

    private static Bitmap getOriginPic(LruCache<String, Bitmap> cache, String name) {
        return cache.get(name + "_origin");
    }

    private static void putOriginPic(LruCache<String, Bitmap> cache, String name, Bitmap bitmap) {
        cache.put(name + "_origin", bitmap);
    }

    private static Bitmap getSmallPic(LruCache<String, Bitmap> cache, String name) {
        return cache.get(name + "_small");
    }

    private static void putSmallPic(LruCache<String, Bitmap> cache, String name, Bitmap bitmap) {
        cache.put(name + "_small", bitmap);
    }
}
