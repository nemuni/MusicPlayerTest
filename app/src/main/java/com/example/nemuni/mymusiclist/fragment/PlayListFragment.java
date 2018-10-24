package com.example.nemuni.mymusiclist.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.nemuni.mymusiclist.R;
import com.example.nemuni.mymusiclist.config.Config;
import com.example.nemuni.mymusiclist.entry.Data;
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
public class PlayListFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private TextView btn_playMode;
    private ImageView btn_removeAll;
    private RecyclerView rv_PlayList;
    private LinearLayoutManager layoutManager;

    private PlayListListener listener;

    private PlayListAdapter adapter;
    private List<MusicMsg> musics;

    public interface PlayListListener {
        void changeMusic(int nextMusic);
        void changePlayMode(int playMode, boolean display);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog =  super.onCreateDialog(savedInstanceState);
        dialog.getWindow().getAttributes().windowAnimations = R.style.MyBottomSheetAnimation;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container);
        btn_playMode = view.findViewById(R.id.btn_playmode);
        btn_playMode.setOnClickListener(this);
        RecyclerView recyclerView = view.findViewById(R.id.rv_playlist);
        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        Log.d("PlayListFragment", "recycHeight: " + params.height);
//        params.height = getHeight();
        Log.d("PlayListFragment", "recycHeight: " + params.height);
        recyclerView.setLayoutParams(params);
        musics = Data.getPlayMusicList();
        initRvList(view);
        return view;
    }

    private void initRvList(View view) {
        rv_PlayList = view.findViewById(R.id.rv_playlist);
        layoutManager = new LinearLayoutManager(getContext());
        rv_PlayList.setLayoutManager(layoutManager);
        rv_PlayList.setItemAnimator(null);
        adapter = new PlayListAdapter(musics ,getContext());
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (position != Data.getCurMusic()) {
                    changeMusic(position);
                }
            }
        });
        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                Data.removeMusicFromPlayList(position);
                adapter.notifyItemRemoved(position);
                if (position == PlayListFragment.this.adapter.curMusic) {
                    if (position >= musics.size()) {
                        changeMusic(musics.size()-1);
                    } else {
                        changeMusic(position);
                    }
                } else if (position < PlayListFragment.this.adapter.curMusic) {
                    PlayListFragment.this.adapter.curMusic--;
                }
            }
        });
        rv_PlayList.setAdapter(adapter);
    }

    private void changeMusic(int nextMusic) {
        adapter.notifyItemChanged(adapter.curMusic);
        adapter.curMusic = nextMusic;
        adapter.notifyItemChanged(nextMusic);
        listener.changeMusic(nextMusic);
    }

    private void changePlayMode(int playMode, boolean callListener) {
        switch (playMode) {
            case Config.PLAY_MODE_SINGLE:
                btn_playMode.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_playmode_single_gray_24dp, null),
                        null, null, null);
                btn_playMode.setText(Config.getPlayModeDesc(playMode));
                break;
            case Config.PLAY_MODE_RANDOM:
                btn_playMode.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_playmode_random_gray_24dp, null),
                        null, null, null);
                btn_playMode.setText(Config.getPlayModeDesc(playMode));
                break;
            default:
                btn_playMode.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_playmode_circulation_gray_24dp, null),
                        null, null, null);
                btn_playMode.setText(Config.getPlayModeDesc(playMode));
                break;
        }
        if (callListener) {
            listener.changePlayMode(playMode, false);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        if (activity instanceof PlayListListener) {
            listener = (PlayListListener) activity;
        } else {
            throw new IllegalArgumentException("activity must implements PlayListListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog)getDialog();
        FrameLayout bottomSheet = dialog.getDelegate().findViewById(android.support.design.R.id.design_bottom_sheet);
        ViewGroup.LayoutParams params = bottomSheet.getLayoutParams();
        params.height = getHeight();
        bottomSheet.setBackground(getContext().getDrawable(R.drawable.dialogbackgroung_shape));
//        dialog.getWindow().getAttributes().windowAnimations = R.style.MyBottomSheetAnimation;
//        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
//        behavior.setPeekHeight(getHeight());
//        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_left);
//        LayoutAnimationController controller = new LayoutAnimationController(animation);
//        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
//        bottomSheet.setLayoutAnimation(controller);
//        bottomSheet.startLayoutAnimation();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.curMusic = Data.getCurMusic();
        adapter.notifyDataSetChanged();
        changePlayMode(Data.getPlayMode(), false);
        rv_PlayList.post(new Runnable() {
            @Override
            public void run() {
                log("findFirstVisible: " + layoutManager.findFirstCompletelyVisibleItemPosition());
                log("findLastVisible: " + layoutManager.findLastCompletelyVisibleItemPosition());
                int first = layoutManager.findFirstVisibleItemPosition();
                int last = layoutManager.findLastVisibleItemPosition();
                first = adapter.curMusic - ((last - first - 1) >> 1);
                log("firstPosition: " + first);
                if (first <= 0) return;
                layoutManager.scrollToPositionWithOffset(first, 0);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.releaseContext();
    }

    private int getHeight() {
        return (int)(getContext().getResources().getDisplayMetrics().heightPixels * 0.6);
    }

    public void refreshCurMusic() {
        int curMusic = Data.getCurMusic();
        if (adapter != null && curMusic != adapter.curMusic) {
            adapter.notifyItemChanged(adapter.curMusic);
            adapter.curMusic = curMusic;
            adapter.notifyItemChanged(adapter.curMusic);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_playmode:
                int playMode = Data.changePlayMode();
                changePlayMode(playMode, true);
                break;
        }
    }

    private void log(String msg) {
        Log.d("PlayListFragment", msg);
    }

}
