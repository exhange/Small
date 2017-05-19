package com.czt.mp3recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

import com.czt.mp3recorder.util.CztUtil;
import com.czt.mp3recorder.util.LameUtil;

import java.io.File;
import java.io.IOException;

public class MP3Recorder {

    private static final String TAG = "MP3Recorder";
    //=======================AudioRecord Default Settings=======================
    private static final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    /**
     * 以下三项为默认配置参数。Google Android文档明确表明只有以下3个参数是可以在所有设备上保证支持的。
     */
    private static final int DEFAULT_SAMPLING_RATE = 44100;//模拟器仅支持从麦克风输入8kHz采样率
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 下面是对此的封装
     * private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
     */
    private static final PCMFormat DEFAULT_AUDIO_FORMAT = PCMFormat.PCM_16BIT;

    //======================Lame Default Settings=====================
    private static final int DEFAULT_LAME_MP3_QUALITY = 7;
    /**
     * 与DEFAULT_CHANNEL_CONFIG相关，因为是mono单声，所以是1
     */
    private static final int DEFAULT_LAME_IN_CHANNEL = 1;
    /**
     * Encoded bit rate. MP3 file will be encoded with bit rate 32kbps
     */
    private static final int DEFAULT_LAME_MP3_BIT_RATE = 32;

    //==================================================================

    /**
     * 自定义 每160帧作为一个周期，通知一下需要进行编码
     */
    private static final int FRAME_COUNT = 160;
    private AudioRecord mAudioRecord = null;
    private int mBufferSize;
    private short[] mPCMBuffer;
    private DataEncodeThread mEncodeThread;
    private boolean mIsRecording = false;
    private File mRecordFile;
    private String mRecordFileName;
    private String filePath;

    private MediaPlayer mPlayer = null;
    private Handler mHandler;

    /**
     * Default constructor. Setup recorder with default sampling rate 1 channel,
     * 16 bits pcm
     *
     * @param filePath target file
     */
    public MP3Recorder(String filePath, Handler mHandler) {
        if (!TextUtils.isEmpty(filePath) && filePath.endsWith(".mp3")) {
            this.filePath = filePath.substring(0, filePath.lastIndexOf("/"));
            mRecordFileName = CztUtil.getFileName(filePath);
        } else {
            this.filePath = filePath;
        }
        this.mHandler = mHandler;

        try {
            prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start recording. Create an encoding thread. Start record from this
     * thread.
     *
     * @throws IOException initAudioRecorder throws
     */
    public void startRecording() throws IOException {
        if (mIsRecording) return;
        initAudioRecorder();
        mAudioRecord.startRecording();
        new Thread() {

            @Override
            public void run() {
                //设置线程权限
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                mIsRecording = true;
                while (mIsRecording) {

                    if (mIsPause) {
                        continue;
                    }

                    int readSize = mAudioRecord.read(mPCMBuffer, 0, mBufferSize);
                    if (readSize > 0) {
                        mEncodeThread.addTask(mPCMBuffer, readSize);
                        calculateRealVolume(mPCMBuffer, readSize);
                    }

                    if (mHandler != null) {
                        mHandler.sendEmptyMessage(mVolume);
                    }
                }
            }

            /**
             * 此计算方法来自samsung开发范例
             *
             * @param buffer buffer
             * @param readSize readSize
             */
            private void calculateRealVolume(short[] buffer, int readSize) {
                double sum = 0;
                for (int i = 0; i < readSize; i++) {
                    // 这里没有做运算的优化，为了更加清晰的展示代码
                    sum += buffer[i] * buffer[i];
                }
                if (readSize > 0) {
                    double amplitude = sum / readSize;
                    mVolume = (int) Math.sqrt(amplitude);
                }
            }

            ;
        }.start();
    }

    private int mVolume;

    /**
     * 获取真实的音量。 [算法来自三星]
     *
     * @return 真实音量
     */
    public int getRealVolume() {
        return mVolume;
    }

    /**
     * 获取相对音量。 超过最大值时取最大值。
     *
     * @return 音量
     */
    public int getVolume() {
        if (mVolume >= MAX_VOLUME) {
            return MAX_VOLUME;
        }
        return mVolume;
    }

    private static final int MAX_VOLUME = 2000;

    /**
     * 根据资料假定的最大值。 实测时有时超过此值。
     *
     * @return 最大音量值。
     */
    public int getMaxVolume() {
        return MAX_VOLUME;
    }


    public boolean ismIsPause() {
        return mIsPause;
    }

    public void stopRecording() {
        Log.i(TAG, "stopRecording");
        if (!mIsRecording) {
            return;
        }

        if (mAudioRecord == null) {
            return;
        }

        mIsRecording = false;
        mIsPause = false;
        // release and finalize audioRecord
        mAudioRecord.stop();
        mAudioRecord.release();
        mAudioRecord = null;
        // stop the encoding thread and try to wait
        // until the thread finishes its job
        if (mEncodeThread != null) {
            Message msg = Message.obtain(mEncodeThread.getHandler(), DataEncodeThread.PROCESS_STOP);
            msg.sendToTarget();
        }
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    private boolean mIsPause = false;

    public void pauseRecording() {
        mIsPause = true;
    }

    public void restartRecording() {
        mIsPause = false;
    }

    public File getmRecordFile() {
        return mRecordFile;
    }

    public void startPlaying() {
         Log.i(TAG, "startPlaying");
        if (mPlayer != null) {
            return;
        }
        mPlayer = new MediaPlayer();
        try {
             Log.i(TAG, "DATA SOURCE: " + mRecordFile.getAbsolutePath());
            mPlayer.setDataSource(mRecordFile.getAbsolutePath());
            mPlayer.prepare();
            mPlayer.start();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mPlayer.release();
                    mPlayer = null;
                }
            });
        } catch (IOException e) {
             Log.i(TAG, e.toString() + "\n prepare() failed");
        }
    }


