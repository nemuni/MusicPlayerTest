package com.example.nemuni.mymusiclist.entry;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import com.example.nemuni.mymusiclist.bean.MusicMsg;
import com.example.nemuni.mymusiclist.config.Config;
import com.example.nemuni.mymusiclist.util.MusicUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author nemuni
 * @create 2018/9/29
 * @since 1.0.0
 */
public class Data {
    private static final String TAG = "Data";

    private static LruCache<String, Bitmap> bitmapCache;

    private static List<MusicMsg> localMusicMsgList;
    private static List<MusicMsg> playMusicList;
    private static RandomLinkedList<MusicMsg> randomMusicList;
    private static Random random = new Random();
    private static int curMusic;
    @Config.PlayMode
    private static int playMode = Config.PLAY_MODE_CIRCULATION;

    public static List<MusicMsg> getPlayMusicList() {
        return playMusicList;
    }

    public static void setPlayMusicList(List<MusicMsg> musicList) {
        if (playMusicList == null) {
            playMusicList = new ArrayList<>();
        }
        playMusicList.clear();
        playMusicList.addAll(musicList);
    }

    @Config.PlayMode
    public static int changePlayMode() {
        playMode = Config.getNextPlayMode(playMode);
        if (playMode == Config.PLAY_MODE_RANDOM) {
            initRandomMusicList();
        }
        return playMode;
    }

    public static MusicMsg getPlayingMusicMsg() {
        return playMusicList.get(curMusic);
    }

    public static String getPreMusicPath() {
        String path;
        switch (playMode) {
            case Config.PLAY_MODE_SINGLE:
            case Config.PLAY_MODE_CIRCULATION:
                curMusic = (curMusic+playMusicList.size()-1) % playMusicList.size();
                break;
            case Config.PLAY_MODE_RANDOM:
                curMusic = getPreRandomMusic();
                break;
        }
        path = playMusicList.get(curMusic).getPath();
        return path;
    }

    public static String getNextMusicPath() {
        String path;
        switch (playMode) {
            case Config.PLAY_MODE_SINGLE:
            case Config.PLAY_MODE_CIRCULATION:
                curMusic = (curMusic+1) % playMusicList.size();
                break;
            case Config.PLAY_MODE_RANDOM:
                curMusic = getNextRandomMusic();
                break;
        }
        path = playMusicList.get(curMusic).getPath();
        return path;
    }

    public static void removeMusicFromPlayList(int removeIndex) {
        if (randomMusicList != null) {
            randomMusicList.remove(playMusicList.get(removeIndex));
        }
        playMusicList.remove(removeIndex);
        if (removeIndex < curMusic) {
            curMusic--;
        }
    }

    public static void initRandomMusicList() {
        if (randomMusicList == null) {
            randomMusicList = new RandomLinkedList<>(playMusicList.get(curMusic));
        } else {
            randomMusicList.clear();
            randomMusicList.addFirst(playMusicList.get(curMusic));
        }
    }

    public static int getPreRandomMusic() {
        int nextIndex;
        MusicMsg m = randomMusicList.getPreMusic();
        if (m == null) {
            Log.d(TAG, "getPreRandomMusic: pre null");
            nextIndex = random.nextInt(playMusicList.size());
            while (nextIndex == curMusic) {
                nextIndex = random.nextInt(playMusicList.size());
            }
            randomMusicList.addFirst(playMusicList.get(nextIndex));
        } else {
            nextIndex = playMusicList.indexOf(m);
            Log.d(TAG, "getPreRandomMusic: pre "+ nextIndex);
        }
        return nextIndex;
    }

    public static int getNextRandomMusic() {
        int nextIndex;
        MusicMsg m = randomMusicList.getNextMusic();
        if (m == null) {
            Log.d(TAG, "getNextRandomMusic: next null");
            nextIndex = random.nextInt(playMusicList.size());
            while (nextIndex == curMusic) {
                nextIndex = random.nextInt(playMusicList.size());
            }
            randomMusicList.addList(playMusicList.get(nextIndex));
        } else {
            nextIndex = playMusicList.indexOf(m);
            Log.d(TAG, "getNextRandomMusic: next " + nextIndex);
        }
        return nextIndex;
    }

    public static String setNextMusic(int nextMusic) {
        curMusic = nextMusic;
        if (playMode == Config.PLAY_MODE_RANDOM) {
            randomMusicList.addList(playMusicList.get(nextMusic));
        }
        String path = playMusicList.get(curMusic).getPath();
        return path;
    }

    public static List<MusicMsg> getLocalMusicMsgList(Context context) {
        if (localMusicMsgList == null) {
            localMusicMsgList = MusicUtil.getMusicData(context);
        }
        return localMusicMsgList;
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

    public static int getPlayMode() {
        return playMode;
    }

    public static void setPlayMode(@Config.PlayMode int playMode) {
        Data.playMode = playMode;
    }
}
