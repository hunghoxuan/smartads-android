package com.stech.smartads.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.stech.smartads.R;
import com.stech.smartads.core.MainApplication;
import com.stech.smartads.config.AppConfigs;
import com.stech.smartads.components.exoplayer.ExoMediaPlayer;
import com.stech.smartads.components.exoplayer.ListenerPlayer;
import com.stech.smartads.models.DataContentObj;
import com.stech.smartads.models.LayoutFrameObj;
import com.stech.smartads.utils.DateTimeUtil;
import com.stech.smartads.utils.CommonUtil;
import com.stech.smartads.utils.LocalBroadCastUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends BaseFragment {

    private static final String TAG = VideoFragment.class.getSimpleName();

    private static final String PARAM_URL = "videoUrl";


    private List<DataContentObj> data = new ArrayList<>();
    private int currentIndex = 0;
    private DataContentObj currentData;
    private int currentDisPlayCount = 0; //check for type number;
    private long nextVideoTime = 0; // check for type time

    public VideoFragment() {
        // Required empty public constructor
    }

    public static VideoFragment getInstance(LayoutFrameObj data) {
        VideoFragment fragment = new VideoFragment();
        fragment.setLayoutFrame(data);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(PARAM_DATA)) {
            this.layoutFrame = bundle.getParcelable(PARAM_DATA);

            if(layoutFrame!=null) {
                data = layoutFrame.getData();
                currentIndex = 0;
            }
            //CommonUtil.log(TAG, "Start Fragment");
            //start broadcast service
            LocalBroadCastUtil.registerBroadCast(getActivity(), new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(videoPlayer!=null) {
                        releasePlayer();
                    }
                }
            }, LocalBroadCastUtil.ACTION_STOP_VIDEO_PLAYER);
        }
    }

    @Override
    View inflateLayout(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    @Override
    void initUI(View view) {
        // Grabs a reference to the mPlayer view
        initVideoView(videoView);

        // Should call this methods at the end of declaring UI
        checkVideo();
    }

    @Override
    void refreshData(LayoutFrameObj data) {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoPlayer != null) {
            videoPlayer.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoPlayer != null) {
            videoPlayer.pause();
        }
    }

    @Override
    public void onDetach() {
        releasePlayer();
        super.onDetach();

    }

    @Override
    public void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }

    private void playVideo(boolean isNext) {

        if(data.size()==0)
        {
            return;
        }

        if(!isNext) {
            currentIndex = 0;

        } else {
            if (currentIndex == data.size()-1)
            {
                currentIndex = 0;
            } else {
                currentIndex++;
            }
        }

        //check video
        currentData = data.get(currentIndex);
        String url = currentData.getPlayUrl(self);
        String type = currentData.getDataType();

        if (url != null && url.trim().isEmpty()) {
            if(currentIndex < data.size()-1) {
                playVideo(true);
            }
            return;
        } else {
            CommonUtil.error(TAG, "Play Video #" +  currentIndex + ". File " + url + " [" + currentData.getDataType() + "]");
            videoPlayer.play(url, AppConfigs.AUTO_LOOP_VIDEO); //loop video when only 1 video in list
            ((MainApplication) getApplication()).setVideoPlaying(true);
        }

        //CommonUtil.log(TAG,"Load Video Url : " + url);
        if (currentData.getDisplayType().equals(DataContentObj.DISPLAY_TYPE_NUMBER)) {
             currentDisPlayCount = 1;
        } else if (currentData.getDisplayType().equals(DataContentObj.DISPLAY_TYPE_TIME)) {
            int currentVideoDuration = currentData.getDisplayDuration();

            //CommonUtil.log(TAG, "currentVideoDuration: " + currentVideoDuration);
            if (currentVideoDuration > 0) {
                nextVideoTime = DateTimeUtil.getCurrentTime(((MainApplication)getApplication()).getCurrentCalendar(),DateTimeUtil.SECOND) + currentVideoDuration;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playVideo(true);
                    }
                },currentVideoDuration*1000);
            }
            //CommonUtil.log(TAG, "nextVideoTime: " + DateTimeUtil.convertTimeStampToDate(nextVideoTime * 1000, "HH:mm:ss"));

        } else {
            currentDisPlayCount = 0;
            nextVideoTime = 0;
        }
    }

    protected void checkVideo() {
        if(currentData!=null && !currentData.getDisplayType().isEmpty()){
            if(currentData.getDisplayType().equals(DataContentObj.DISPLAY_TYPE_NUMBER)){
                if(currentDisPlayCount >= currentData.getDisplayDuration()){
                    playVideo(true);
                }else{
                    currentDisPlayCount++;
                    videoPlayer.resume();
                }
            } else if (currentData.getDisplayType().equals(DataContentObj.DISPLAY_TYPE_TIME)){
                //CommonUtil.log(TAG, "nextVideoTime on checkDisplayOfCurrentVideo: " + nextVideoTime);
                if(DateTimeUtil.getCurrentTime(((MainApplication)getApplication()).getCurrentCalendar(),DateTimeUtil.SECOND)<nextVideoTime) {
                    videoPlayer.resume();
                } else {
                    playVideo(true);
                }
            }
        } else {
            playVideo(true);
        }
    }

    protected void processPlayerStateChanged(int playbackState) {
        switch (playbackState) {
            case SimpleExoPlayer.STATE_BUFFERING:
                break;
            case SimpleExoPlayer.STATE_READY:

                break;
            case SimpleExoPlayer.STATE_ENDED:

                ((MainApplication)getApplication()).setVideoPlaying(false);

                checkVideo();
                break;
            case SimpleExoPlayer.STATE_IDLE:
                break;
        }
    }
}
