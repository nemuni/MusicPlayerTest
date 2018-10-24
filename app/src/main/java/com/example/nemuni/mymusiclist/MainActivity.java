package com.example.nemuni.mymusiclist;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.nemuni.mymusiclist.fragment.PlayListFragment;
import com.example.nemuni.mymusiclist.activity.PlayMusicActivity;
import com.example.nemuni.mymusiclist.adapter.MusicListAdapter;
import com.example.nemuni.mymusiclist.entry.Data;
import com.example.nemuni.mymusiclist.bean.MusicMsg;
import com.example.nemuni.mymusiclist.receiver.MusicChangedReceiver;
import com.example.nemuni.mymusiclist.service.PlayBackgroundService;

import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,PlayListFragment.PlayListListener {

    private RecyclerView rvList;
    private RelativeLayout relay_Play;
    private Button btn_PlayPause;
    private Button btn_SkipPre;
    private Button btn_SkipNext;
    private Button btn_PlayList;
    private TextView tv_MusicName;
    private TextView tv_Singer;

    private List<MusicMsg> musics;
    private MusicListAdapter adapter;
    private Handler mServiceHandler;
    private boolean isBindService = false;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            log("onServiceConnected");
            PlayBackgroundService.PlayBackgroundServiceBinder binder =
                    (PlayBackgroundService.PlayBackgroundServiceBinder) service;
            mServiceHandler = binder.getHandler();
            isBindService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private MusicChangedReceiver musicChangedReceiver;
    private LocalBroadcastManager localBroadcastManager;
    private PlayListFragment playListFragment;
    private boolean isPause = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO
                }, 1010);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 1010);
            }
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO
            }, 1010);
        }else {
            initActivity();
        }
    }

    private void initActivity() {
        initView();
        bindService();
        initMusicList();
        initListener();
        registerReceiver();
    }

    private void initView() {
        rvList = findViewById(R.id.rvList);
        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        relay_Play = findViewById(R.id.relay_play);
        btn_PlayPause = findViewById(R.id.btn_play_pause);
        btn_SkipPre = findViewById(R.id.btn_skip_pre);
        btn_SkipNext = findViewById(R.id.btn_skip_next);
        btn_PlayList = findViewById(R.id.btn_playlist);
        tv_MusicName = findViewById(R.id.tv_musicname);
        tv_Singer = findViewById(R.id.tv_singer);
    }

    private void initMusicList() {
        musics = Data.getLocalMusicMsgList(this);
        adapter = new MusicListAdapter(musics);
        adapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM);
        rvList.setAdapter(adapter);
    }

    private void initListener() {
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Data.setPlayMusicList(musics);
                Message msg = Message.obtain(mServiceHandler, PlayBackgroundService.SERIVCEMSG_CHANGEMUSIC);
                msg.arg1 = position;
                msg.sendToTarget();
//                if (btn_SkipPre.getVisibility() == View.GONE) {
//                    btn_PlayList.setVisibility(View.VISIBLE);
//                    btn_SkipNext.setVisibility(View.VISIBLE);
//                    btn_PlayPause.setVisibility(View.VISIBLE);
//                    btn_SkipPre.setVisibility(View.VISIBLE);
//                }
                if (relay_Play.getVisibility() == View.GONE) {
                    relay_Play.setVisibility(View.VISIBLE);
                }
            }
        });
        btn_PlayPause.setOnClickListener(this);
        btn_SkipPre.setOnClickListener(this);
        btn_SkipNext.setOnClickListener(this);
        btn_PlayList.setOnClickListener(this);
        relay_Play.setOnClickListener(this);
    }

    private void bindService() {
        if (!isBindService) {
            Log.d("MainActivity", "bindService");
            Intent intent = new Intent(this, PlayBackgroundService.class);
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
                            MusicMsg music = Data.getPlayingMusicMsg();
                            tv_MusicName.setText(music.getMusic());
                            tv_Singer.setText(music.getSinger());
                            if (playListFragment != null) {
                                playListFragment.refreshCurMusic();
                            }
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

    /**
     *
     * @param nextMusic
     */
    @Override
    public void changeMusic(int nextMusic) {
        Message msg = Message.obtain(mServiceHandler, PlayBackgroundService.SERIVCEMSG_CHANGEMUSIC);
        msg.arg1 = nextMusic;
        msg.sendToTarget();
    }

    @Override
    public void changePlayMode(int playMode, boolean display) {

    }

    private void changeState() {
        if (isPause) {
            btn_PlayPause.setBackground(getDrawable(R.drawable.ic_play_circle_outline_black_32dp));
        } else {
            btn_PlayPause.setBackground(getDrawable(R.drawable.ic_pause_circle_outline_black_40dp));
        }
    }

//    private MediaPlayer mediaPlayer;
//    private void initMediaPlayer() {
//        mediaPlayer = new MediaPlayer();
//        if (musics.size() > 0) {
//            try {
//                mediaPlayer.setDataSource(musics.get(0).getPath());
//                mediaPlayer.prepare();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        // 设置设备进入锁状态模式-可在后台播放或者缓冲音乐-CPU一直工作
//        //mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
//        // 处理音频焦点-处理多个程序会来竞争音频输出设备
//        // 处理AUDIO_BECOMING_NOISY意图
//        // 设置播放错误监听
//        // 设置播放完成监听
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play_pause:
                Message msg1 = Message.obtain(mServiceHandler, PlayBackgroundService.SERVICEMSG_PLAYORPAUSE);
                msg1.sendToTarget();
                break;
            case R.id.btn_skip_pre:
                Message msg2 = Message.obtain(mServiceHandler, PlayBackgroundService.SERVICEMSG_SKIPPRE);
                msg2.sendToTarget();
                break;
            case R.id.btn_skip_next:
                Message msg3 = Message.obtain(mServiceHandler, PlayBackgroundService.SERVICEMSG_SKIPNEXT);
                msg3.sendToTarget();
                break;
            case R.id.btn_playlist:
                if (playListFragment == null) {
                    playListFragment = new PlayListFragment();
//                    playListFragment.refreshCurMusic();
                }
                playListFragment.show(getSupportFragmentManager(), "bottomSheet");
                break;
            case R.id.relay_play:
                Intent intent = new Intent(this, PlayMusicActivity.class);
                startActivity(intent);
                break;
            default :
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)  {
            case 1010:
                if (grantResults.length > 0) {
                    boolean allGranted = true;
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (allGranted) {
                        initActivity();
                    } else {
                        Toast.makeText(this, "程序获取权限失败，无法运行", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(this, "程序获取权限失败，无法运行", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        localBroadcastManager.unregisterReceiver(musicChangedReceiver);
    }

    private void log(String msg) {
        Log.d("MainActivity", msg);
    }
}
