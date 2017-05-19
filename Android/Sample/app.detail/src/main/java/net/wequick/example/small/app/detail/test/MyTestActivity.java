package net.wequick.example.small.app.detail.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import net.wequick.example.small.app.detail.R;


/**
 * Created by huangzhen on 2016/12/19.
 */

public class MyTestActivity extends Activity implements View.OnClickListener {

    Button btn_test1, btn_test2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        btn_test1 = (Button) findViewById(R.id.btn_test1);
        btn_test2 = (Button) findViewById(R.id.btn_test2);
        btn_test1.setOnClickListener(this);
        btn_test2.setOnClickListener(this);
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
        }
        MyTestActivity.this.finish();
    }
}
