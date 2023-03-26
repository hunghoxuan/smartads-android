package com.stech.smartads.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.stech.smartads.R;
import com.stech.smartads.core.AppData;
import com.stech.smartads.core.MainApplication;
import com.stech.smartads.models.DataContentObj;
import com.stech.smartads.models.LayoutFrameObj;
import com.stech.smartads.utils.CommonUtil;
import com.stech.smartads.utils.DateTimeUtil;
import com.stech.smartads.utils.LocalBroadCastUtil;

import java.util.List;

import im.delight.android.webview.AdvancedWebView;

/**
 * A simple {@link Fragment} subclass.
 */
public class TextFragment extends BaseFragment {

    private static final String TAG = TextFragment.class.getSimpleName();

    private static final String PARAM_DATA = "data";

    //private AdvancedWebView mWv;

    private ImageView imgBackground;

    private int reloadWebCountInError = 0;
    private boolean isLoading = false;
    private long latestReloadTime = 0;
    private int autoReFreshPageTime = 0;
    private boolean isCheckingAutoRefreshUrl = false;

    private Runnable runnableCheckReloadWebView = new Runnable() {
        @Override
        public void run() {

            if(autoReFreshPageTime == 0) return;

            if (DateTimeUtil.getCurrentTime(DateTimeUtil.SECOND) - latestReloadTime > (autoReFreshPageTime)) {
                CommonUtil.log(TAG, "Auto Refresh Webview !!");
                showWebContent(layoutFrame.getData());
            }

            isCheckingAutoRefreshUrl = true;
            CommonUtil.log(TAG, "Check Webview after each 5 seconds");

            ((MainApplication)getApplication()).getAppHandler().postDelayed(this, 5*1000);
        }
    };

    public TextFragment() {
        // Required empty public constructor
    }

