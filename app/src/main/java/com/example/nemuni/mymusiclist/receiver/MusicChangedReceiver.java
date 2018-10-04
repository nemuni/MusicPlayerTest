package com.example.nemuni.mymusiclist.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author nemuni
 * @create 2018/9/29
 * @since 1.0.0
 */
public abstract class MusicChangedReceiver extends BroadcastReceiver {

    public static final String Action_Changed_Music = "com.example.nemuni.Action_Changed_Music";
    public static final String Action_Changed_State = "com.example.nemuni.Action_Changed_State";

    public static final String Action_Notification_Pause = "Action_Notification_Pause";
    public static final String Action_Notification_SkipNext = "Action_Notification_SkipNext";
    public static final String Action_Notification_Stop = "Action_Notification_Stop";

    public static final String Intent_Music_CurMusic = "Intent_Music_CurMusic";
    public static final String Intent_Music_Music = "Intent_Music_Music";
    public static final String Intent_Music_Singer = "Intent_Music_Singer";

    public static final String Intent_State = "Intent_State";
    public static final int Intent_State_Play = 0;
    public static final int Intent_State_Pause = 1;

}