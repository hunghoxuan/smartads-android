package com.stech.smartads.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.daimajia.slider.library.SliderLayout;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.stech.smartads.core.MainApplication;
import com.stech.smartads.core.AppData;
import com.stech.smartads.utils.PacketUtility;
import com.stech.smartads.R;
import com.stech.smartads.config.AppConfigs;
import com.stech.smartads.components.exoplayer.ExoMediaPlayer;
import com.stech.smartads.components.exoplayer.ListenerPlayer;
import com.stech.smartads.config.Constants;
import com.stech.smartads.components.network.NetworkUtility;
import com.stech.smartads.models.DataContentObj;
import com.stech.smartads.models.LayoutFrameObj;
import com.stech.smartads.utils.CommonUtil;
import com.stech.smartads.utils.DateTimeUtil;
import com.stech.smartads.utils.LocalBroadCastUtil;

import java.util.ArrayList;
import java.util.List;

import im.delight.android.webview.AdvancedWebView;

/**
 * A simple {@link Fragment} subclass.
 */
public class SlideFragment extends BaseFragment {

    private static final String TAG = SlideFragment.class.getSimpleName();

    //private GestureDetector gdt;
    private static final int MIN_SWIPPING_DISTANCE = 80;
    private static final int MIN_ZOOM_DISTANCE = 30;
    private static final int THRESHOLD_VELOCITY = 50;

    private List<DataContentObj> data = new ArrayList<>();
    private int currentIndex = -1;
    private DataContentObj currentData;
    private DataContentObj currentContent;

    private int currentDisPlayCount = 0; //check for type number;
    private long nextVideoTime = 0; // check for type time

    private RelativeLayout frameLayout;

    private View currentView;
    private Handler handler;

    private boolean isPaused = false;
    private boolean isTouching = false;

    public SlideFragment() {
        // Required empty public constructor
    }

