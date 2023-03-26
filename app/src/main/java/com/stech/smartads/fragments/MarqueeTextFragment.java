package com.stech.smartads.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stech.smartads.R;
import com.stech.smartads.models.LayoutFrameObj;
import com.stech.smartads.utils.StringUtil;
import com.stech.smartads.components.textview.TextViewRegular;

/**
 * A simple {@link Fragment} subclass.
 */
public class MarqueeTextFragment extends BaseFragment {

    private static final String TAG = MarqueeTextFragment.class.getSimpleName();

    private static final String PARAM_DATA = "data";

    private String mBannerText;

    public MarqueeTextFragment() {
        // Required empty public constructor
    }

    public static MarqueeTextFragment getInstance(LayoutFrameObj data) {
        MarqueeTextFragment fragment = new MarqueeTextFragment();
        fragment.setLayoutFrame(data);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(PARAM_DATA)) {
            layoutFrame = bundle.getParcelable(PARAM_DATA);
            if(layoutFrame!=null && layoutFrame.getData().size()>0)
            {
                this.mBannerText = layoutFrame.getData().get(0).getDescription();
            } else {
                this.mBannerText ="";
            }
        }
    }

    @Override
    View inflateLayout(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_marquee_text, container, false);
    }

    @Override
    void initUI(View view) {
        TextViewRegular lblBanner = (TextViewRegular) view.findViewById(R.id.lbl_banner);
        lblBanner.setSelected(true);

        // Should call this methods at the end of declaring UI
        lblBanner.setText(StringUtil.removeHtmlTags(mBannerText));
        lblBanner.setTextColor(Color.parseColor(this.layoutFrame.getFontColor()));
        lblBanner.setTextSize((layoutFrame.getHeight(self)*42/100)/ getResources().getDisplayMetrics().scaledDensity);
    }


    @Override
    void refreshData(LayoutFrameObj data) {

        if(data == null) return;

        this.layoutFrame = data;

        if( this.layoutFrame.getData().size()>0)
        {
            this.mBannerText = this.layoutFrame.getData().get(0).getDescription();
        }else {
            this.mBannerText ="";
        }

        //start audio service
        startAudioService();
    }
}
