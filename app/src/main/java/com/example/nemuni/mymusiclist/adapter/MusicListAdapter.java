package com.example.nemuni.mymusiclist.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.nemuni.mymusiclist.R;
import com.example.nemuni.mymusiclist.entry.MusicMsg;

import java.util.List;

public class MusicListAdapter extends BaseQuickAdapter<MusicMsg, BaseViewHolder> {

    public MusicListAdapter( @Nullable List<MusicMsg> data) {
        super(R.layout.adapter_item_music, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, MusicMsg item) {
        helper.setText(R.id.tv_music, item.getMusic())
                .setText(R.id.tv_musicsize, item.getSizeWithMB())
                .setText(R.id.tv_musicsinger, item.getSinger());
    }
}
