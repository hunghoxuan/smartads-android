package com.stech.smartads.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.stech.smartads.R;
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
public class WebViewFragment extends BaseFragment {

    private static final String TAG = WebViewFragment.class.getSimpleName();

    private static final String PARAM_DATA = "data";
    
    private int reloadWebCountInError = 0;

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

        ((MainApplication)getApplication()).getAppHandler().postDelayed(this, 15*1000);
        }
    };

    public WebViewFragment() {
        // Required empty public constructor
    }

    public static WebViewFragment getInstance(LayoutFrameObj data) {
        WebViewFragment fragment = new WebViewFragment();
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

        reloadWebCountInError = 0;

        //webview
        webView = (AdvancedWebView) view.findViewById(R.id.webview);
        hiddenTextView = view.findViewById(R.id.hiddenTextView);

        initWebview(webView);

        // Should call this methods at the end of declaring UI
        showWebContent(layoutFrame.getData());

    }

    protected void showWebContent(List<DataContentObj> data) {
        if (isLoading)
            return;
        super.showWebContent(webView, data);
        imgBackground.setVisibility(View.GONE);
    }

    //reload webview khi error. 5 giay/ lan
    protected void reloadWebViewWhenError() {
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
