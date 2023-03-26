package com.stech.smartads.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.stech.smartads.R;
import com.stech.smartads.components.surveys.SingleChoiceFragment;
import com.stech.smartads.models.AnswerObj;

import java.util.ArrayList;

/**
 * Created by Na Pro on 10/15/2015.
 */
public class SingleChoiceAdapter extends RecyclerView.Adapter<SingleChoiceAdapter.ItemViewHolder> {

    private static final String TAG = SingleChoiceAdapter.class.getSimpleName();

    private Activity context;
    private ArrayList<AnswerObj> arrAnswers;
    private SingleChoiceFragment.SingleChoiceListener listener;

    public SingleChoiceAdapter(Activity context, ArrayList<AnswerObj> answers, SingleChoiceFragment.SingleChoiceListener listener) {
        this.context = context;
        this.arrAnswers = answers;
        this.listener = listener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_single_choice, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {
        if (getItemCount() > 0) {
            final AnswerObj obj = arrAnswers.get(position);
            if (obj != null) {

                holder.lblContent.setText(obj.getValue());
                holder.rbSelection.setChecked(obj.isSelected());
                holder.rbSelection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        obj.setSelected(true);
                        listener.onChecked(position,obj);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        try {
            return arrAnswers.size();
        } catch (NullPointerException ex) {
            return 0;
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView lblContent;
        private RadioButton rbSelection;

        private ItemViewHolder(View view) {
            super(view);

            lblContent = (TextView) view.findViewById(R.id.lblContent);
            rbSelection = (RadioButton) view.findViewById(R.id.rbSelection);
        }
    }
}
