package com.czt.mp3recorder;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czt.mp3recorder.util.CztUtil;
import com.czt.mp3recorder.view.DbmHandler;
import com.czt.mp3recorder.view.GLAudioVisualizationView;

import net.wequick.example.small.app.detail.R;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class AudioRecorderActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private final static String TAG = AudioRecorderActivity.class.getSimpleName();

    private String filePath;
    private int color;
    private boolean autoStart;
    private boolean keepDisplayOn;

    private MediaPlayer player;
    private MP3Recorder mRecorder;
    private VisualizerHandler visualizerHandler;

    private Timer timer;
    private MenuItem saveMenuItem;
    private int recorderSecondsElapsed;
    private int playerSecondsElapsed;
    private boolean isRecording;

    private RelativeLayout contentLayout;
    private GLAudioVisualizationView visualizerView;
    private TextView statusView;
    private TextView timerView;
    private ImageButton restartView;
    private ImageButton recordView;
    private ImageButton playView;

    private AudioServiceConn conn;
    private AudioRecorderService audioService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aar_activity_audio_recorder);

        if (savedInstanceState != null) {
            filePath = savedInstanceState.getString(AndroidAudioRecorder.EXTRA_FILE_PATH);
            color = savedInstanceState.getInt(AndroidAudioRecorder.EXTRA_COLOR);
            autoStart = savedInstanceState.getBoolean(AndroidAudioRecorder.EXTRA_AUTO_START);
            keepDisplayOn = savedInstanceState.getBoolean(AndroidAudioRecorder.EXTRA_KEEP_DISPLAY_ON);
        } else {
            filePath = getIntent().getStringExtra(AndroidAudioRecorder.EXTRA_FILE_PATH);
            color = getIntent().getIntExtra(AndroidAudioRecorder.EXTRA_COLOR, Color.BLACK);
            autoStart = getIntent().getBooleanExtra(AndroidAudioRecorder.EXTRA_AUTO_START, false);
            keepDisplayOn = getIntent().getBooleanExtra(AndroidAudioRecorder.EXTRA_KEEP_DISPLAY_ON, false);
        }

        if (keepDisplayOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(CztUtil.getDarkerColor(color)));
            getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.mipmap.aar_ic_clear));
        }

        conn = new AudioServiceConn();
        bindService(new Intent(this, AudioRecorderService.class), conn, BIND_AUTO_CREATE);

        visualizerView = new GLAudioVisualizationView.Builder(this)
                .setLayersCount(1)
                .setWavesCount(6)
                .setWavesHeight(R.dimen.aar_wave_height)
                .setWavesFooterHeight(R.dimen.aar_footer_height)
                .setBubblesPerLayer(20)
                .setBubblesSize(R.dimen.aar_bubble_size)
                .setBubblesRandomizeSize(true)
                .setBackgroundColor(CztUtil.getDarkerColor(color))
                .setLayerColors(new int[]{color})
                .build();

        contentLayout = (RelativeLayout) findViewById(R.id.content);
        statusView = (TextView) findViewById(R.id.status);
        timerView = (TextView) findViewById(R.id.timer);
        restartView = (ImageButton) findViewById(R.id.restart);
        recordView = (ImageButton) findViewById(R.id.record);
        playView = (ImageButton) findViewById(R.id.play);

        contentLayout.setBackgroundColor(CztUtil.getDarkerColor(color));
        contentLayout.addView(visualizerView, 0);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);

        if (CztUtil.isBrightColor(color)) {
            ContextCompat.getDrawable(this, R.mipmap.aar_ic_clear).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            ContextCompat.getDrawable(this, R.mipmap.aar_ic_check).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            statusView.setTextColor(Color.BLACK);
            timerView.setTextColor(Color.BLACK);
            restartView.setColorFilter(Color.BLACK);
            recordView.setColorFilter(Color.BLACK);
            playView.setColorFilter(Color.BLACK);
        }

        //mRecorder = new MP3Recorder(filePath, mhandler);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (autoStart && !isRecording) {
            toggleRecording(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            visualizerView.onResume();
        } catch (Exception e) {
        }
    }

    @Override
    protected void onPause() {
        try {
            visualizerView.onPause();
        } catch (Exception e) {
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mRecorder != null && mRecorder.isRecording()) {
            mRecorder.release();
        } else if (isPlaying()) {
            stopPlaying(null);
        }

        setResult(RESULT_CANCELED);
        try {
            visualizerView.release();
        } catch (Exception e) {
        }
        unbindService(conn);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(AndroidAudioRecorder.EXTRA_FILE_PATH, filePath);
        outState.putInt(AndroidAudioRecorder.EXTRA_COLOR, color);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.aar_audio_recorder, menu);
        saveMenuItem = menu.findItem(R.id.action_save);
        saveMenuItem.setIcon(ContextCompat.getDrawable(this, R.mipmap.aar_ic_check));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.action_save) {
            selectAudio();
        }
        return super.onOptionsItemSelected(item);
    }


    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mRecorder != null) {
                float vol = (mRecorder.getVolume() / (float) mRecorder.getMaxVolume()) * 100;
                float amplitude = isRecording ? vol : 0f;
                float upVol = amplitude * 3.0f;
                float lastVol = upVol < 100 ? upVol : 100;
                visualizerHandler.onDataReceived(lastVol);
            }
        }
    };

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopPlaying(null);
    }

    private void selectAudio() {
        if (mRecorder != null && mRecorder.isRecording()) {
            mRecorder.release();
        }
        Intent it = new Intent();
        it.putExtra("AudioFile", filePath);
        setResult(RESULT_OK, it);
        finish();
    }

    public void toggleRecording(final View v) {
        stopPlaying(v);
        CztUtil.wait(100, new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    pauseRecording(v);
                } else {
                    resumeRecording();
                }
            }
        });
    }

    public void togglePlaying(final View v) {
        pauseRecording(v);
        CztUtil.wait(100, new Runnable() {
            @Override
            public void run() {
                if (isPlaying()) {
                    stopPlaying(v);
                } else {
                    startPlaying();
                }
            }
        });
    }

    public void restartRecording(View v) {
        if (mRecorder != null && mRecorder.isRecording()) {
            mRecorder.release();
            boolean isDel = mRecorder.deleteAudioFile();
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (isPlaying()) {
            stopPlaying(v);
        } else {
            visualizerHandler = new VisualizerHandler();
            visualizerView.linkTo(visualizerHandler);
            visualizerView.release();
            if (visualizerHandler != null) {
                visualizerHandler.stop();
            }
        }
        saveMenuItem.setVisible(false);
        statusView.setVisibility(View.INVISIBLE);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);
        recordView.setImageResource(R.drawable.aar_ic_rec);
        timerView.setText("00:00:00");
        recorderSecondsElapsed = 0;
        playerSecondsElapsed = 0;
    }

    private void resumeRecording() {
        isRecording = true;
        saveMenuItem.setVisible(false);
        statusView.setText(R.string.aar_recording);
        statusView.setVisibility(View.VISIBLE);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);
        recordView.setImageResource(R.drawable.aar_ic_pause);
        playView.setImageResource(R.drawable.aar_ic_play);

        visualizerHandler = new VisualizerHandler();
        visualizerView.linkTo(visualizerHandler);

        if (mRecorder == null) {
            timerView.setText("00:00:00");
        } else {

            if (mRecorder.ismIsPause()) {
                mRecorder.restartRecording();
            }

            try {
                if (!mRecorder.isRecording()) {
                    mRecorder.startRecording();
                    filePath = mRecorder.getmRecordFile().getAbsolutePath();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            startTimer();
        }
    }

    private void pauseRecording(View view) {
        isRecording = false;
        if (!isFinishing()) {
            saveMenuItem.setVisible(true);
        }

        if (view.getId() == R.id.record) {
            statusView.setText(R.string.aar_paused);
            statusView.setVisibility(View.VISIBLE);
        }

        restartView.setVisibility(View.VISIBLE);
        playView.setVisibility(View.VISIBLE);
        recordView.setImageResource(R.drawable.aar_ic_rec);
        playView.setImageResource(R.drawable.aar_ic_play);

        visualizerView.release();
        if (visualizerHandler != null) {
            visualizerHandler.stop();
        }

        if (mRecorder != null) {
            mRecorder.pauseRecording();
        }

        stopTimer();
    }

    private void startPlaying() {
        try {
            player = new MediaPlayer();
            player.setDataSource(filePath);
            player.prepare();
            player.start();

            visualizerView.linkTo(DbmHandler.Factory.newVisualizerHandler(this, player));
            visualizerView.post(new Runnable() {
                @Override
                public void run() {
                    player.setOnCompletionListener(AudioRecorderActivity.this);
                }
            });

            timerView.setText("00:00:00");
            statusView.setText(R.string.aar_playing);
            statusView.setVisibility(View.VISIBLE);
            playView.setImageResource(R.drawable.aar_ic_stop);

            playerSecondsElapsed = 0;
            startTimer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPlaying(View view) {
        if (view == null) {
            statusView.setText(R.string.aar_playing_over);
        } else if (view.getId() == R.id.play) {
            statusView.setText(R.string.aar_playing_paused);
        }
        statusView.setVisibility(View.VISIBLE);
        playView.setImageResource(R.drawable.aar_ic_play);

        visualizerView.release();
        if (visualizerHandler != null) {
            visualizerHandler.stop();
        }

        if (player != null) {
            try {
                player.stop();
                player.reset();
            } catch (Exception e) {
            }
        }

        stopTimer();
    }

    private boolean isPlaying() {
        try {
            return player != null && player.isPlaying() && !isRecording;
        } catch (Exception e) {
            return false;
        }
    }

    private void startTimer() {
        if (timer == null) {
            stopTimer();
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateTimer();
                }
            }, 0, 1000);
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    private void updateTimer() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    recorderSecondsElapsed++;
                    timerView.setText(CztUtil.formatSeconds(recorderSecondsElapsed));
                } else if (isPlaying()) {
                    playerSecondsElapsed++;
                    timerView.setText(CztUtil.formatSeconds(playerSecondsElapsed));
                }
            }
        });
    }

    private class AudioServiceConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            audioService = ((AudioRecorderService.AudioBinder) binder).getService();
            audioService.setFilePath(filePath, mhandler);
            mRecorder = audioService.getmRecorder();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            audioService = null;
        }
    }
}
