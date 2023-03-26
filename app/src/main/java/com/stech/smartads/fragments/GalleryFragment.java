package com.stech.smartads.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.stech.smartads.R;
import com.stech.smartads.models.DataContentObj;
import com.stech.smartads.models.LayoutFrameObj;

import java.io.File;
import java.util.List;

//import com.stech.smartads.widgets.SliderLayout;
//import com.stech.smartads.widgets.SliderAdapter;
//import com.stech.smartads.widgets.DefaultSliderView;

/**
 * A simple {@link Fragment} subclass.
 */
public class GalleryFragment extends BaseFragment {

    private static final String TAG = GalleryFragment.class.getSimpleName();

    private SliderLayout mSlider;

    public GalleryFragment() {
        // Required empty public constructor
    }

    public static GalleryFragment getInstance(LayoutFrameObj data) {
        GalleryFragment fragment = new GalleryFragment();
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
        return inflater.inflate(R.layout.fragment_slide, container, false);
    }

    @Override
    void initUI(View view) {
        mSlider = (SliderLayout) view.findViewById(R.id.slider);

        // Should call this methods at the end of declaring UI
        initSlider(layoutFrame.getData());
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mSlider != null) {
            mSlider.startAutoCycle();
        }
    }

    @Override
    public void onPause() {
        if (mSlider != null) {
            mSlider.stopAutoCycle();
        }
        super.onPause();
    }

    private void initSlider(List<DataContentObj> data) {
       mSlider.removeAllSliders();
        if (data != null) {

            DataContentObj item;

            for (int i = 0; i < data.size(); i++) {

                item = data.get(i);

                DefaultSliderView textSliderView = new DefaultSliderView(self);
                // initialize a SliderLayout
                textSliderView
                        .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                        .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                            @Override
                            public void onSliderClick(BaseSliderView slider) {
                              //  CommonUtil.startActivityLTR(self,MainFunctionsActivity.class);
                            }
                        });

                //set description
                //textSliderView.description(item.getDescription());


                //set image

                String url = item.getPlayUrl(self);

                //CommonUtil.log(TAG, "Slide File Url: "+ url);

                if(url.startsWith("http")) {
                    textSliderView.image(url);
                }else{
                    File f = new File(url);
                    textSliderView.image(f);
                }

                //add your extra information
//            textSliderView.bundle(new Bundle());
//            textSliderView.getBundle().putString("extra", objs.get(i).getUrl());
                mSlider.addSlider(textSliderView);
            }
        }
    }

    @Override
    void refreshData(LayoutFrameObj data) {
        this.layoutFrame = data;
       initSlider(this.layoutFrame.getData());

       //start audio service
       startAudioService();
    }
}
