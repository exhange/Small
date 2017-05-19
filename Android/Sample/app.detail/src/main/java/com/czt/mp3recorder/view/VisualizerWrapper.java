package com.czt.mp3recorder.view;

import android.content.Context;
import android.media.audiofx.Visualizer;
import android.support.annotation.NonNull;

/**
 * Wrapper for visualizer.
 */
class VisualizerWrapper {

    private static final long WAIT_UNTIL_HACK = 500;
    private Visualizer visualizer;
    private Visualizer.OnDataCaptureListener captureListener;
    private int captureRate;
    private long lastZeroArrayTimestamp;

    public VisualizerWrapper(@NonNull Context context, int audioSessionId, @NonNull final OnFftDataCaptureListener onFftDataCaptureListener) {
        visualizer = new Visualizer(audioSessionId);
        visualizer.setEnabled(false);
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        captureRate = Visualizer.getMaxCaptureRate();
        captureListener = new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {

            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                boolean allZero = Utils.allElementsAreZero(fft);
                if (lastZeroArrayTimestamp == 0) {
                    if (allZero) {
                        lastZeroArrayTimestamp = System.currentTimeMillis();
                    }
                } else {
                    if (!allZero) {
                        lastZeroArrayTimestamp = 0;
                    } else if (System.currentTimeMillis() - lastZeroArrayTimestamp >= WAIT_UNTIL_HACK) {
                        setEnabled(true);
                        lastZeroArrayTimestamp = 0;
                    }
                }
                onFftDataCaptureListener.onFftDataCapture(fft);
            }
        };
        visualizer.setEnabled(true);
    }

    public void release() {
        visualizer.setEnabled(false);
        visualizer.release();
        visualizer = null;
    }

    public void setEnabled(final boolean enabled) {
        if (visualizer == null) return;
        visualizer.setEnabled(false);
        if (enabled) {
            visualizer.setDataCaptureListener(captureListener, captureRate, false, true);
        } else {
            visualizer.setDataCaptureListener(null, captureRate, false, false);
        }
        visualizer.setEnabled(true);
    }

    public interface OnFftDataCaptureListener {
        void onFftDataCapture(byte[] fft);
    }
}
