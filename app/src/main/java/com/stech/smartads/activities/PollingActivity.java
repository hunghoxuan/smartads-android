package com.stech.smartads.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stech.smartads.R;
import com.stech.smartads.components.surveys.MultiChoiceFragment;
import com.stech.smartads.components.surveys.SingleChoiceFragment;
import com.stech.smartads.interfaces.IModelListener;
import com.stech.smartads.core.AppData;
import com.stech.smartads.utils.ParseUtility;
import com.stech.smartads.models.QuestionObj;
import com.stech.smartads.components.surveys.TextAnswerFragment;
import com.stech.smartads.utils.CommonUtil;
import com.stech.smartads.components.noscrollviewpager.CustomViewPager;

import java.util.ArrayList;
import java.util.List;

public class PollingActivity extends BaseActivity implements View.OnClickListener {

    private CustomViewPager viewPager;
    private Button btnPrevious, btnNext;
    private TextView lblPosition;
    private RelativeLayout rtlHeader;
    private ImageView imgLogo;
    private TextView lblTitle;
    private ImageView btnCancel;

    private ArrayList<QuestionObj> arrQuestions = new ArrayList<>();
    private MyViewPagerAdapter adapter;
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
       btnPrevious = (Button) this.findViewById(R.id.prev_button);
       btnNext = (Button) this.findViewById(R.id.next_button);
       lblPosition = (TextView) this.findViewById(R.id.lblPosition);

    }

    @Override
    protected void initControl() {

        btnCancel.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        btnNext.setOnClickListener(this);

       //init viewpager
        adapter = new MyViewPagerAdapter(getSupportFragmentManager());
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

        //load data
        getSurvey();

    }

    @Override
    protected void getExtraValues() {

    }


    private void getSurvey()
    {

        AppData().requestPolling(new IModelListener() {
            @Override
            public void onSuccess(Object obj) {
                String json = obj.toString();

                List<QuestionObj> arr =  ParseUtility.parseQuestions(json);

                //clear old list
                arrQuestions.clear();

                if(arr.size() > 0){
                    arrQuestions.addAll(arr);
                }

                adapter.notifyDataSetChanged();

                updateBottomBar();

            }

            @Override
            public void onError(Throwable error) {

            }
        });
    }

    public void updateQuestionList(QuestionObj questionObj){
        for (int i = 0; i <arrQuestions.size();i++){
            QuestionObj question = arrQuestions.get(i);

            if(question.getId() == questionObj.getId()){
                arrQuestions.set(i,questionObj);
                break;
            }
        }

        updateBottomBar();

        //reset count when user is working
        count = 0;

    }



    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    public void onClick(View v) {

        //reset if
        count = 0;

        if(v == btnPrevious){
            onClickPrevious();
            return;
        }

        if(v == btnNext){

            onClickNext();
            return;
        }

        if(v == btnCancel){

            this.finish();

        }

    }

    private void onClickNext(){

        if(viewPager.getCurrentItem() == arrQuestions.size()-1){
            finish();
            showMessage( "Đã gửi kết quả lên server ! Cám ơn bạn đã đóng góp ý kiến.");

        } else {
              viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
        }
    }

    private void onClickPrevious(){
      viewPager.setCurrentItem(viewPager.getCurrentItem()-1);
    }

    private void updateBottomBar() {
        int position = viewPager.getCurrentItem();

        lblPosition.setText("Question "+(position+1) +"/"+arrQuestions.size() + ":");

        if (position == arrQuestions.size()-1) {
            btnNext.setText(R.string.submit);

        } else {
            btnNext.setText(R.string.next);
            btnNext.setBackgroundResource(R.drawable.bg_pressed_grey);

        }

        btnPrevious.setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);

        btnNext.setEnabled(arrQuestions.get(position).isAnswered());
    }


    class MyViewPagerAdapter extends FragmentStatePagerAdapter{

        public MyViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return arrQuestions.size();
        }

        @Override
        public Fragment getItem(int position) {

            QuestionObj questionObj = arrQuestions.get(position);

            if(questionObj.isMultiChoices()){
                return MultiChoiceFragment.getInstance(self,questionObj);
            } else if (questionObj.isSingleChoices()){
                return SingleChoiceFragment.getInstance(self,questionObj);
            } else {
                return TextAnswerFragment.getInstance(self,questionObj);
            }

        }

    }
}


