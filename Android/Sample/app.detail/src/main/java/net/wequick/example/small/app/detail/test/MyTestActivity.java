package net.wequick.example.small.app.detail.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.czt.mp3recorder.AndroidAudioRecorder;

import net.wequick.example.small.app.detail.R;


/**
 * Created by huangzhen on 2016/12/19.
 */

public class MyTestActivity extends Activity implements View.OnClickListener {

    Button btn_test1, btn_test2, btn_test3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        btn_test1 = (Button) findViewById(R.id.btn_test1);
        btn_test2 = (Button) findViewById(R.id.btn_test2);
        btn_test3 = (Button) findViewById(R.id.btn_test3);
        btn_test1.setOnClickListener(this);
        btn_test2.setOnClickListener(this);
        btn_test3.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_test1:
                Intent it1 = new Intent();
                it1.setClass(MyTestActivity.this, XRecyclerViewTestActivity.class);
                startActivity(it1);
                break;
            case R.id.btn_test2:
                Intent it2 = new Intent();
                it2.setClass(MyTestActivity.this, GreenDaoTestActivity.class);
                startActivity(it2);
                break;
            case R.id.btn_test3:
                recordAudio();
                break;
        }
    }

    public void recordAudio() {
        String AUDIO_FILE_PATH = Environment.getExternalStorageDirectory() + "/small/sound/";
        AndroidAudioRecorder.with(this)
                // Required
                .setFilePath(AUDIO_FILE_PATH)
                .setColor(ContextCompat.getColor(this, R.color.recorder_blue))
                .setRequestCode(0)
                // Optional
                .setAutoStart(false)
                .setKeepDisplayOn(true)
                // Start recording
                .record();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Audio recorded successfully!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Audio was not recorded", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
