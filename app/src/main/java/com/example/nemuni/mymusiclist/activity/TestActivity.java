package com.example.nemuni.mymusiclist.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.audiofx.Visualizer;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.nemuni.mymusiclist.R;
import com.example.nemuni.mymusiclist.myviews.MyView;
import com.example.nemuni.mymusiclist.service.MediaPlayerService;

public class TestActivity extends AppCompatActivity {

    private MyView test_myview;

    private MediaPlayerService mediaPlayerService;
    private boolean isBindService = false;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
            mediaPlayerService = (MediaPlayerService) binder.getService();
            isBindService = true;
            initVisualizer();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mediaPlayerService = null;
        }
    };
    private Visualizer visualizer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        test_myview = findViewById(R.id.test_myview);
        test_myview.init();
        bindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        visualizer.release();
    }

    private void bindService() {
        if (!isBindService) {
            Intent intent = new Intent(this, MediaPlayerService.class);
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        }
    }

    private void initVisualizer() {
        visualizer = new Visualizer(mediaPlayerService.getAudioSession());
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
//                relay_test.updateVisualizer(waveform);
//                relay_test.setWaveData(waveform);
                test_myview.setWaveData(waveform);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
//                int frequencyCounts = fft.length / 2 + 1;
//                byte[] bytes = new byte[frequencyCounts];
//                bytes[0] = (byte)Math.abs(fft[0]);
//                for (int i = 1; i < frequencyCounts-1; i++) {
//                    bytes[i] = (byte)Math.hypot(fft[2*i], fft[i*2+1]);
//                }
//                fft[frequencyCounts-1] = (byte)Math.abs(fft[1]);
//                relay_test.updateVisualizer(bytes);
            }
        }, Visualizer.getMaxCaptureRate() / 2, true, false);
        visualizer.setEnabled(true);
    }

    private void log(String msg) {
        Log.d("TestActivity", msg);
    }
}
