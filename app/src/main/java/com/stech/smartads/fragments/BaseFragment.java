package com.stech.smartads.fragments;

import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;

import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.stech.smartads.R;
import com.stech.smartads.activities.BaseActivity;
import com.stech.smartads.activities.MainActivity;
import com.stech.smartads.components.exoplayer.ExoMediaPlayer;
import com.stech.smartads.components.exoplayer.ListenerPlayer;
import com.stech.smartads.components.network.NetworkUtility;
import com.stech.smartads.config.AppConfigs;
import com.stech.smartads.core.AppData;
import com.stech.smartads.core.MainApplication;
import com.stech.smartads.config.Constants;
import com.stech.smartads.models.DataContentObj;
import com.stech.smartads.models.LayoutFrameObj;
import com.stech.smartads.models.ServerSetting;
import com.stech.smartads.utils.CacheManager;
import com.stech.smartads.utils.CommonUtil;
import com.stech.smartads.utils.DateTimeUtil;
import com.stech.smartads.utils.FileUtility;
import com.stech.smartads.utils.StringUtil;
import com.stech.smartads.components.PDFViewer;
import java.io.File;
import java.io.IOException;
import java.util.List;

import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.VideoView;

import im.delight.android.webview.AdvancedWebView;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseFragment extends Fragment {

    protected AppCompatActivity self;
    protected static final String PARAM_DATA = "data";
    protected LayoutFrameObj layoutFrame;

    protected BaseActivity activity;
    protected GestureDetector gdt;

    protected ImageView imgBackground;
    protected boolean isLoading = false;

    protected long latestReloadTime = 0;
    protected int autoReFreshPageTime = 0;

    // protected SimpleExoPlayerView videoView;
    protected SimpleExoPlayerView videoView;
    protected ExoMediaPlayer videoPlayer;

    protected ImageView imageView;
    protected AdvancedWebView webView;
    protected PDFView pdfView;

    protected VideoView livestreamView;

    protected TextView hiddenTextView;

    protected int screenHeight;
    protected int screenWidth;

    public Application getApplication(){
       return self.getApplication();
    }

    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        self = (AppCompatActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (gdt == null)
            gdt = new GestureDetector(new GestureListener());

        View view = inflateLayout(inflater, container, savedInstanceState);

        initUI(view);
        initControl();

        return view;
    }

    abstract View inflateLayout(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    abstract void initUI(View view);

    abstract void refreshData(LayoutFrameObj data);

    protected void initControl() {
        startAudioService();
    }

    protected void startAudioService(){
        if(this.layoutFrame!=null && this.layoutFrame.isAvailableSongs() && ((MainApplication)getApplication()).isMusicServiceAvailable() ){
            ((MainActivity)getActivity()).startAudioService(layoutFrame.getAudios());
        }
    }

    protected void setFragment(Fragment fragment, int container) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(container, fragment);
        CommonUtil.commitTransaction(transaction);
        //transaction.commit();
    }

    public LayoutFrameObj getLayoutFrame() {
        return layoutFrame;
    }

    public void setLayoutFrame(LayoutFrameObj data) {
        layoutFrame = data;

        Bundle bundle = new Bundle();
        bundle.putParcelable(PARAM_DATA, data);
        setArguments(bundle);
    }


    public void setBaseActivity(BaseActivity obj) {
        this.activity = obj;
    }

    public BaseActivity getBaseActivity() {
        if (layoutFrame != null && layoutFrame.getActivity() != null) {
            return layoutFrame.getActivity();
        }
        return this.activity;
    }

    public void setImageView(ImageView image) {
        imageView = image;
        imageView.bringToFront();
        imageView.setClickable(true);
    }

    public VideoView getLivestreamView() {
        return livestreamView;
    }

    public void setLivestreamView(VideoView video) {
        livestreamView = video;
        livestreamView.bringToFront();
        livestreamView.setClickable(true);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void gotoWebScreen(String url) {
        BaseActivity ctx = getBaseActivity();
        if (ctx != null)
            ctx.gotoWebScreen(url);
    }

    public void goBack() {
        BaseActivity ctx = getBaseActivity();
        if (ctx != null)
            ctx.goBack();
    }

    public void switchAppMode(String mode) {
        BaseActivity ctx = getBaseActivity();
        if (ctx != null)
            ctx.switchAppMode(mode);
    }

//    public GestureDetector.SimpleOnGestureListener createGestureDetector() {
//        return
//    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return onSingleClick(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return onDoubleClick(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
        {
            return onSwipe(e1, e2, velocityX, velocityY);
        }
    }

    public boolean onClick(MotionEvent e) {
        return true;
    }

    public boolean onSingleClick(MotionEvent e) {
        return true;
    }

    public boolean onDoubleClick(MotionEvent e) {
        return true;
    }

    public boolean onSwipe(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        return true;
    }

    protected ImageView initImageView(ImageView imgView) {
        if (AppData.getInstance().getServerSetting().getVideoFillMode() == Constants.FILL_MODE_FULL_SCREEN) {
            imgView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            imgView.setScaleType(ImageView.ScaleType.FIT_XY);
        } else if (AppData.getInstance().getServerSetting().getVideoFillMode() == Constants.FILL_MODE_FIT_SCREEN) {
            imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            imgView.setAdjustViewBounds(true);
        }

        return imgView;
    }

    protected VideoView initLiveStreamView(final VideoView mVideoview) {
        mVideoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            // Close the progress bar and play the video
            public void onPrepared(MediaPlayer mp) {
                mVideoview.start();
            }
        });

        mVideoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {

            }
        });
        return mVideoview;
    }

    protected SimpleExoPlayerView initVideoView(SimpleExoPlayerView videoView) {
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
        return videoView;
    }

    protected WebView initWebview() {
        return initWebview(this.webView);
    }

    protected WebView initWebview(WebView mWv) {
        if (mWv == null)
            return null;

        mWv.getSettings().setAppCacheMaxSize( 15 * 1024 * 1024 ); // 15MB
        mWv.getSettings().setAppCachePath(CacheManager.getCacheFolder());
        mWv.getSettings().setDatabasePath(getApplication().getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath());

        mWv.getSettings().setAllowFileAccess( true );
        mWv.getSettings().setAppCacheEnabled( true );
        mWv.getSettings().setJavaScriptEnabled( true );

        mWv.getSettings().setAllowContentAccess(true);

        mWv.getSettings().setDatabaseEnabled(true);
        mWv.getSettings().setDomStorageEnabled(true);
        mWv.getSettings().setDefaultTextEncodingName("utf-8");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            //mWv.getSettings().setDatabasePath("/data/data/" + mWv.getContext().getPackageName() + "/" + AppConfigs.LOCAL_DATABASE + "/");
            mWv.getSettings().setDatabasePath(getApplication().getApplicationContext().getDir(AppConfigs.LOCAL_DATABASE, Context.MODE_PRIVATE).getPath());
        }

        if (AppConfigs.CACHE_WEBSITE && !NetworkUtility.getInstance(getContext()).isNetworkAvailable() ) { // loading offline
            mWv.getSettings().setCacheMode( WebSettings.LOAD_CACHE_ELSE_NETWORK );
        } else {
            mWv.getSettings().setCacheMode( WebSettings.LOAD_DEFAULT ); // load online by default
        }

        // Allow Zoom
        mWv.getSettings().setSupportZoom(true);
        mWv.getSettings().setBuiltInZoomControls(true);
        mWv.getSettings().setLoadsImagesAutomatically(true);
        mWv.getSettings().setDisplayZoomControls(false);

        //mWv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWv.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        mWv.getSettings().setPluginState(WebSettings.PluginState.ON);
        mWv.getSettings().setMediaPlaybackRequiresUserGesture(false);//kiemdv
        mWv.getSettings().setLightTouchEnabled(true);

        mWv.getSettings().setLoadWithOverviewMode(true); // youtube ?


        mWv.getSettings().setSavePassword(false);
        mWv.getSettings().setSaveFormData(false);
        //mWv.getSettings().setUseWideViewPort(true);

        //mWv.onCreateInputConnection()
        mWv.requestFocus(View.FOCUS_DOWN);
        mWv.setFocusableInTouchMode(true);
        mWv.setFocusable(true);
        mWv.requestFocusFromTouch();

        mWv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE :  break;
                    case MotionEvent.ACTION_CANCEL :  break;
                }
                return false;
            }
        });

        mWv.setWebViewClient(new WebViewClient() {
            private boolean alertVisiblity = false;

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.isEmpty())
                    return false;

                //showMessage(url);
                //if call native screen from webView
                if (url.contains("@") || url.endsWith("/" + Constants.APP_MODE_API) || url.endsWith("/" + Constants.APP_MODE_HOMEPAGE)) {
                    //showMessage(url);
                    if (url.contains("@"))
                        url = url.substring(url.lastIndexOf("@") + 1);
                    else if (url.endsWith("/" + Constants.APP_MODE_HOMEPAGE))
                        url = Constants.APP_MODE_HOMEPAGE;
                    else if (url.endsWith("/" + Constants.APP_MODE_API))
                        url = Constants.APP_MODE_API;

                    switchAppMode(url);
                    return false;
                }

                if ( NetworkUtility.getInstance(self).isOnline() || !AppConfigs.CACHE_WEBSITE) {
                    checkAutoRefresh(url);

                        if(url.startsWith("intent")) {
                            try {
                            // fetching the part that starts with http // https
                            int startIndex,endIndex;
                            startIndex=url.indexOf("=")+1;
                            endIndex=url.indexOf("#");
                            url=url.substring(startIndex,endIndex); // this url will open the playStore but wait !

                            // the url we formed still contains some problem at "details?id%3Dcom" bcoz it must be "details?id=com"
                            url=url.replace("%3D","=");
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(intent);
                            } catch (ActivityNotFoundException ex) {
                                showMessage("Url not found");
                            }
                        }


                    if (DateTimeUtil.getCurrentTime(DateTimeUtil.SECOND) - latestReloadTime >= autoReFreshPageTime) {
                        loadUrl(view, url);
                        return false;
                    }
                } else {

//                    view.loadUrl("file:///" + Environment.getExternalStorageDirectory()
//                            + File.separator+ FileUtility.getCachedFileNameFromURL(url));
                    view.loadUrl(Constants.PROTOCOL_FILE + "/" + getApplication().getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath()
                            + File.separator + FileUtility.getCachedFileNameFromURL(url));
                }

                return false;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
               // Log.e("load wv error: ", "");
                CommonUtil.log("WEBVIEW", "Lỗi  webView http");

                if (imgBackground != null) {
                    imgBackground.setVisibility(View.VISIBLE);

                    if (!AppConfigs.isDebug) {
                        imgBackground.setVisibility(View.VISIBLE); //Hung: nếu là debug mode thì show màn lỗi, không cần show imgBackground frienly
                    }
                }
                //Hung:PLAY_VIDEO_IN_WEB: Không phải lỗi nào cũng cần reload --> can check them loi kieu gi thi moi reload ??
                // if (NetworkUtility.getInstance(self).isOnline()) {
                //     reloadWebViewWhenError();
                // }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                CommonUtil.log("WEBVIEW","Lỗi tải webView http");

                if (imgBackground != null) {
                    if (!AppConfigs.isDebug) {
                        imgBackground.setVisibility(View.VISIBLE); //Hung: nếu là debug mode thì show màn lỗi, không cần show imgBackground frienly
                    }
                }
                if (NetworkUtility.getInstance(self).isOnline()) {
                    reloadWebViewWhenError();
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                latestReloadTime = DateTimeUtil.getCurrentTime(DateTimeUtil.SECOND);
                isLoading = true;
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                initJavascript(webView);
                super.onPageFinished(view, url);
                view.setVisibility(View.VISIBLE);
                if (imgBackground != null)
                    imgBackground.setVisibility(View.GONE);

                if (AppConfigs.CACHE_WEBSITE)
                    view.saveWebArchive(Environment.getExternalStorageDirectory()
                            + File.separator + FileUtility.getCachedFileNameFromURL(url));

                isLoading = false;

                if (hiddenTextView != null) {
                    hiddenTextView.setFocusable(true);
                    hiddenTextView.requestFocus();
                }

                webView.setVisibility(View.VISIBLE);

                //click on video -> auto play
                if (url.contains("youtube"))
                    clickOnWebView(view);

            }

//            @Override
//            public boolean onCreateOptionsMenu(Menu menu) {
//                // Inflate the menu; this adds items to the action bar if it is present.
//                getMenuInflater().inflate(R.menu.menu_main, menu);
//                return true;
//            }
//
//            @Override
//            public boolean onOptionsItemSelected(MenuItem item) {
//                // Handle action bar item clicks here. The action bar will
//                // automatically handle clicks on the Home/Up button, so long
//                // as you specify a parent activity in AndroidManifest.xml.
//                int id = item.getItemId();
//
//                //noinspection SimplifiableIfStatement
//                if (id == R.id.action_settings) {
//                    return true;
//                }
//
//                return super.onOptionsItemSelected(item);
//            }

        });

