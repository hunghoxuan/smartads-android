package com.stech.smartads.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.stech.smartads.R;
import com.stech.smartads.components.surveys.MultiChoiceFragment;
import com.stech.smartads.models.AnswerObj;

import java.util.ArrayList;

/**
 * Created by Na Pro on 10/15/2015.
 */
public class MultiChoiceAdapter extends RecyclerView.Adapter<MultiChoiceAdapter.ItemViewHolder> {

    private static final String TAG = MultiChoiceAdapter.class.getSimpleName();

    private Activity context;
    private ArrayList<AnswerObj> arrAnswers;
    private MultiChoiceFragment.MultiChoiceListener listener;

    public MultiChoiceAdapter(Activity context, ArrayList<AnswerObj> answers, MultiChoiceFragment.MultiChoiceListener listener) {
        this.context = context;
        this.arrAnswers = answers;
        this.listener = listener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_checkbox, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {
        if (getItemCount() > 0) {
            final AnswerObj obj = arrAnswers.get(position);
            if (obj != null) {

                holder.lblContent.setText(obj.getValue());
                holder.checkBox.setChecked(obj.isSelected());
                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        obj.setSelected(isChecked);
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
        private CheckBox checkBox;

        private ItemViewHolder(View view) {
            super(view);

            lblContent = (TextView) view.findViewById(R.id.lblContent);
            checkBox = (CheckBox) view.findViewById(R.id.chkSelection);
        }
    }
}
