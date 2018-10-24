package com.example.nemuni.mymusiclist.config;

import android.support.annotation.IntDef;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author nemuni
 * @create 2018/9/29
 * @since 1.0.0
 */
public class Config {

    @IntDef({PLAY_MODE_CIRCULATION, PLAY_MODE_SINGLE, PLAY_MODE_RANDOM})
    public @interface PlayMode{}
    public static final int PLAY_MODE_CIRCULATION = 0;
    public static final int PLAY_MODE_SINGLE = 1;
    public static final int PLAY_MODE_RANDOM = 2;

    private static String[] playModeDesc = new String[]{"列表循环", "单曲循环", "随机播放"};

    public static String getPlayModeDesc(@PlayMode int playMode) {
        return playModeDesc[playMode];
    }

    @PlayMode
    public static int getNextPlayMode(@PlayMode int curPlayMode) {
        return (curPlayMode+1) % 3;
    }
}
