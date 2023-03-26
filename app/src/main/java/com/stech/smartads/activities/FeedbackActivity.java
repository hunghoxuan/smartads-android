package com.stech.smartads.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stech.smartads.R;
import com.stech.smartads.models.QuestionObj;
import com.stech.smartads.components.surveys.TextAnswerFragment;
import com.stech.smartads.components.noscrollviewpager.CustomViewPager;

public class FeedbackActivity extends BaseActivity implements View.OnClickListener {


    public interface FeedbackListener {
        void onSelectedRate(int rateValue);
        void onSelectedResult(String reason);
    }

    private CustomViewPager viewPager;
    private RelativeLayout rtlHeader;
    private ImageView imgLogo;
    private TextView lblTitle;
    private ImageView btnCancel;

    private FeedbackViewPagerAdapter adapter;
    private FeedbackListener listener;


    private Handler handler = new Handler(Looper.myLooper());

    //check finish time
    private int totalTime = 30;
    private int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //hide NavigationBar
        getDecorView().setSystemUiVisibility(uiOptions);

        handler.post(new Runnable() {
            @Override
            public void run() {

                if(count < totalTime){
                    count++;
                    handler.postDelayed(this,1000);
                } else {
                    finish();
                }

            }
        });

    }

    @Override
    protected void inflateLayout() {
        getLayoutInflater().inflate(R.layout.activity_polling, getFrameLayout());
    }

    @Override
    protected void initUI() {

        //header
        rtlHeader = (RelativeLayout) this.findViewById(R.id.rtlHeader);
        imgLogo = (ImageView) this.findViewById(R.id.imgLogo);
        lblTitle = (TextView) this.findViewById(R.id.lblTitle);
        btnCancel = (ImageView) this.findViewById(R.id.btnCancel);

       viewPager = (CustomViewPager) this.findViewById(R.id.viewPager);


    }

    @Override
    protected void initControl() {

        btnCancel.setOnClickListener(this);


       //init viewpager
        adapter = new FeedbackViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setPagingEnabled(false);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
              updateBottomBar();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    protected void getExtraValues() {

    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    public void onClick(View v) {

        //reset if
        count = 0;


        if(v == btnCancel){

            this.finish();

        }

    }

    private void onClickNext(){

        viewPager.setCurrentItem(viewPager.getCurrentItem()+1);

    }

    private void onClickPrevious(){
      viewPager.setCurrentItem(viewPager.getCurrentItem()-1);
    }

    private void updateBottomBar() {
        int position = viewPager.getCurrentItem();

    }


    class FeedbackViewPagerAdapter extends FragmentStatePagerAdapter{

        public FeedbackViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Fragment getItem(int position) {



                return TextAnswerFragment.getInstance(self,new QuestionObj(null));


        }

    }
}