    public void stopPlaying() {
         Log.i(TAG, "stopPlaying");
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void release() {

         Log.i(TAG, "release");
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }

        if (mAudioRecord != null) {
            mIsRecording = false;
            mIsPause = false;

            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }

        // stop the encoding thread and try to wait
        // until the thread finishes its job
        if (mEncodeThread != null) {
            Message msg = Message.obtain(mEncodeThread.getHandler(), DataEncodeThread.PROCESS_STOP);
            msg.sendToTarget();
        }
    }

    /**
     * Initialize audio recorder
     */
    public void prepare() throws IOException {
        mBufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLING_RATE,
                DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT.getAudioFormat());

        int bytesPerFrame = DEFAULT_AUDIO_FORMAT.getBytesPerFrame();
        /* Get number of samples. Calculate the buffer size
         * (round up to the factor of given frame size)
		 * 使能被整除，方便下面的周期性通知
		 * */
        int frameSize = mBufferSize / bytesPerFrame;
        if (frameSize % FRAME_COUNT != 0) {
            frameSize += (FRAME_COUNT - frameSize % FRAME_COUNT);
            mBufferSize = frameSize * bytesPerFrame;
        }

		/* Setup audio recorder */
        mAudioRecord = new AudioRecord(DEFAULT_AUDIO_SOURCE,
                DEFAULT_SAMPLING_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT.getAudioFormat(),
                mBufferSize);

        mPCMBuffer = new short[mBufferSize];
        /*
         * Initialize lame buffer
		 * mp3 sampling rate is the same as the recorded pcm sampling rate 
		 * The bit rate is 32kbps
		 * 
		 */
        LameUtil.init(DEFAULT_SAMPLING_RATE, DEFAULT_LAME_IN_CHANNEL, DEFAULT_SAMPLING_RATE, DEFAULT_LAME_MP3_BIT_RATE, DEFAULT_LAME_MP3_QUALITY);
    }

    private void initAudioRecorder() {
        // Create and run thread used to encode data
        // The thread will
        try {
            Time time = new Time();
            time.setToNow();
            File dirFile = new File(filePath);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }

            if (!TextUtils.isEmpty(mRecordFileName)) {
                mRecordFile = new File(filePath, mRecordFileName);
            } else {
                mRecordFile = new File(filePath, time.format("%Y%m%d%H%M%S") + ".mp3");
            }

            mEncodeThread = new DataEncodeThread(mRecordFile, mBufferSize);
            mEncodeThread.start();
            mAudioRecord.setRecordPositionUpdateListener(mEncodeThread, mEncodeThread.getHandler());
            mAudioRecord.setPositionNotificationPeriod(FRAME_COUNT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean deleteAudioFile() {
        if (mRecordFile != null && mRecordFile.exists()) {
            return mRecordFile.delete();
        }
        return false;
    }
}