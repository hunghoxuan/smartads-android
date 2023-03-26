package com.stech.smartads.adapters;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.stech.smartads.R;
import com.stech.smartads.models.DataContentObj;
import com.stech.smartads.utils.ImageUtil;
import com.stech.smartads.components.textview.TextViewRegular;

import java.util.ArrayList;

/**
 * Created by Na Pro on 10/15/2015.
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ItemViewHolder> {

    private static final String TAG = NewsAdapter.class.getSimpleName();

    private AppCompatActivity context;
    private ArrayList<DataContentObj> newsObjs;

    public NewsAdapter(AppCompatActivity context, ArrayList<DataContentObj> newsObjs) {
        this.context = context;
        this.newsObjs = newsObjs;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {
        if (getItemCount() > 0) {
            final DataContentObj obj = newsObjs.get(position);
            if (obj != null) {
                int pos = position + 1;
                holder.lblPosition.setText(String.valueOf(pos < 10 ? "0" + pos : pos));
                holder.lblTitle.setText(obj.getTitle());
//                holder.lblDesc.setText(obj.getDescription());
                ImageUtil.setImage(context, holder.img, obj.getUrl());

                if (position < getItemCount() - 1) {
                    holder.divider.setVisibility(View.VISIBLE);
                } else {
                    holder.divider.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        try {
            return newsObjs.size();
        } catch (NullPointerException ex) {
            return 0;
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextViewRegular lblPosition, lblTitle, lblDesc;
        private ImageView img;
        private View divider;

        private ItemViewHolder(View view) {
            super(view);

            lblPosition = (TextViewRegular) view.findViewById(R.id.lbl_position);
            lblTitle = (TextViewRegular) view.findViewById(R.id.lbl_title);
//            lblTitle.setSelected(true);
            lblDesc = (TextViewRegular) view.findViewById(R.id.lbl_desc);
            img = (ImageView) view.findViewById(R.id.img);
            divider = view.findViewById(R.id.divider);
        }
    }
}
