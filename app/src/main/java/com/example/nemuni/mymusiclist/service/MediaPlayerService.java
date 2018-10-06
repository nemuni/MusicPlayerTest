package com.example.nemuni.mymusiclist.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.nemuni.mymusiclist.MainActivity;
import com.example.nemuni.mymusiclist.R;
import com.example.nemuni.mymusiclist.entry.Data;
import com.example.nemuni.mymusiclist.entry.MusicMsg;
import com.example.nemuni.mymusiclist.receiver.MusicChangedReceiver;
import com.example.nemuni.mymusiclist.util.MusicUtil;

import java.util.List;

public class MediaPlayerService extends Service {
    private static final int CONTENT_PENDINGINTENT_REQUESTCODE = 1023;
    private static final int NEXT_PENDINGINTENT_REQUESTCODE = 1024;
    private static final int PAUSE_PENDINGINTENT_REQUESTCODE = 1025;
    private static final int STOP_PENDINGINTENT_REQUESTCODE = 1026;
    private static final int NOTIFICATION_PENDINGINTENT_ID = android.os.Process.myPid();

    private final MediaPlayerBinder mBinder = new MediaPlayerBinder();
    private List<MusicMsg> musics;
    private int curMusic = 0;

    private LocalBroadcastManager localBroadcastManager;
    private MusicChangedReceiver receiver;
    private RemoteViews views;
    private NotificationManager notificationManager;
    private Notification.Builder builder;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("service", "onCreate");
        initMediaPlayer();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    private MediaPlayer mediaPlayer;
    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                start();
            }
        });
        // 设置设备进入锁状态模式-可在后台播放或者缓冲音乐-CPU一直工作
        //mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        // 处理音频焦点-处理多个程序会来竞争音频输出设备
        // 处理AUDIO_BECOMING_NOISY意图
        // 设置播放错误监听
        // 设置播放完成监听
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                skipNext();
            }
        });
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicChangedReceiver.Action_Notification_Pause);
        intentFilter.addAction(MusicChangedReceiver.Action_Notification_SkipNext);
        intentFilter.addAction(MusicChangedReceiver.Action_Notification_Stop);
        receiver = new MusicChangedReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        case Action_Notification_Pause :
                            pauseOrPlay();
                            break;
                        case Action_Notification_SkipNext :
                            skipNext();
                            break;
                        case Action_Notification_Stop :
                            stop();
                            break;
                        default:
                    }
                }
            }
        };
        registerReceiver(receiver, intentFilter);
    }

    private boolean isPause = false;
    private boolean isStop = true;
    private boolean musicChenged = false;
    public void play() {
        log("play");
        if (isPause && !isStop) {
            start();
        } else if (musics.size() > 0){
            try {
                log("play_curMusic_" + curMusic);
                mediaPlayer.reset();
                Data.setCurMusic(curMusic);
                MusicMsg music = musics.get(curMusic);
                sendMusicMsgBroadcast(music);
                updateNotification();
                mediaPlayer.setDataSource(music.getPath());
                mediaPlayer.prepare();
                musicChenged = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void start() {
        log("start");
        isPause = false;
        isStop = false;
        sendStateChangedBroadcast();
        updateNotification();
        mediaPlayer.start();
    }

    private void pause() {
        log("pause");
        isPause = true;
        sendStateChangedBroadcast();
        updateNotification();
        mediaPlayer.pause();
    }

    public void pauseOrPlay() {
        log("pauseOrPlay");
        if (mediaPlayer.isPlaying() && !isPause) {
            pause();
        } else if (isPause) {
            play();
        }
    }

    public void skipPre() {
        log("skipPre");
        curMusic--;
        if (curMusic < 0) {
            curMusic = musics.size()-1;
        }
        isPause = false;
        play();
    }

    public void skipNext() {
        log("skipNext");
        curMusic++;
        if (curMusic >= musics.size()) {
            curMusic = 0;
        }
        isPause = false;
        play();
    }

    public void stop() {
        isPause = true;
        sendStateChangedBroadcast();
        isStop = true;
        mediaPlayer.stop();
        stopForeground(true);
        hasStartForeground = false;
//        notificationManager.cancel(NOTIFICATION_PENDINGINTENT_ID);
    }

    private void sendMusicMsgBroadcast(MusicMsg music) {
        Intent intent = new Intent(MusicChangedReceiver.Action_Changed_Music);
//        intent.putExtra(MusicChangedReceiver.Intent_Music_Music, music.getMusic());
//        intent.putExtra(MusicChangedReceiver.Intent_Music_Singer, music.getSinger());
        intent.putExtra(MusicChangedReceiver.Intent_Music_CurMusic, curMusic);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void sendStateChangedBroadcast() {
        Intent intent = new Intent(MusicChangedReceiver.Action_Changed_State);
        intent.putExtra(MusicChangedReceiver.Intent_State,
                isPause ? MusicChangedReceiver.Intent_State_Pause : MusicChangedReceiver.Intent_State_Play);
        localBroadcastManager.sendBroadcast(intent);
    }

    private boolean hasStartForeground = false;
    private void updateNotification() {
        if (null == views) {
            initNotification();
            return;
        }
        if (musicChenged) {
            MusicMsg music = musics.get(curMusic);
            views.setTextViewText(R.id.tv_playmusic, music.getMusic());
            views.setTextViewText(R.id.tv_playsinger, music.getSinger());
            int px = (int)(getResources().getDisplayMetrics().density * 60 + 0.5f);
            Bitmap cover = MusicUtil.getMusicCover(music.getMusic(), music.getPath(), px);
            log(cover.getWidth() + " " + cover.getHeight());
            if (cover == null) {
                views.setImageViewResource(R.id.iv_playcover, R.drawable.ic_library_music_green_50dp);
            } else {
                views.setImageViewBitmap(R.id.iv_playcover, cover);
            }
            musicChenged = false;
        }
        if (isPause) {
            views.setInt(R.id.btn_play_circle, "setBackgroundResource", R.drawable.ic_play_circle_outline_black_32dp);
        } else {
            views.setInt(R.id.btn_play_circle, "setBackgroundResource", R.drawable.ic_pause_circle_outline_black_40dp);
        }
        if (hasStartForeground) {
            notificationManager.notify(NOTIFICATION_PENDINGINTENT_ID, builder.build());
        } else {
            startForeground(NOTIFICATION_PENDINGINTENT_ID, builder.build());
            hasStartForeground = true;
        }
    }

    private void initNotification() {
        views = new RemoteViews(getPackageName(), R.layout.notification_music);
        views.setTextViewText(R.id.tv_playmusic, musics.get(curMusic).getMusic());
        views.setTextViewText(R.id.tv_playsinger, musics.get(curMusic).getSinger());

        Intent intentContent = new Intent(this, MainActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, CONTENT_PENDINGINTENT_REQUESTCODE,
                intentContent, PendingIntent.FLAG_CANCEL_CURRENT);
        //pause music
        Intent intentPause = new Intent(MusicChangedReceiver.Action_Notification_Pause);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, PAUSE_PENDINGINTENT_REQUESTCODE,
                intentPause, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_play_circle, pausePendingIntent);
        //next music
        Intent intentSkipNext = new Intent(MusicChangedReceiver.Action_Notification_SkipNext);
        PendingIntent skipNextPendingIntent = PendingIntent.getBroadcast(this, NEXT_PENDINGINTENT_REQUESTCODE,
                intentSkipNext, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_skip_next, skipNextPendingIntent);
        //pause music and cancel notification
        Intent intentStop = new Intent(MusicChangedReceiver.Action_Notification_Stop);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, STOP_PENDINGINTENT_REQUESTCODE,
                intentStop, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_cancel, stopPendingIntent);

        builder = new Notification.Builder(this)
                // 设置小图标
                .setSmallIcon(R.drawable.ic_stop_red_40dp)
                // 设置标题
                .setContentTitle("nemuniPlayer")
                // 设置内容
                .setContentText("content")
                .setAutoCancel(false)
                .setContentIntent(contentPendingIntent)
                .setContent(views);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "com.example.nemuni.channel";
            String CHANNEL_NAME = "Music Channel";
            NotificationChannel notificationChannel= new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(notificationChannel);
            builder.setChannelId(CHANNEL_ID);
        }

        registerReceiver();
        startForeground(NOTIFICATION_PENDINGINTENT_ID, builder.build());
        hasStartForeground = true;
    }

    public void setMusics() {
        this.musics = Data.getPlayMusicList();
    }

    public void setCurMusic(int curMusic) {
        this.curMusic = curMusic;
        isStop = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_PENDINGINTENT_ID);
            stopForeground(true);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MediaPlayerBinder extends Binder {
        public Service getService() {
            return MediaPlayerService.this;
        }
    }

    private void log(String msg) {
        Log.d("MediaPlayerService", msg);
    }
}
