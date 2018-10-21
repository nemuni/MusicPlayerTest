package com.example.nemuni.mymusiclist.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.nemuni.mymusiclist.R;
import com.example.nemuni.mymusiclist.entry.Data;
import com.example.nemuni.mymusiclist.bean.MusicMsg;
import com.example.nemuni.mymusiclist.fragment.PlayListFragment;
import com.example.nemuni.mymusiclist.myviews.MyImageView;
import com.example.nemuni.mymusiclist.myviews.MyRelativeLayout;
import com.example.nemuni.mymusiclist.myviews.RotateCircleBitmapView;
import com.example.nemuni.mymusiclist.receiver.MusicChangedReceiver;
import com.example.nemuni.mymusiclist.service.MediaPlayerService;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayMusicActivity extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, PlayListFragment.ChangeMusic {

    private Toolbar toolbar;
    private Button btn_tbBack;
    private TextView tv_tbMusicName;
    private TextView tv_tbMusicSinger;
    private MyImageView iv_background;
    private MyRelativeLayout relay_myrelay;
    private RotateCircleBitmapView bitmapView;
    private SeekBar seekBar;
    private TextView tv_duration;
    private TextView tv_curProgress;
    private Button btn_skipPre;
    private Button btn_playPause;
    private Button btn_skipNext;
    private Button btn_playList;

    ValueAnimator animator;
    private MediaPlayerService mediaPlayerService;
    private boolean isBindService = false;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
            mediaPlayerService = (MediaPlayerService) binder.getService();
            isBindService = true;
            isPause = mediaPlayerService.getIsPause();
            changeState();
            setSeekBarDuration();
            updateSeekBarRunnable = new UpdateSeekbarRunnable(PlayMusicActivity.this);
            seekbarExecutor.execute(updateSeekBarRunnable);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mediaPlayerService = null;
        }
    };
    private MusicChangedReceiver musicChangedReceiver;
    private LocalBroadcastManager localBroadcastManager;
    private PlayListFragment playListFragment;
    private boolean isPause = true;
    private int duration;
    private int curProgress;
    private ExecutorService seekbarExecutor = Executors.newSingleThreadExecutor();
    private PlayMusicHandler mHandler = new PlayMusicHandler(this);

    private String musicName;
    private String musicPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);
//            View decorView = getWindow().getDecorView();
//            int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION	//表示会让应用的主体内容占用系统导航栏的空间
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;		//两行必须结合在一起，表示会让应用的主体内容占用系统状态栏的空间
//            decorView.setSystemUiVisibility(option);
//            getWindow().setNavigationBarColor(Color.TRANSPARENT);	//设置透明色
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
        initView();
        setListener();
        initAnimator();
        bindService();
        registerReceiver();
        startAnimator();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
        layoutParams.height += getStatusBarHeight();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        btn_tbBack = findViewById(R.id.btn_tbback);
        tv_tbMusicName = findViewById(R.id.tv_tbmusicname);
        tv_tbMusicSinger = findViewById(R.id.tv_tbmusicsinger);
        iv_background = findViewById(R.id.iv_background);
        iv_background.init(mHandler);
        relay_myrelay = findViewById(R.id.relay_myrelay);
        bitmapView = findViewById(R.id.myview_rotateCircle);
        seekBar = findViewById(R.id.seekbar);
        tv_curProgress = findViewById(R.id.tv_curprogress);
        tv_duration = findViewById(R.id.tv_duration);
        btn_skipPre = findViewById(R.id.btn_skip_pre);
        btn_playPause = findViewById(R.id.btn_play_pause);
        btn_skipNext = findViewById(R.id.btn_skip_next);
        btn_playList = findViewById(R.id.btn_playlist);
    }

    private void setListener() {
        btn_tbBack.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        btn_skipPre.setOnClickListener(this);
        btn_playPause.setOnClickListener(this);
        btn_skipNext.setOnClickListener(this);
        btn_playList.setOnClickListener(this);
    }

    private void bindService() {
        if (!isBindService) {
            Log.d("MainActivity", "bindService");
            Intent intent = new Intent(this, MediaPlayerService.class);
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        }
    }

    private void registerReceiver() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicChangedReceiver.Action_Changed_Music);
        intentFilter.addAction(MusicChangedReceiver.Action_Changed_State);
        musicChangedReceiver = new MusicChangedReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        case MusicChangedReceiver.Action_Changed_Music:
                            setSeekBarDuration();
                            animator.cancel();
                            startAnimator();
