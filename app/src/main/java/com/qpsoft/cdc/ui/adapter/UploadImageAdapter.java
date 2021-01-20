package com.qpsoft.cdc.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.luck.picture.lib.entity.LocalMedia;
import com.qpsoft.cdc.R;

import java.util.List;

public class UploadImageAdapter extends RecyclerView.Adapter<UploadImageAdapter.ViewHolder> {

    private Context mContext;
    private List<LocalMedia> mList;
    private LayoutInflater inflater;

    public UploadImageAdapter(Context mContext, List<LocalMedia> mList) {
        this.mContext = mContext;
        this.mList = mList;
        inflater  = LayoutInflater.from(mContext);
    }

    private OnItemClickListener mOnItemClickListener;
    private OnItemDelListener mOnItemDelListener;

    public interface OnItemClickListener {

        void onItemClick(UploadImageAdapter adapter , View view , int position);
    }

    public interface OnItemDelListener {

        void onItemDel(UploadImageAdapter adapter , View view , int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemDelListener(OnItemDelListener listener) {
        mOnItemDelListener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_upload_image, parent,false));
    }

    @Override
    public int getItemCount(){
        if (mList.size() >= 1) {
            return mList.size();
        }
        return mList.size() + 1;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder , final int position) {
        if (getItemViewType(position) == 2) {
            holder.llDel.setVisibility(View.GONE);
        } else {
            holder.llDel.setVisibility(View.VISIBLE);

            holder.llDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemDelListener.onItemDel(UploadImageAdapter.this, view, position);
                }
            });

            String picUrl = mList.get(position).getPath();
            //RequestOptions options = (new RequestOptions()).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE);
            Glide.with(mContext).load(picUrl).into(holder.ivAddPic);
        }

        holder.ivAddPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClickListener.onItemClick(UploadImageAdapter.this, view, position);
            }
        });
    }
    @Override
    public int getItemViewType(int position) {
         if (position == mList.size())
             return 2;
         else
             return 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAddPic;
        LinearLayout llDel;
        public ViewHolder(View itemView) {
            super(itemView);
            ivAddPic = itemView.findViewById(R.id.ivAddPic);
            llDel = itemView.findViewById(R.id.llDel);
        }

    }

    public void setItems(List<LocalMedia> loarMoreDatas) {
        mList = loarMoreDatas;
        notifyDataSetChanged();
    }



}