//        mWv.setMixedContentAllowed(true);
        mWv.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onReachedMaxAppCacheSize(long spaceNeeded, long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
                quotaUpdater.updateQuota(spaceNeeded * 2);
            }

            @Override
            public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize, long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater)
            {
                quotaUpdater.updateQuota(estimatedSize * 2);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                result.confirm();
                return true;
            }
        });

        mWv.setVisibility(View.INVISIBLE);
        AppData.getInstance(self).setCurrentWebView(mWv);

        return mWv;
    }

    protected void clickOnWebView(final WebView webview) {
        long delta = 100;
        long downTime = SystemClock.uptimeMillis();
        float x = webview.getLeft() + webview.getWidth()/2; //in the middle of the webview
        float y = webview.getTop() + webview.getHeight()/2;

        final MotionEvent downEvent = MotionEvent.obtain( downTime, downTime + delta, MotionEvent.ACTION_DOWN, x, y, 0 );
        // change the position of touch event, otherwise, it'll show the menu.
        final MotionEvent upEvent = MotionEvent.obtain( downTime, downTime+ delta, MotionEvent.ACTION_UP, x+10, y+10, 0 );

        webview.post(new Runnable() {
            @Override
            public void run() {
                if (webview != null) {
                    webview.dispatchTouchEvent(downEvent);
                    webview.dispatchTouchEvent(upEvent);
                }
            }
        });
    }

    // Inject CSS method: read style.css from assets folder
    // Append stylesheet to document head
    protected void initJavascript(WebView webView) {
        if (true) // this method has error -> skip for now.
            return;

        String inject = "";
        if (false) {
            inject =
                    "var __head = document.getElementsByTagName('head');" +
                    "var __body = document.getElementsByTagName('body');" +
                    "if (__head) __head.item(0).innerHTML += '<link rel=\"stylesheet\" href=\"../../common/style.css\" />';" +
                    "if (__body) __body.item(0).innerHTML += '<script src=\"../../common/android.js\"></script>'; alert('haha');" ;
        } else {
            String css = FileUtility.readStringFromAsset(getActivity(), "common/style.css", true);
            String js = FileUtility.readStringFromAsset(getActivity(), "common/android.js", true);
            //CommonUtil.message(self, css);
            //CommonUtil.message(self, js);
            inject =
                    "var __head = document.getElementsByTagName('head').item(0);" +
                    "var __body = document.getElementsByTagName('body').item(0);" +

                    "var __style = document.createElement('style');" +
                    "__style.type = 'text/css';" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "__style.innerHTML = window.atob('" + css + "');" +
                    "__head.appendChild(__style)" +
                    "var __js = document.createElement('script');" +
                    "__js.type = 'text/css';" +
                    "__js.innerHTML = window.atob('" + js + "');" +
                    "__body.appendChild(__js);" ;
        }

        initJavascript(webView, inject);
    }

    protected void initJavascript(WebView webView, String js) {
        if (js.isEmpty())
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript("javascript:(function() {" + js + "})()", null);
        } else {
            webView.loadUrl("javascript:(function() {" + js + "})()");
        }
    }

    protected void checkAutoRefresh(String url) {

    }

    protected String getParamValueFromUrl(String url, String paramKey){
        Uri uri = Uri.parse(url);
        return uri.getQueryParameter(paramKey);
    }

    protected void showWebContent(WebView mWv, List<DataContentObj> data){
        if (data == null || data.size()==0) return;
        showWebContent(mWv, data.get(0));
    }

    protected void showWebContent(WebView mWv, DataContentObj displayContent) {
        if (isLoading || displayContent == null) return;

        AppData.getInstance(self).setCurrentWebView(mWv);

        String url = displayContent.getUrl();
        String content = displayContent.getDescription();
        String type = displayContent.getDataType();

        if (!url.isEmpty()) {
            url = StringUtil.convertToUrl(url, self);

            checkAutoRefresh(url);
            if (type.equals(Constants.TYPE_IMAGE) || type.equals(Constants.TYPE_VIDEO)) {
                content = StringUtil.convertToHtml(url, type);
                mWv.loadData(content,"text/html", "UTF-8");
            } else {
//              String html = "<html><h1>Hahaha</h1></html>";
//              html = Base64.encodeToString(html.getBytes(), Base64.NO_PADDING);
//              mWv.loadDataWithBaseURL("file:///android_asset/js/", html, "text/html", "UTF-8", null);

                mWv.addJavascriptInterface(AppData.getInstance(self), Constants.PARAM_APP_MAIN_DATA);
                loadUrl(mWv, url);
            }

        } else {

            content = StringUtil.convertToHtml(content, type);
            mWv.addJavascriptInterface(AppData.getInstance(self), Constants.PARAM_APP_MAIN_DATA);

            mWv.loadData(content,"text/html", "UTF-8");
        }
    }

    protected void loadUrl(WebView mWv, String url) {
        if (url.contains(AppConfigs.ASSET_FOLDER) && !url.startsWith(Constants.PROTOCOL_FILE))
            url = Constants.PROTOCOL_FILE + url;
        //mWv.loadDataWithBaseURL(null,  url, "text/html", "utf-8", null);
        mWv.loadUrl(url);
    }

    protected void showPDFContent(PDFView pdfView, DataContentObj displayContent) {
        if (displayContent == null) return;

        String url = displayContent.getUrl();

        if (!url.isEmpty()) {
            url = StringUtil.convertToUrl(url, self);
            new PDFViewer().execute(url);

        } else {
            //CommonUtil.message(self, Constants.TEXT_DOWNLOAD_DATA + " " + Constants.ERR);
        }
    }

    protected void runJavascript(WebView webView, String js) {
        webView.loadUrl("Javascript:" + js);
    }

    protected void reloadWebViewWhenError() {
    }

    protected void hideVideoView() {
        ((MainApplication)getApplication()).setVideoPlaying(false);
        if (videoView != null) {
            videoView.setVisibility(View.INVISIBLE);
            videoPlayer.pause();
        }
    }

    protected void hideWebview() {
        isLoading = false;
        if (webView != null) {
            webView.setVisibility(View.INVISIBLE);
        }
    }

    protected void hideLiveView() {
        isLoading = false;
        if (livestreamView != null) {
            livestreamView.stopPlayback();
            livestreamView.setVisibility(View.INVISIBLE);
        }
    }

    protected void hidePDFview() {
        isLoading = false;
        if (pdfView != null) {
            pdfView.setVisibility(View.INVISIBLE);
        }
    }

    protected void hideImageView() {
        if (imageView != null)  {
            imageView.setVisibility(View.INVISIBLE);
        }
    }

    protected void releasePlayer() {
        ((MainApplication)getApplication()).setVideoPlaying(false);

        if (videoPlayer != null) {
            videoPlayer.pause();
            videoPlayer.release();
        }
    }


    protected void showImageContent(String url) {
        if (url.startsWith("/"))
            url = "file://" + url;

        if (AppData.getInstance().getServerSetting().getVideoFillMode() == Constants.FILL_MODE_FIT_SCREEN) {
            Glide.with(self).load(url).error(R.mipmap.ic_launcher)
                    //.override(screenWidth, screenHeight)
                    .fitCenter()
                    //.crossFade()
                    .into(imageView);
//                            Picasso.with(self).load(url)
//                                    .resize(screenWidth, screenHeight)
//                                    .onlyScaleDown()
//                                    .error(R.mipmap.ic_launcher)
//                                    //.placeholder(R.drawable.placeholder)
//                                    .centerInside()
//                                    .noPlaceholder()
//                                    .into(imageView); //
        } else if (AppData.getInstance().getServerSetting().getVideoFillMode() == Constants.FILL_MODE_FULL_SCREEN) {
            Glide.with(self).load(url).error(R.mipmap.ic_launcher)
                    //.override(screenWidth, screenHeight)
                    .fitCenter()
                    //.crossFade()
                    .into(imageView);
//                            Picasso.with(self).load(url)
//                                    //.resize(2048, 1600) // remove: Fit cannot be used with resize.
//                                    .resize(screenWidth, screenHeight)
//                                    .onlyScaleDown()
//                                    .error(R.mipmap.ic_launcher)
//                                    //.placeholder(R.drawable.placeholder)
//                                    //.fit() // remove: Fit cannot be used with resize.
//                                    .noPlaceholder()
//                                    .onlyScaleDown()
//                                    .into(imageView); //
        } else if (AppData.getInstance().getServerSetting().getVideoFillMode() == Constants.FILL_MODE_CENTER_CROP) {
            Glide.with(self).load(url).error(R.mipmap.ic_launcher)
                    //.override(screenWidth, screenHeight)
                    .centerCrop()
                    //.crossFade()
                    .into(imageView);
//                            Picasso.with(self).load(url)
//                                    .resize(screenWidth, screenHeight)
//                                    .onlyScaleDown()
//                                    .error(R.mipmap.ic_launcher)
//                                    //.placeholder(R.drawable.placeholder)
//                                    .centerCrop()
//                                    .noPlaceholder()
//                                    .onlyScaleDown()
//                                    .into(imageView); //
        }
    }

    protected void showLiveStreamContent(String url) {
        try {
//            MediaPlayer mp = new MediaPlayer();
//            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mp.setDataSource(url);
//            mp.prepareAsync();
//            mp.start();

            // Start the MediaController
            MediaController mediacontroller = new MediaController(getContext());
            mediacontroller.setAnchorView(livestreamView);
            // Get the URL from String VideoURL
            Uri mVideo = Uri.parse(url);
            //videoView.setMediaController(mediacontroller);
            livestreamView.setVideoURI(mVideo);
            livestreamView.start();

        } catch (Exception e) {
            CommonUtil.error(e);
        }
    }

    protected void showVideoContent(ExoMediaPlayer videoPlayer, String url) {
        videoPlayer.resume();
        videoPlayer.play(url, AppConfigs.AUTO_LOOP_VIDEO); //loop video when only 1 video in list
        ((MainApplication) getApplication()).setVideoPlaying(true);
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

    protected void checkVideo() {
        if (videoPlayer != null)
            videoPlayer.resume();
    }

    protected void hideViews() {
        hideVideoView();
        hideImageView();
        hideWebview();
        hideLiveView();
        hidePDFview();
    }

    protected void startView(View view) {
        if (view != null) {
            hideViews();
            view.setVisibility(View.VISIBLE);
            view.bringToFront();
        }
    }

    protected void showMessage(String msg) {
        CommonUtil.message(self, msg);
    }

    protected ServerSetting Setting() {
        return AppData.getInstance(self).getServerSetting();
    }
    protected AppData AppData() { return AppData.getInstance(self); }
}
