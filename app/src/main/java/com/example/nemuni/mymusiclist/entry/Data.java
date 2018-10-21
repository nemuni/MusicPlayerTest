package com.example.nemuni.mymusiclist.entry;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.example.nemuni.mymusiclist.bean.MusicMsg;

import java.util.ArrayList;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author nemuni
 * @create 2018/9/29
 * @since 1.0.0
 */
public class Data {

    private static List<MusicMsg> localMusicMsgList;
    private static List<MusicMsg> playMusicList = new ArrayList<>();
    private static int curMusic;

    private static LruCache<String, Bitmap> bitmapCache;

    public static void setLocalMusicMsgList(List<MusicMsg> list) {
        localMusicMsgList = list;
    }

    public static List<MusicMsg> getLocalMusicMsgList() {
        return localMusicMsgList;
    }

    public static List<MusicMsg> getPlayMusicList() {
        return playMusicList;
    }

    public static void setPlayMusicList(List<MusicMsg> musicList) {
        playMusicList.clear();
        playMusicList.addAll(musicList);
    }

    public static MusicMsg getPlayingMusicMsg() {
        return playMusicList.get(curMusic);
    }

    public static LruCache<String, Bitmap> getBitmapCache() {
        if (bitmapCache == null) {
            int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            int cacheSize = maxMemory / 8;
            bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getRowBytes() * value.getHeight() / 1024;
                }
            };
        }
        return bitmapCache;
    }

    public static int getCurMusic() {
        return curMusic;
    }

    public static void setCurMusic(int curMusic) {
        Data.curMusic = curMusic;
    }
}
