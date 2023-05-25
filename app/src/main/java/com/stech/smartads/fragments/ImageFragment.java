package com.stech.smartads.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.stech.smartads.R;
import com.stech.smartads.models.DataContentObj;
import com.stech.smartads.models.LayoutFrameObj;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImageFragment extends BaseFragment {

    private static final String TAG = ImageFragment.class.getSimpleName();

    private static final String PARAM_DATA = "data";

    public ImageFragment() {
        // Required empty public constructor
    }

    public static ImageFragment getInstance(LayoutFrameObj data) {
        ImageFragment fragment = new ImageFragment();
        fragment.setLayoutFrame(data);

        return fragment;
    }

    @Override
    public ImageView getImageView() {
        if (imageView == null && getView() != null)
            imageView = (ImageView) getView().findViewById(R.id.img_view);
        return imageView;
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
        return inflater.inflate(R.layout.fragment_image, container, false);
    }

    @Override
    void initUI(View view) {

        //default image
        imgBackground = (ImageView) view.findViewById(R.id.imgBackground);
        imgBackground.setVisibility(View.GONE);
        imgBackground.setImageResource(R.drawable.bg_default_screen);

        //webview
        imageView = (ImageView) view.findViewById(R.id.img_view);
        hiddenTextView = view.findViewById(R.id.hiddenTextView);

        // Should call this methods at the end of declaring UI
        //showWebContent(layoutFrame.getData());
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    void refreshData(LayoutFrameObj data) {
        this.layoutFrame = data;
        //start audio service
        startAudioService();
    }
}