    public static TextFragment getInstance(LayoutFrameObj data) {
        TextFragment fragment = new TextFragment();
        fragment.setLayoutFrame(data);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(PARAM_DATA)) {
            this.layoutFrame = bundle.getParcelable(PARAM_DATA);
        }
    }

    @Override
    View inflateLayout(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_webview, container, false);
    }

    @Override
    void initUI(View view) {

        //default image
        imgBackground = (ImageView) view.findViewById(R.id.imgBackground);
        imgBackground.setVisibility(View.GONE);
        imgBackground.setImageResource(R.drawable.bg_default_screen);

        //android_asset/resources/default_image_1.png

        reloadWebCountInError = 0;

        //webview
        webView = (AdvancedWebView) view.findViewById(R.id.webview);
        initWebview(webView);

//        mWv.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//
//                if (NetworkUtility.getInstance(self).isOnline()) {
//                    checkAutoRefresh(url);
//
//                    if(DateTimeUtil.getCurrentTime(DateTimeUtil.SECOND) - latestReloadTime >= autoReFreshPageTime) {
//                        view.loadUrl(url);
//                        if (AppConfigs.isDebug)
//                            CommonUtil.log(TAG,"load from OverrideUrlLoading : "+url);
//                        return true;
//                    }
//                }
//
//                return false;
//            }
//
//            @Override
//            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
//                super.onReceivedError(view, request, error);
//                Log.e("load wv error: ", "");
//                imgBackground.setVisibility(View.VISIBLE);
//                CommonUtil.log("WEBVIEW", "Lỗi  webview http");
//
//                if (!AppConfigs.isDebug) {
//                    imgBackground.setVisibility(View.VISIBLE); //Hung: nếu là debug mode thì show màn lỗi, không cần show imgBackground frienly
//                }
//                //Hung:PLAY_VIDEO_IN_WEB: Không phải lỗi nào cũng cần reload --> can check them loi kieu gi thi moi reload ??
//                // if (NetworkUtility.getInstance(self).isOnline()) {
//                //     reloadWebViewWhenError();
//                // }
//            }
//
//            @Override
//            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
//                super.onReceivedHttpError(view, request, errorResponse);
//                CommonUtil.log("WEBVIEW","Lỗi tải webview http");
//                if (!AppConfigs.isDebug) {
//                    imgBackground.setVisibility(View.VISIBLE); //Hung: nếu là debug mode thì show màn lỗi, không cần show imgBackground frienly
//                }
//                if (NetworkUtility.getInstance(self).isOnline()) {
//                    reloadWebViewWhenError();
//                }
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//                view.setVisibility(View.VISIBLE);
//                imgBackground.setVisibility(View.GONE);
//                isLoading = false;
//            }
//
//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                super.onPageStarted(view, url, favicon);
//
//                latestReloadTime = DateTimeUtil.getCurrentTime(DateTimeUtil.SECOND);
//                isLoading = true;
//            }
//        });
////        mWv.setMixedContentAllowed(true);
//        mWv.setWebChromeClient(new WebChromeClient(){
//        });
//        // Allow Zoom
//        mWv.getSettings().setSupportZoom(true);
//        mWv.getSettings().setBuiltInZoomControls(true);
//        mWv.getSettings().setLoadsImagesAutomatically(true);
//        mWv.getSettings().setJavaScriptEnabled(true);
//        mWv.getSettings().setDisplayZoomControls(false);
//        mWv.setLayerType(WebView.LAYER_TYPE_NONE, null);
//
//        mWv.getSettings().setAllowFileAccess(true);
//        mWv.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
//
//        //mWv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
//        mWv.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//        mWv.getSettings().setAppCacheEnabled(true);
//        mWv.getSettings().setPluginState(WebSettings.PluginState.ON);
//        mWv.getSettings().setJavaScriptEnabled(true);
//        mWv.getSettings().setMediaPlaybackRequiresUserGesture(false);//kiemdv

        // Should call this methods at the end of declaring UI
        showWebContent(layoutFrame.getData());

    }

    //reload webview khi error. 5 giay/ lan
    protected void reloadWebViewWhenError(){
        ((MainApplication)getApplication()).getAppHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (reloadWebCountInError <5) { //nho hon 5 lan -> load lai content web

                    CommonUtil.log(TAG,"load from error");
                    reloadWebCountInError++;

                    showWebContent(layoutFrame.getData());
                } else {  //lon hon 5 lan -> load lai toan bo schedule

                    LocalBroadCastUtil.sendBroadcastListener(self,LocalBroadCastUtil.ACTION_REFRESH_LAYOUT);
                }
            }
        }, 5*1000);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        ((MainApplication)getApplication()).getAppHandler().removeCallbacks(runnableCheckReloadWebView);

    }

    @Override
    void refreshData(LayoutFrameObj data) {
        this.layoutFrame = data;
        showWebContent(this.layoutFrame.getData());

        //start audio service
        startAudioService();
    }


    private void showWebContent(List<DataContentObj> data){

        if(data.size()==0) return;
        DataContentObj displayContent = data.get(0);

        if(isLoading) return;

        String url = displayContent.getUrl();

        if (!url.isEmpty()) {
            if(!url.startsWith("http")) {
                if (url.startsWith("/")) {
                    url = AppData().getConfiguredAddressIp() + url;
                } else {
                    url = AppData().getConfiguredAddressIp() + "/" + url;
                }
            }

            CommonUtil.log(TAG,"webview url :"+url);
            checkAutoRefresh(url);
            webView.loadUrl(url);

            isLoading = true;

        } else {
            String content = displayContent.getDescription();
            String background = AppData.getInstance().getServerSetting().getBackground();
            String color = AppData.getInstance().getServerSetting().getFontColor();
            String kind = displayContent.getDisplayType();
            //CommonUtil.log("KIND ", kind);
            String[] styles = kind.split(":");

            try {
                background = styles[3];
                color = styles[4];

                String _size = styles[0];
                String _speed = styles[1];
                String _direction = styles[2];
                String _font = styles[5];
                String _height = styles[6];
                String _padding = styles[7];
                String _background = styles[8];
                String _style = styles[9];
                String _scaleX = styles[10];
                String _scaleY = styles[11];
                String _margin = styles[12];
            } catch (Exception ex) {
                /* ignore */
            }

            if (content.startsWith("<html")) {
                /* ignore */
            }  else {
                content = "<html>" +
                        "<style>p.marquee{\n" +
                        "    -webkit-animation-name: marquee;\n" +
                        "    -webkit-animation-timing-function: linear;\n" +
                        "    -webkit-animation-duration:50s;\n" +
                        "    -webkit-animation-iteration-count: infinite;\n" +
                        "    margin: 0;\n" +
                        "    padding: 0;\n" +
                        "    overflow: hidden;\n" +
                        "    display: block;\n" +
                        "    white-space: nowrap;\n" +
                        "}\n" +
                        "\n" +
                        "\n" +
                        "@-webkit-keyframes marquee{\n" +
                        "    0%{\n" +
                        "        text-indent: 95%;\n" +
                        "}\n" +
                        "    100%{\n" +
                        "        text-indent: -20%;  \n" +
                        "    }\n" +
                        "}</style>" +
                        "<body style='background-color: " + background + ";width:100%;height:100%'>" +
                        "<div class='outer' style='display: table;position: absolute;top: 0;left: 0;height: 100%;width: 100%;'>" +
                        "  <div class='middle' style='display: table-cell;vertical-align: middle;'>" +
                        "    <div class='inner' style='color:" + color + ";text-align:center'>" +
                        "       <marquee behavior='scroll'  style='width:auto;font-size:6vh;' scrollamount='10' direction = 'left'>" +
                                    content +
                        "       </marquee>" +
                        //"<p class='marquee' style='font-size:12vh'>" + content + "</p>" +
                        "   </div>" +
                        " </div>" +
                        "</div>" +
                        "</body></html>";
            }
            CommonUtil.error("HTML Content", content);
            webView.loadData(content,"text/html", "UTF-8");
        }

        imgBackground.setVisibility(View.GONE);
    }

    protected void checkAutoRefresh(String url) {

        CommonUtil.log(TAG,"webview check auto_refresh url :"+url);

        String autoRefreshPageValue = getParamValueFromUrl(url, "auto_refresh");
        if(autoRefreshPageValue != null && !autoRefreshPageValue.isEmpty()){
            try {
                long autoReFreshPageLongValue = Long.parseLong(autoRefreshPageValue);
                autoReFreshPageTime = (int) (autoReFreshPageLongValue/1000);

                if(autoReFreshPageTime > 0 && !isCheckingAutoRefreshUrl) {
                    //add the new one
                    ((MainApplication)getApplication()).getAppHandler().post(runnableCheckReloadWebView);
                }

            } catch (Exception ex){

            }
        }
    }


}
