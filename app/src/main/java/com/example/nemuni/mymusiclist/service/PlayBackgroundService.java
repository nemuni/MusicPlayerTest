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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;


import com.example.nemuni.mymusiclist.MainActivity;
import com.example.nemuni.mymusiclist.R;
import com.example.nemuni.mymusiclist.bean.MusicMsg;
import com.example.nemuni.mymusiclist.config.Config;
import com.example.nemuni.mymusiclist.entry.Data;
import com.example.nemuni.mymusiclist.receiver.MusicChangedReceiver;
import com.example.nemuni.mymusiclist.util.MusicUtil;

import java.lang.ref.WeakReference;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author nemuni
 * @create 2018/9/29
 * @since 1.0.0
 */
public class PlayBackgroundService extends Service {
    private static final int CONTENT_PENDINGINTENT_REQUESTCODE = 1023;
    private static final int NEXT_PENDINGINTENT_REQUESTCODE = 1024;
    private static final int PAUSE_PENDINGINTENT_REQUESTCODE = 1025;
    private static final int STOP_PENDINGINTENT_REQUESTCODE = 1026;
    private static final int NOTIFICATION_PENDINGINTENT_ID = android.os.Process.myPid();

    private MediaPlayer mediaPlayer;
//    @Config.PlayMode
//    private int playMode = Config.PLAY_MODE_CIRCULATION;

    private Binder mBinder;
    private LocalBroadcastManager localBroadcastManager;
    private MusicChangedReceiver receiver;
    private RemoteViews views;
    private NotificationManager notificationManager;
    private Notification.Builder builder;
    private boolean isPause = false;
    private boolean musicChenged = false;
    private boolean hasStartForeground = false;
    private HandlerThread mThread;
    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("service", "onCreate");
        initMediaPlayer();
        mBinder = new PlayBackgroundServiceBinder(this);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        mThread = new HandlerThread("PlayBackgroundThread");
        mThread.start();
        mHandler = new PlayBackgroundServiceHandler(this, mThread.getLooper());
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                sendChangedMusicBroadcast();
                start();
            }
        });
        // 设置设备进入锁状态模式-可在后台播放或者缓冲音乐-CPU一直工作
        //mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        // 处理音频焦点-处理多个程序会来竞争音频输出设备
        // 处理AUDIO_BECOMING_NOISY意图
        // 设置播放错误监听
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return true;
            }
        });
        // 设置播放完成监听
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                log("onCompletion");
                switch (Data.getPlayMode()) {
                    case Config.PLAY_MODE_CIRCULATION:
                    case Config.PLAY_MODE_RANDOM:
                        skipNext();
                        break;
                    case Config.PLAY_MODE_SINGLE:
                        mp.start();
                        break;
                }
            }
        });
    }

    private void start() {
        log("start");
        isPause = false;
        mediaPlayer.start();
        sendStateChangedBroadcast();
        updateNotification();
    }

    private void pause() {
        log("pause");
        isPause = true;
        mediaPlayer.pause();
        sendStateChangedBroadcast();
        updateNotification();
    }

    public void pauseOrPlay() {
        log("pauseOrPlay");
        if (mediaPlayer.isPlaying() && !isPause) {
            pause();
        } else if (isPause) {
            start();
        }
    }

    public void skipPre() {
        log("skipPre");
        String path = Data.getPreMusicPath();
        switchMusic(path);
    }

    public void skipNext() {
        log("skipNext");
        String path = Data.getNextMusicPath();
        switchMusic(path);
    }

    public void stop() {
        isPause = true;
        mediaPlayer.pause();
        sendStateChangedBroadcast();
        mediaPlayer.seekTo(0);
        stopForeground(true);
        hasStartForeground = false;
//        notificationManager.cancel(NOTIFICATION_PENDINGINTENT_ID);
    }

    public void changeMusic(int nextIndex) {
        log("changeMusic");
        String path = Data.setNextMusic(nextIndex);
        switchMusic(path);
    }

    private void switchMusic(String path) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            musicChenged = true;
