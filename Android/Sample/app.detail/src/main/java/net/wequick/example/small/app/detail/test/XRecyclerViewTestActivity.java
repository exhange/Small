package net.wequick.example.small.app.detail.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.wequick.example.appstub.Note;
import net.wequick.example.small.app.detail.R;

import java.util.ArrayList;
import java.util.List;

import cn.droidlover.xrecyclerview.RecyclerItemCallback;
import cn.droidlover.xrecyclerview.XRecyclerView;

public class XRecyclerViewTestActivity extends Activity {

    XRecyclerView recyclerView;
    TextView tv_next;

    TestRecAdapter adapter;

    static final int MAX_PAGE = 5;
    private int pageSize = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xrecyclerview_test);

        recyclerView = (XRecyclerView) findViewById(R.id.recyclerView);
        tv_next = (TextView) findViewById(R.id.tv_next);

        tv_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(XRecyclerViewTestActivity.this, NextActivity.class));
            }
        });

        initAdapter();
        loadData(1);
    }

    private void initAdapter() {
        if (adapter == null) adapter = new TestRecAdapter(this);
        recyclerView.verticalLayoutManager(this)        //设置layoutManager
                .setAdapter(adapter);                   //设置Adapter

        adapter.setRecItemClick(new RecyclerItemCallback<Note, TestRecAdapter.ViewHolder>() {
            @Override
            public void onItemClick(int position, Note model, int tag, TestRecAdapter.ViewHolder holder) {
                super.onItemClick(position, model, tag, holder);

                switch (tag) {
                    case TestRecAdapter.TAG_CLICK:
                        Toast.makeText(XRecyclerViewTestActivity.this, "position:" + position, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
        recyclerView.horizontalDivider(R.color.x_red, R.dimen.divider_height);  //设置divider
        recyclerView.setOnRefreshAndLoadMoreListener(new XRecyclerView.OnRefreshAndLoadMoreListener() { //设置刷新和上拉加载监听
            @Override
            public void onRefresh() {
                loadData(1);
            }

            @Override
            public void onLoadMore(int page) {
                loadData(page);
            }
        });
        recyclerView.useDefLoadMoreView();
    }

    private void loadData(final int page) {
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Note> list = buildData(page);
                if (page > 1) {
                    adapter.addData(list);
                } else {
                    adapter.setData(list);
                }
                recyclerView.setPage(page, MAX_PAGE);
            }
        }, 500L);

    }

    private List<Note> buildData(int page) {
        List<Note> list = new ArrayList<>();

        int init = (page - 1) * pageSize;

        for (int pos = init; pos < page * pageSize; pos++) {
            Note mNote = new Note((long) pos);
            mNote.setText(pos + "");
            mNote.setComment("测试" + pos);
            list.add(mNote);
        }
        return list;
    }

}
