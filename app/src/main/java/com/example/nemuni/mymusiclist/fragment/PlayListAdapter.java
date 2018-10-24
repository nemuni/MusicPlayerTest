package com.example.nemuni.mymusiclist.fragment;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.Log;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.nemuni.mymusiclist.R;
import com.example.nemuni.mymusiclist.bean.MusicMsg;

import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author nemuni
 * @create 2018/9/29
 * @since 1.0.0
 */
public class PlayListAdapter extends BaseQuickAdapter<MusicMsg, BaseViewHolder> {

    public int curMusic;
    private Context context;

    public PlayListAdapter(@Nullable List<MusicMsg> data, Context context) {
        super(R.layout.adapter_playlist, data);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, MusicMsg item) {
        helper.setGone(R.id.iv_playing, false)
                .setText(R.id.tv_musicname, item.getMusic())
                .setTextColor(R.id.tv_musicname, Color.BLACK)
                .setText(R.id.tv_musicsinger, " - " + item.getSinger())
                .setTextColor(R.id.tv_musicsinger, Color.parseColor("#8A000000"))
                .addOnClickListener(R.id.iv_remove);

        if (helper.getLayoutPosition() == curMusic) {
            helper.setGone(R.id.iv_playing, true);
            helper.setTextColor(R.id.tv_musicname, Color.RED);
            helper.setTextColor(R.id.tv_musicsinger, Color.RED);
        }
        Log.d("PlayListAdapter", "position: " + helper.getLayoutPosition() + helper.itemView.getBackground());
    }

    public void releaseContext() {
        context = null;
    }
}
