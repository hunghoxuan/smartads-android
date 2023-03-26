package com.stech.smartads.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.stech.smartads.R;
import com.stech.smartads.adapters.NewsWithImageAdapter;
import com.stech.smartads.models.DataContentObj;
import com.stech.smartads.models.LayoutFrameObj;
import com.stech.smartads.components.autoscrolltwoway.AutoScrollHelper;
import com.stech.smartads.components.autoscrolltwoway.ListViewAutoScrollHelper;
import com.stech.smartads.components.autoscrolltwoway.TwoWayView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class CycleListFragment extends BaseFragment {

    private static final String TAG = CycleListFragment.class.getSimpleName();

    private static final String PARAM_DATA = "data";

    private List<DataContentObj> mNewsObjs = new ArrayList<>();
    private NewsWithImageAdapter mAdapter;
    private TwoWayView listView;
    private ListViewAutoScrollHelper mScrollHelper;
    protected boolean mActionDown;

    public CycleListFragment() {
        // Required empty public constructor
    }

    public static CycleListFragment getInstance(LayoutFrameObj data) {

        CycleListFragment fragment = new CycleListFragment();
        fragment.setLayoutFrame(data);

        Bundle bundle = new Bundle();
        bundle.putParcelable(PARAM_DATA,data);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(PARAM_DATA)) {

            layoutFrame = bundle.getParcelable(PARAM_DATA);

            if(layoutFrame!=null)
             mNewsObjs = layoutFrame.getData();

        }
    }

    @Override
    View inflateLayout(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_twowayview_vertical, container, false);
    }

    @Override
    void initUI(View view) {
        listView = (TwoWayView) view.findViewById(R.id.list);

        mAdapter = new NewsWithImageAdapter(self, mNewsObjs, layoutFrame.getFontColor());
        listView.setAdapter(mAdapter);
        listView.setOnScrollListener(onScrollListener);
        listView.setSelection(Integer.MAX_VALUE / 2);

        initScrollHelper();
        startAutoScroll();
        
    }

    @Override
    void refreshData(LayoutFrameObj data) {
        this.layoutFrame = data;
        this.mNewsObjs.clear();
        this.mNewsObjs.addAll(layoutFrame.getData());
        mAdapter.notifyDataSetChanged();

        startAudioService();
    }

    @Override
    public void onDetach() {

        super.onDetach();
    }


    protected void initScrollHelper() {
        mScrollHelper = new ListViewAutoScrollHelper(listView) {
            @Override
            public void scrollTargetBy(int deltaX, int deltaY) {
                listView.smoothScrollBy(2, 0);
            }
        };

        mScrollHelper.setEnabled(true);
        mScrollHelper.setEdgeType(AutoScrollHelper.EDGE_TYPE_OUTSIDE);

    }

    protected void startAutoScroll() {
        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                forceScroll();
            }
        }, 1000);

    }

    @SuppressLint("Recycle")
    protected void forceScroll() {
        MotionEvent event = MotionEvent.obtain(System.currentTimeMillis(),
                System.currentTimeMillis(), MotionEvent.ACTION_MOVE,
                listView.getX(), -1, 0);
        mScrollHelper.onTouch(listView, event);
        event.recycle();
    }


    protected TwoWayView.OnScrollListener onScrollListener = new TwoWayView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(TwoWayView view, int scrollState) {
            // TODO Auto-generated method stub
            switch (scrollState) {
                case SCROLL_STATE_IDLE:
                    if (!mActionDown) {
                        forceScroll();
                    }
                    break;
            }
        }

        @Override
        public void onScroll(TwoWayView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            // TODO Auto-generated method stub
        }
    };




}