    public static SlideFragment getInstance(LayoutFrameObj data) {
        SlideFragment fragment = new SlideFragment();
        fragment.setLayoutFrame(data);
        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Bundle bundle = getArguments();

            if (bundle != null && bundle.containsKey(PARAM_DATA)) {
                this.layoutFrame = bundle.getParcelable(PARAM_DATA);

                if (layoutFrame != null) {
                    data = layoutFrame.getData();
                    currentIndex = -1;
                }
                //CommonUtil.log(TAG, "Start Fragment");
                //start broadcast service
                LocalBroadCastUtil.registerBroadCast(getActivity(), new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (videoPlayer != null) {
                            releasePlayer();
                        }
                    }
                }, LocalBroadCastUtil.ACTION_STOP_VIDEO_PLAYER);
            }

            //Mint.initAndStartSession(this.getApplication(), AppData.getInstance().getServerSetting().getMintAPIKey());
        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }

    @Override
    View inflateLayout(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_slide, container, false);
        } catch (Exception ex) {
            CommonUtil.error(self,  ex);
            return null;
        }
    }

    @Override
    void initUI(View view) {
        screenHeight  = PacketUtility.getScreenHeight() ;
        screenWidth = PacketUtility.getScreenWidth();

        frameLayout = (RelativeLayout) view.findViewById(R.id.fr_main);

        // show Webview
        webView = (AdvancedWebView) view.findViewById(R.id.webview);
        initWebview(webView);

        //imageView -> show image
        imageView = (ImageView) view.findViewById(R.id.img_view);
        initImageView(imageView);
        setEvents(imageView);

        //videoView -> show livestream
        livestreamView = (VideoView) view.findViewById(R.id.live_view);
        initLiveStreamView(livestreamView);
        setEvents(livestreamView);

        //pdf View
        pdfView = (PDFView) view.findViewById(R.id.idPDFView);
        setEvents(pdfView);

        hiddenTextView = view.findViewById(R.id.hiddenTextView);

        // show video
        videoView = (SimpleExoPlayerView) view.findViewById(R.id.video_view);
        setEvents(videoView);

        videoPlayer = new ExoMediaPlayer.Builder().setSimpleExoPlayerView(videoView, false).setListener(new ListenerPlayer() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                processPlayerStateChanged(playbackState);
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                CommonUtil.error(self, error);
            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        }).build(self);

        if (AppData.getInstance().getServerSetting().getVideoFillMode() == Constants.FILL_MODE_FULL_SCREEN) {
            videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);

        } else if (AppData.getInstance().getServerSetting().getVideoFillMode() == Constants.FILL_MODE_FIT_SCREEN) {
            videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            //videoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        }

        // Should call this methods at the end of declaring UI
        checkDisplayOfCurrentVideo();
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
        getHandler().removeCallbacksAndMessages(null);
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


    private void setEvents(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentContent != null && currentContent.getDataType() == Constants.TYPE_GALLERY) {
                    if (isTouching) {
                        return;
                    }
                    pauseSlide();
                }
            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if (gdt == null)
//                    gdt = new GestureDetector(new GestureListener());
                if (gdt != null)
                    gdt.onTouchEvent(motionEvent);

                return false;
            }
        });
    }

    private void pauseSlide() {
        isPaused = !isPaused;
        if (isPaused) {
            onPause();
        } else if (!isPaused) {
            onResume();
            playSlide();
        }
    }

    private void playSlide()  {
        playSlide(false);
    }

    private void playSlide(boolean isNext)  {
        if (isNext && data.size() > 0) {
            if (currentIndex == data.size() - 1) {
                currentIndex = 0;
            } else {
                currentIndex++;
            }
        } 
        playSlide(currentIndex);
    }

    private Handler getHandler() {
        if (handler == null)
            handler = new Handler();
        return handler;
    }

    private void playSlide(int index) {
        try {
            try {
                //remove existing timer of previous slides
                getHandler().removeCallbacksAndMessages(null);
            } catch (Exception ex) {
                /* ignore */
                CommonUtil.error(getActivity(), ex);
            }

            if  (isPaused)
                return;;
                
            if (data.size() == 0) {
                if (imageView != null) {
                    startView(imageView);
                }
                return;
            }
            
            if (index < 0)
                currentIndex = data.size() - 1;
            else if (index > data.size() -  1)
                currentIndex = 0;
            else
                currentIndex = index;

            //check video
            currentData = data.get(currentIndex);
            currentContent = currentData;

            String url = currentData.getPlayUrl(self); // get cached url
            String type = currentData.getDataType();

            boolean isOnline = NetworkUtility.getInstance(self).isOnline();
            endView();

            // test livestream
            // url = "http://techslides.com/demos/sample-videos/small.mp4";
            // url = "rtsp://172.30.1.234:18554/camera1";
            // type = Constants.TYPE_LIVESTREAM;

            if (type.equals(Constants.TYPE_IMAGE)) {
                if (url != null && url.trim().isEmpty()) {
                    playSlide(true);
                    return;
                }

                if (isOnline && AppData.getInstance().getServerSetting().getUseWebViewToShowImage()) {
                    startView(webView);
                    showWebContent(webView, currentData);
                    //HUng: hien thi WebView o trong Fragment
                    // CommonUtil.error(TAG, "IMAGE:" + url + " [" + currentData.getDataType() + "]");
                } else {
                    try {
                        startView(imageView);
                        showImageContent(url);
                        // CommonUtil.error(TAG, "Image: " + url + " [ Duration: " + currentData.getDisplayDuration() + ". Type: " + currentData.getDataType() + " ]");
                    } catch (Exception ex) {
                        CommonUtil.error(getContext(), ex);
                        playSlide(true);
                        return;
                    }
                }

            } else if (currentData.getUrl().toLowerCase().endsWith(".pdf")) {
                startView(pdfView);
                showPDFContent(pdfView, currentData);

                //HUng: hien thi WebView o trong Fragment
                // CommonUtil.error(TAG, "PDF:" + url + " [" + currentData.getDataType() + "]");

            } else if (type.equals(Constants.TYPE_HTML) || type.equals(Constants.TYPE_TEXT)  || type.equals(Constants.TYPE_SLIDE)  || type.equals(Constants.TYPE_URL)) {
                url = currentData.getUrl();

                if (url != null && !url.isEmpty() && url.startsWith("http") && !NetworkUtility.getInstance(self).isOnline()) {
                    playSlide(true);
                }
                startView(webView);
                showWebContent(webView, currentData);

                //HUng: hien thi WebView o trong Fragment
                // CommonUtil.error(TAG, "HTML:" + url + " [" + currentData.getDataType() + "]");

            } else if (type.equals(Constants.TYPE_LIVESTREAM)) {

                if (url != null && url.trim().isEmpty()) {
                    playSlide(true);
                    return;
                }
                try {
                    startView(videoView);
                    showLiveStreamContent(url);
                    // CommonUtil.error(TAG, "LiveStream: " + url + " [ Duration: " + currentData.getDisplayDuration() + ". Type: " + currentData.getDataType() + " ]");
                } catch (Exception ex) {
                    CommonUtil.error(getContext(), ex);
                    playSlide(true);
                    return;
                }

            } else {
                if (url != null && url.trim().isEmpty()) {
                    playSlide(true);
                    return;
                }

                if (isOnline && AppData.getInstance().getServerSetting().getUseWebViewToShowImage()) {
                    startView(webView);
                    showWebContent(webView, currentData);

                    //HUng: hien thi WebView o trong Fragment
                    // CommonUtil.error(TAG, "IMAGE:" + url + " [" + currentData.getDataType() + "]");

                } else {
                    startView(videoView);
                    showVideoContent(videoPlayer, url);

                    // CommonUtil.error(TAG, "VIDEO: " + url + " [ Duration: " + currentData.getDisplayDuration() + ". Type: " + currentData.getDataType() + " ]");
                }
            }

            //CommonUtil.log(TAG,"Load Video Url : " + url);
            if (currentData.getDisplayType().equals(DataContentObj.DISPLAY_TYPE_NUMBER)) {
                currentDisPlayCount = 1;
            } else if (currentData.getDisplayType().equals(DataContentObj.DISPLAY_TYPE_TIME)) {
                int currentDuration = currentData.getDisplayDuration();
                if (currentDuration <= 0)
                    currentDuration = 5;

                if (currentDuration > 0) {
                    nextVideoTime = DateTimeUtil.getCurrentTime(((MainApplication) getApplication()).getCurrentCalendar(), DateTimeUtil.SECOND) + currentDuration;

                    if (data.size() > 1 && !isPaused) {
                        getHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                playSlide(true);
                            }
                        }, (currentDuration + 2) * 1000);
                    }

                } else {
                    playSlide(true); // tiếp tục next slide
                }
                //CommonUtil.log(TAG, "nextVideoTime: " + DateTimeUtil.convertTimeStampToDate(nextVideoTime * 1000, "HH:mm:ss"));

            } else {
                currentDisPlayCount = 0;
                nextVideoTime = 0;
            }
        } catch (Exception exception) {
            CommonUtil.error(getActivity(), exception);
            playSlide(true); // Tiep tuc
        }
    }

    private void checkDisplayOfCurrentVideo() {
        if(currentData!=null && !currentData.getDisplayType().isEmpty()) {
            if (currentData.getDisplayType().equals(DataContentObj.DISPLAY_TYPE_NUMBER)) {
                if(currentDisPlayCount >= currentData.getDisplayDuration()) {
                    playSlide(true);
                }else{
                    currentDisPlayCount++;
                    videoPlayer.resume();
                }
            } else if (currentData.getDisplayType().equals(DataContentObj.DISPLAY_TYPE_TIME)) {
                CommonUtil.log("VIDEO", "nextVideoTime on checkDisplayOfCurrentVideo: " + nextVideoTime);
                if (DateTimeUtil.getCurrentTime(((MainApplication)getApplication()).getCurrentCalendar(),DateTimeUtil.SECOND) < nextVideoTime) {
                    videoPlayer.resume();
                } else {
                    playSlide(true);
                }
            }
        } else {
            playSlide(true);
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

                checkDisplayOfCurrentVideo();
                break;
            case SimpleExoPlayer.STATE_IDLE:
                break;
        }
    }

    private void startView() {
        startView(currentView);
    }

    @Override
    protected void startView(View view) {
        super.startView(view);
        if (view != null) {
            currentView = view;
            try {
                Animation animFadeIn = AnimationUtils.loadAnimation(self, R.anim.fade_in);
                view.startAnimation(animFadeIn);
            } catch (Exception ex) {
                CommonUtil.error(getContext(), ex);
                /* ignore */
            }
        }
    }


    private void endView() {
        if (currentView != null) {
            try {
                Animation animFadeIn = AnimationUtils.loadAnimation(self, R.anim.fade_out);
                currentView.startAnimation(animFadeIn);
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        /* just wait */
                    }
                }, (2) * 1000);
            } catch (Exception ex) {
                CommonUtil.error(getContext(), ex);
                /* ignore */
            }
        }
    }

    private void endView(View view) {
        try {
            Animation animFadeIn = AnimationUtils.loadAnimation(self, R.anim.fade_out);
            view.startAnimation(animFadeIn);
        } catch (Exception ex) {
            /* ignore */
        }
    }

    @Override
    public boolean onClick(MotionEvent e) {
        AppData.getInstance(self).setCurrentContent(currentContent);
        AppData.getInstance(self).setCurrentLayoutFrame(layoutFrame);
        return true;
    }

    @Override
    public boolean onSingleClick(MotionEvent e) {
        if (data.size() == 0) {
            if (imageView != null)
                return imageView.performClick();
            return false;
        }

        onClick(e);

        String description = currentContent.getDescription().trim();

        if (!description.isEmpty()) {
            if (description.toLowerCase().startsWith("http") || !description.contains(" ")) {
                gotoWebScreen(description);
                return true;
            }
        } else {
            gotoWebScreen(AppConfigs.DEFAULT_CONTENT_APP);
            return true;
        }

        if (currentContent != null && currentContent.getDataType() != Constants.TYPE_GALLERY) {
            pauseSlide();
        }
        return super.onSingleClick(e);
    }

    @Override
    public boolean onDoubleClick(MotionEvent e) {
        onClick(e);

        if (currentContent != null && currentContent.getDataType() != Constants.TYPE_GALLERY) {
            pauseSlide();
        }
        return super.onDoubleClick(e);
    }

    @Override
    public boolean onSwipe(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        if ((e1.getX() - e2.getX() > MIN_SWIPPING_DISTANCE) && (Math.abs(e1.getY() - e2.getY())  < MIN_ZOOM_DISTANCE) && Math.abs(velocityX) > THRESHOLD_VELOCITY)
        {
            isPaused = false;
            isTouching = true;

            if (currentIndex < data.size() - 1)
                currentIndex++;
            else
                currentIndex = 0;
            playSlide(currentIndex);
            return false;
        }
        else if ((e2.getX() - e1.getX() > MIN_SWIPPING_DISTANCE) && (Math.abs(e1.getY() - e2.getY())  < MIN_ZOOM_DISTANCE) && Math.abs(velocityX) > THRESHOLD_VELOCITY)
        {
            isPaused = false;
            isTouching = true;
            if (currentIndex > 0)
                currentIndex--;
            else
                currentIndex = data.size() - 1;
            playSlide(currentIndex);
            return false;
        }
        else if ((e1.getY() - e2.getY() > MIN_SWIPPING_DISTANCE) && (Math.abs(e1.getX() - e2.getX())  < MIN_ZOOM_DISTANCE) && Math.abs(velocityY) > THRESHOLD_VELOCITY)
        {
            isPaused = false;
            isTouching = true;
            if (currentIndex == data.size() - 1)
                currentIndex = 0;
            else
                currentIndex = data.size() - 1;
            playSlide(currentIndex);

            return false;
        }
        else if ((e2.getY() - e1.getY() > MIN_SWIPPING_DISTANCE) && (Math.abs(e1.getX() - e2.getX())  < MIN_ZOOM_DISTANCE) && Math.abs(velocityY) > THRESHOLD_VELOCITY)
        {
            isPaused = false;
            isTouching = true;
            if (currentIndex == 0)
                currentIndex = data.size() - 1;
            else
                currentIndex = 0;
            playSlide(currentIndex);
            return false;
        }
        isTouching = false;
        return false;
    }
}