//            updateNotification();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean getIsPause() {
        return isPause;
    }

    private int getDuration() {
        int duration = mediaPlayer.getDuration();
        return duration /= 1000;
    }

    private int getCurrentPosition() {
        int curPosition = mediaPlayer.getCurrentPosition();
        return curPosition /= 1000;
    }

    private void setcurPosition(int seconds) {
        mediaPlayer.seekTo(seconds*1000);
    }

    private void sendChangedMusicBroadcast() {
        Intent intent = new Intent(MusicChangedReceiver.Action_Changed_Music);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void sendStateChangedBroadcast() {
        Intent intent = new Intent(MusicChangedReceiver.Action_Changed_State);
        intent.putExtra(MusicChangedReceiver.Intent_State,
                isPause ? MusicChangedReceiver.Intent_State_Pause : MusicChangedReceiver.Intent_State_Play);
        localBroadcastManager.sendBroadcast(intent);
    }


    private void updateNotification() {
        if (null == views) {
            initNotification();
        }
        if (musicChenged) {
            MusicMsg music = Data.getPlayingMusicMsg();
            views.setTextViewText(R.id.tv_playmusic, music.getMusic());
            views.setTextViewText(R.id.tv_playsinger, music.getSinger());
            int px = (int)(getResources().getDisplayMetrics().density * 60 + 0.5f);
            Bitmap cover = MusicUtil.getFixMusicCover(music.getMusic(), music.getPath(), px);
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
//        startForeground(NOTIFICATION_PENDINGINTENT_ID, builder.build());
//        hasStartForeground = true;
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
                            Message.obtain(mHandler, SERVICEMSG_PLAYORPAUSE).sendToTarget();
                            break;
                        case Action_Notification_SkipNext :
                            Message.obtain(mHandler, SERVICEMSG_SKIPNEXT).sendToTarget();
                            break;
                        case Action_Notification_Stop :
                            Message.obtain(mHandler, SERVICEMSG_STOP).sendToTarget();
                            break;
                        default:
                    }
                }
            }
        };
        registerReceiver(receiver, intentFilter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public static final int SERIVCEMSG_CHANGEMUSIC = 1;
    public static final int SERVICEMSG_SKIPPRE = 2;
    public static final int SERVICEMSG_PLAYORPAUSE = 3;
    public static final int SERVICEMSG_SKIPNEXT = 4;
    public static final int SERVICEMSG_STOP = 5;
    public static final int SERVICEMSG_SETPOSITION = 6;
    @IntDef({SERIVCEMSG_CHANGEMUSIC, SERVICEMSG_SKIPPRE, SERVICEMSG_PLAYORPAUSE,
            SERVICEMSG_SKIPNEXT, SERVICEMSG_STOP,SERVICEMSG_SETPOSITION})
    public @interface ServiceMsg{}
    private static class PlayBackgroundServiceHandler extends Handler {
        private WeakReference<PlayBackgroundService> mWeakReference;

        PlayBackgroundServiceHandler(PlayBackgroundService service, Looper looper) {
            super(looper);
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mWeakReference != null) {
                switch (msg.what) {
                    case SERIVCEMSG_CHANGEMUSIC:
                        int nextIndex = msg.arg1;
                        mWeakReference.get().changeMusic(nextIndex);
                        break;
                    case SERVICEMSG_PLAYORPAUSE:
                        mWeakReference.get().pauseOrPlay();
                        break;
                    case SERVICEMSG_SKIPPRE:
                        mWeakReference.get().skipPre();
                        break;
                    case SERVICEMSG_SKIPNEXT:
                        mWeakReference.get().skipNext();
                        break;
                    case SERVICEMSG_STOP:
                        mWeakReference.get().stop();
                        break;
                    case SERVICEMSG_SETPOSITION:
                        int position = msg.arg1;
                        mWeakReference.get().setcurPosition(position);
                        break;
                }
            }
        }
    }

    public static class PlayBackgroundServiceBinder extends Binder {
        private WeakReference<PlayBackgroundService> mWeakReference;

        private PlayBackgroundServiceBinder(PlayBackgroundService reference) {
            this.mWeakReference = new WeakReference<>(reference);
        }

        public Handler getHandler() {
            if (mWeakReference != null) {
                mWeakReference.get().log("BinderGetHandler: " + mWeakReference.get().mHandler);
                return mWeakReference.get().mHandler;
            }
            return null;
        }

        public boolean isPause() {
            if (mWeakReference != null) {
                return mWeakReference.get().getIsPause();
            } else {
                throw new RuntimeException("lost PlayBackgroundService");
            }
        }

        public int getDuration() {
            if (mWeakReference != null) {
                return mWeakReference.get().getDuration();
            } else {
                throw new RuntimeException("lost PlayBackgroundService");
            }
        }

        public int getCurrentPosition() {
            if (mWeakReference != null) {
                return mWeakReference.get().getCurrentPosition();
            } else {
                throw new RuntimeException("lost PlayBackgroundService");
            }
        }
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

    private void log(String msg) {
        Log.d("PlayBackgroundService", msg);
    }
}
