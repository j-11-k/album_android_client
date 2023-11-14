package com.example.SharedAlbum.ui.cloud_frag;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.SharedAlbum.AlbumData;
import com.example.SharedAlbum.R;
import com.example.SharedAlbum.ui.local_frag.ImageAdapter;

import java.util.ArrayList;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumHolder> {
    List<AlbumData> albums;
    Activity context;

    AlbumAdapter(Activity context) {
        albums = new ArrayList<>();
        refreshDataFromNet();
        this.context = context;
    }

    public void refreshDataFromNet() {
        albums = AlbumData.getAlbumList();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlbumHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_cover_holder, parent, false);
        return new AlbumHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumHolder holder, int position) {
        if (position == 0) {
            holder.albumText.setText("新建相册");
            Glide.with(context).load(R.drawable.add_new_album).into(holder.albumCover);
            holder.itemView.setOnClickListener(v -> {
                new AlertDialog.Builder(v.getContext()).setTitle("新建相册").setView(R.layout.dialog_ask_for_text)
                        .setPositiveButton("确定", (dialog, which) -> {
                            TextView inputView = ((AlertDialog) dialog).findViewById(R.id.dialog_input);
                            String input = inputView.getText().toString();
                            if (input.isBlank()) {
                                Toast.makeText(context, "请输入相册名", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, input, Toast.LENGTH_SHORT).show();
                                refreshDataFromNet();
                            }
                        }).show();
            });
        } else {
            --position;
            holder.albumText.setText(albums.get(position).albumName);
            Glide.with(context).load(albums.get(position).coverThumbnailUrl).centerCrop().into(holder.albumCover);
            int finalPosition = position;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("name", albums.get(finalPosition).albumName);
                    bundle.putInt("id",albums.get(finalPosition).albumId);
                    Navigation.findNavController(v).navigate(R.id.action_navigation_albums_to_albumDetailFrag,
                            bundle/*,new NavOptions.Builder().setLaunchSingleTop(true).build()*/);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return albums.size() + 1;
    }

    static class AlbumHolder extends RecyclerView.ViewHolder {
        public final ImageView albumCover;
        public final TextView albumText;

        public AlbumHolder(@NonNull View itemView) {
            super(itemView);
            albumCover = itemView.findViewById(R.id.album_cover);
            albumText = itemView.findViewById(R.id.album_name);
        }
    }

}
