package net.wequick.example.small.app.detail.test;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.wequick.example.appstub.Note;
import net.wequick.example.small.app.detail.R;

import cn.droidlover.xrecyclerview.RecyclerAdapter;

/**
 * @author wanglei
 * @version 1.5.2
 * @description
 * @createTime 2016/10/31 12:57
 * @editTime
 * @editor
 */
public class TestRecAdapter extends RecyclerAdapter<Note, TestRecAdapter.ViewHolder> {

    public static final int TAG_CLICK = 0;    //点击标识

    public TestRecAdapter(Context context) {
        super(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_test, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Note item = data.get(position);
        holder.tv_msg.setText(item.getText());
        holder.tv_comment.setText(item.getComment());

        holder.list_itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getRecItemClick() != null) {
                    getRecItemClick().onItemClick(position, item, TAG_CLICK, holder);
                }
            }
        });
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_msg;
        TextView tv_comment;
        LinearLayout list_itemLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_msg = (TextView) itemView.findViewById(R.id.tv_msg);
            tv_comment = (TextView) itemView.findViewById(R.id.textViewNoteComment);
            list_itemLayout = (LinearLayout) itemView.findViewById(R.id.list_itemLayout);
        }
    }

}
