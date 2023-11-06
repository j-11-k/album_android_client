package com.example.SharedAlbum.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.SharedAlbum.MainActivity;
import com.example.SharedAlbum.PicDataCenter;
import com.example.SharedAlbum.R;
import com.example.SharedAlbum.ui.CustomVIew.SquareImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.wasabeef.glide.transformations.GrayscaleTransformation;
import jp.wasabeef.glide.transformations.gpu.BrightnessFilterTransformation;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageHolder>{
    ImageAdapter(Context context,List<PicDataCenter.PicData> paths,ItemClickCallback clickCallback){
        this.context = context;
        this.paths = paths;
        this.clickCallback = clickCallback;
    }
    List<PicDataCenter.PicData> paths;
    Context context;
    ItemClickCallback clickCallback;
    static ColorMatrix cm = new ColorMatrix();
    static {
        cm.setSaturation(.2f);
    }

    Set<Integer> selected;

    boolean isMultiSelecting(){
        return selected != null;
    }

    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.one_pic_holder,parent,false);
        return new ImageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageHolder holder, int position) {
        Glide.with(context).load(paths.get(position).path).thumbnail(0.25f).centerCrop().into(holder.image);
        holder.image.clearColorFilter();
        if (isMultiSelecting()){
            holder.checkImage.setVisibility(View.VISIBLE);
            if (selected.contains(position)){
                holder.checkImage.setImageResource(R.drawable.pic_selected);
            } else {
                holder.checkImage.setImageResource(R.drawable.pic_unselected);
                holder.image.setColorFilter(new ColorMatrixColorFilter(cm));
            }
        } else {
            holder.checkImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return paths.size();
    }

     class ImageHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public final SquareImageView image;
        public final ImageView checkImage;
        public ImageHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.the_pic);
            checkImage = itemView.findViewById(R.id.multi_check);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (isMultiSelecting()){
                if(selected.contains(position)){
                    selected.remove(position);
                }else{
                    selected.add(position);
                }
                if (selected.isEmpty()){
                    selected = null;
                    clickCallback.notifyModeChanged(false);
                    notifyDataSetChanged();
                } else {
                    notifyItemChanged(position);
                    clickCallback.notifySizeChanged();
                }
            }else {
                Intent intent = new Intent(context, ImagePagerActivity.class);
                intent.putExtra("position",position);
                ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        (Activity)context, image, ImagePagerActivity.PagerText);
                context.startActivity(intent,activityOptions.toBundle());
            }

        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public boolean onLongClick(View view) {
            if (isMultiSelecting()){
                onClick(view);
                return true;
            }
            selected = new HashSet<>();
            selected.add(getAdapterPosition());
            clickCallback.notifyModeChanged(true);
            notifyDataSetChanged();
            return true;
        }
    }

}