//                            if (playListFragment != null) {
//                                playListFragment.refreshCurMusic();
//                            }
                            break;
                        case MusicChangedReceiver.Action_Changed_State:
                            isPause = intent.getIntExtra(MusicChangedReceiver.Intent_State,
                                    MusicChangedReceiver.Intent_State_Pause) == MusicChangedReceiver.Intent_State_Pause;
                            changeState();
                            break;
                    }
                }
            }
        };
        localBroadcastManager.registerReceiver(musicChangedReceiver, intentFilter);
    }

    private void initAnimator() {
        animator = ObjectAnimator.ofFloat(bitmapView, "rotation", 0, 360);
        animator.setDuration(20000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatMode(ValueAnimator.RESTART);
    }

    private void startAnimator() {
        MusicMsg musicMsg = Data.getPlayingMusicMsg();
        tv_tbMusicName.setText(musicMsg.getMusic());
        tv_tbMusicSinger.setText(musicMsg.getSinger());
        musicName = musicMsg.getMusic();
        musicPath = musicMsg.getPath();
        bitmapView.setBitmap(musicName, musicPath);
        iv_background.setBitmap(musicName, musicPath);
        animator.start();
    }

    private void setSeekBarDuration() {
        duration = mediaPlayerService.getDuration();
        seekBar.setMax(duration);
        seekBar.setSecondaryProgress(duration);
        tv_duration.setText(String.format(getResources().getString(R.string.tv_duration), duration/60, duration%60));
    }

    private void updateSeekBarProgress() {
        if (!isPause && !isSeeking) {
            curProgress = mediaPlayerService.getCurrentPosition();
            seekBar.setProgress(curProgress);
//            tv_curProgress.setText(String.format(getResources().getString(R.string.tv_curminutes), curProgress / 60, curProgress % 60));
        }
    }

    private void changeState() {
        if (isPause) {
            btn_playPause.setBackground(getDrawable(R.drawable.ic_play_circle_outline_white_40dp));
            animator.pause();
        } else {
            btn_playPause.setBackground(getDrawable(R.drawable.ic_pause_circle_outline_white_40dp));
            animator.resume();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        iv_background.destory();
        animator.cancel();
        seekbarExecutor.shutdownNow();
        localBroadcastManager.unregisterReceiver(musicChangedReceiver);
        unbindService(serviceConnection);
    }

    private Runnable updateSeekBarRunnable;

    private static class UpdateSeekbarRunnable implements Runnable {
        private WeakReference<PlayMusicActivity> mWeakReference;

        public UpdateSeekbarRunnable(PlayMusicActivity reference) {
            this.mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (mWeakReference != null) {
                        if (!mWeakReference.get().isPause) {
                            mWeakReference.get().mHandler.sendEmptyMessage(MSG_UPDATESEEKBAR);
//                            mWeakReference.get().log("sendUpdateSeekbarMessage");
                        }
                        Thread.sleep(500);
                    } else {
                        break;
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private final static int MSG_UPDATESEEKBAR = 1;
    public final static int MSG_BACKGROUNDCOLOR = 2;
    private static class PlayMusicHandler extends Handler {
        private WeakReference<PlayMusicActivity> mWeakReference;

        public PlayMusicHandler(PlayMusicActivity reference) {
            this.mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATESEEKBAR:
                    if (mWeakReference != null) {
                        mWeakReference.get().updateSeekBarProgress();
                    }
                    break;
                case MSG_BACKGROUNDCOLOR:
                    mWeakReference.get().relay_myrelay.setColor(msg.arg1 ,msg.arg2);
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play_pause:
                mediaPlayerService.pauseOrPlay();
                break;
            case R.id.btn_skip_pre:
                mediaPlayerService.skipPre();
                break;
            case R.id.btn_skip_next:
                mediaPlayerService.skipNext();
                break;
            case R.id.btn_playlist:
                if (playListFragment == null) {
                    playListFragment = new PlayListFragment();
                }
                playListFragment.show(getSupportFragmentManager(), "bottomSheet");
                break;
            case R.id.btn_tbback:
                finish();
                break;
        }
    }

    @Override
    public void changeMusic(int curMusic, boolean change) {
        mediaPlayerService.setCurMusic(curMusic);
        if (change) {
            mediaPlayerService.play();
        }
    }

    private boolean isSeeking = false;
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//        seekBar.setProgress(progress);
        tv_curProgress.setText(String.format(getResources().getString(R.string.tv_curminutes), progress / 60, progress % 60));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isSeeking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mediaPlayerService.setcurPosition(seekBar.getProgress());
        isSeeking = false;
    }

    public int getStatusBarHeight() {
        int result = 0;
        //获取状态栏高度的资源id
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void log(String msg) {
        Log.d("PlayMusicActivity", msg);
    }
}
