package com.czt.mp3recorder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

/**
 * Created by cpan on 2016/6/23.
 */
public class AudioRecorderService extends Service {

    public MP3Recorder mRecorder;
    private AudioBinder audioBinder;
    private PowerManager.WakeLock wakeLock;// 电源锁

    @Override
    public void onCreate() {
        super.onCreate();
        acquireWakeLock();
        audioBinder = new AudioBinder();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return audioBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void setFilePath(String filepath, Handler handler) {
        mRecorder = new MP3Recorder(filepath, handler);
    }

    public MP3Recorder getmRecorder() {
        return mRecorder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseWakeLock();
    }

    /**
     * @return void
     * @name acquireWakeLock()
     * @description 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
     */
    private void acquireWakeLock() {
        if (wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getCanonicalName());
            wakeLock.acquire();
        }
    }

    /**
     * @return void
     * @name releaseWakeLock()
     * @description 释放设备电源锁
     */
    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    public final class AudioBinder extends Binder {
        public AudioRecorderService getService() {
            return AudioRecorderService.this;
        }
    }
}
