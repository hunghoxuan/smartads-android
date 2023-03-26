package com.stech.smartads.components.surveys;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stech.smartads.R;
import com.stech.smartads.activities.PollingActivity;
import com.stech.smartads.adapters.MultiChoiceAdapter;
import com.stech.smartads.models.AnswerObj;
import com.stech.smartads.models.QuestionObj;
import com.stech.smartads.utils.ImageUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class MultiChoiceFragment extends Fragment {

    private static final String TAG = MultiChoiceFragment.class.getSimpleName();

    public interface MultiChoiceListener{
        void onChecked(int index, AnswerObj answerObj);
    }


    private static final String PARAM_DATA = "data";
    private View view;
    private Activity self;

    private QuestionObj questionObj;
    private RecyclerView rcvAnswers;
    private TextView lblContent;
    private ImageView ivImage;
    private MultiChoiceAdapter adapter;




    public MultiChoiceFragment() {
        // Required empty public constructor
    }

    public static MultiChoiceFragment getInstance(Activity act,QuestionObj question) {

        MultiChoiceFragment fragment = new MultiChoiceFragment();
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

        view = inflater.inflate(R.layout.fragment_questions_list, container, false);

        initUI(view);

        initControls();

        return view;
    }

    private void initControls() {
        lblContent.setText(questionObj.getContent());

        if(!questionObj.getImage().isEmpty()){
            ImageUtil.setImage(self, ivImage, questionObj.getImage());
            ivImage.setVisibility(View.VISIBLE);
        } else {
            ivImage.setVisibility(View.GONE);
        }
    }

    private void initUI(View view) {
        lblContent = view.findViewById(R.id.lblContent);
        ivImage = view.findViewById(R.id.ivImage);

        //setup list
        rcvAnswers = view.findViewById(R.id.rcvAnswers);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(self, 2);
        rcvAnswers.setLayoutManager(mLayoutManager);
        rcvAnswers.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        rcvAnswers.setItemAnimator(new DefaultItemAnimator());

        adapter = new MultiChoiceAdapter(self, questionObj.getAnswers(), new MultiChoiceListener() {
            @Override
            public void onChecked(int index,AnswerObj answerObj) {
                questionObj.updateAnswer(index,answerObj);
                adapter.notifyDataSetChanged();

                //update question list
                if (self instanceof PollingActivity){
                    ((PollingActivity)self).updateQuestionList(questionObj);
                }

            }
        });
        rcvAnswers.setAdapter(adapter);

    }

    @Override
    public void onDetach() {

        super.onDetach();
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }


}
