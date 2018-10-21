package com.example.nemuni.mymusiclist.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.nemuni.mymusiclist.R;
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
public class PlayListFragment extends BottomSheetDialogFragment {

    private RecyclerView rv_PlayList;

    private ChangeMusic listener;

    private PlayListAdapter adapter;
    private List<MusicMsg> musics;
    private int curMusic;

    public interface ChangeMusic {
        void changeMusic(int curMusic, boolean change);
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
        RecyclerView recyclerView = view.findViewById(R.id.rv_playlist);
        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        Log.d("PlayListFragment", "recycHeight: " + params.height);
        params.height = getHeight();
        Log.d("PlayListFragment", "recycHeight: " + params.height);
        recyclerView.setLayoutParams(params);
        musics = Data.getPlayMusicList();
        initRvList(view);
        return view;
    }

    private void initRvList(View view) {
        rv_PlayList = view.findViewById(R.id.rv_playlist);
        rv_PlayList.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_PlayList.setItemAnimator(null);
        adapter = new PlayListAdapter(musics ,getContext());
        adapter.curMusic = curMusic;
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (position != curMusic) {
                    changeMusic(curMusic, position, true);
//                    view.postDelayed(new MyRunnable(position), 1000);
                }
            }
        });
        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                adapter.remove(position);
                if (position == curMusic) {
                    if (curMusic >= musics.size()) {
                        changeMusic(-1, musics.size()-1, true);
                    } else {
                        changeMusic(-1, curMusic, true);
                    }
                } else if (position < curMusic) {
                    curMusic--;
                    changeMusic(-1, curMusic, false);
                }
            }
        });
        rv_PlayList.setAdapter(adapter);
//        adapter.bindToRecyclerView(rv_PlayList);
    }

    private void changeMusic(int oldMusic, int curMusic, boolean change) {
        this.curMusic = curMusic;
        listener.changeMusic(curMusic, change);
        adapter.curMusic = curMusic;
        if (oldMusic != -1) {
            adapter.notifyItemChanged(oldMusic);
        }
        adapter.notifyItemChanged(curMusic);
//        adapter.notifyDataSetChanged();
//        handler.sendEmptyMessageDelayed(0, 200);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        if (activity instanceof ChangeMusic) {
            listener = (ChangeMusic) activity;
        } else {
            throw new IllegalArgumentException("activity must implements ChangeMusic");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog)getDialog();
        FrameLayout bottomSheet = dialog.getDelegate().findViewById(android.support.design.R.id.design_bottom_sheet);
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
        this.curMusic = Data.getCurMusic();
        adapter.curMusic = this.curMusic;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.releaseContext();
    }

    private int getHeight() {
        return (int)(getContext().getResources().getDisplayMetrics().heightPixels * 0.5);
    }

    public void refreshCurMusic() {
        int curMusic = Data.getCurMusic();
        this.curMusic = curMusic;
        if (adapter != null && curMusic != adapter.curMusic) {
            adapter.curMusic = curMusic;
            adapter.notifyDataSetChanged();
        }
    }

    private void log(String msg) {
        Log.d("PlayListFragment", msg);
    }

    private void performClick(int curMusic) {
        log("performclick");
        for (int i = 0; i < 5; i++) {
            log("position : " + i + rv_PlayList.findViewHolderForAdapterPosition(i).itemView.getBackground());
        }
    }

    class MyRunnable implements Runnable {
        private int index;

        public MyRunnable(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            performClick(index);
        }
    }

    private void notifyAdapter() {
        adapter.notifyDataSetChanged();
    }

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            notifyAdapter();
        }
    };
}
