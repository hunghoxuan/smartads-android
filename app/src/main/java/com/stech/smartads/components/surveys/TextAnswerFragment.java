package com.stech.smartads.components.surveys;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.stech.smartads.R;
import com.stech.smartads.activities.PollingActivity;
import com.stech.smartads.models.QuestionObj;
import com.stech.smartads.utils.ImageUtil;
import com.stech.smartads.utils.CommonUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class TextAnswerFragment extends Fragment {

    private static final String TAG = TextAnswerFragment.class.getSimpleName();

    private static final String PARAM_DATA = "data";
    private View view;
    private Activity self;

    private QuestionObj questionObj;
    private TextView lblContent;
    private ImageView ivImage;
    private EditText txtAnswer;




    public TextAnswerFragment() {
        // Required empty public constructor
    }

    public static TextAnswerFragment getInstance(Activity act, QuestionObj question) {

        TextAnswerFragment fragment = new TextAnswerFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(PARAM_DATA,question);
        fragment.setArguments(bundle);
        fragment.self = act;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(PARAM_DATA)) {

            questionObj = bundle.getParcelable(PARAM_DATA);


        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_questions_text, container, false);

        initUI(view);

        initControls();

        return view;
    }

    private void initControls() {

       lblContent.setText(questionObj.getContent());

       if(!questionObj.getImage().isEmpty()){
           CommonUtil.log(TAG,questionObj.getImage());

           ImageUtil.setImage(self, ivImage, questionObj.getImage());

           ivImage.setVisibility(View.VISIBLE);
       } else {
           ivImage.setVisibility(View.GONE);
       }
    }

    private void initUI(View view) {

        lblContent = view.findViewById(R.id.lblContent);
        ivImage = view.findViewById(R.id.ivImage);
        txtAnswer = view.findViewById(R.id.txtAnswer);
        txtAnswer.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if  ((actionId == EditorInfo.IME_ACTION_DONE)) {

                    questionObj.setUserAnswer(txtAnswer.getText().toString());

                    //update question list
                    if (self instanceof PollingActivity){
                        ((PollingActivity)self).updateQuestionList(questionObj);
                    }
                }
                return false;
            }
        });

    }

    @Override
    public void onDetach() {

        super.onDetach();
    }


}
