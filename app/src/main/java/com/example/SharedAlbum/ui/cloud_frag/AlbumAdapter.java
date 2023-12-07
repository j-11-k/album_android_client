package com.example.SharedAlbum.ui.cloud_frag;


import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.SharedAlbum.Data.AlbumData;
import com.example.SharedAlbum.NetUtil;
import com.example.SharedAlbum.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumHolder> {
   List<AlbumData> albums;
   Activity context;

   AlbumAdapter(Activity context) {
      albums = new ArrayList<>();
      this.context = context;
   }

   public void refreshDataFromNet() {
//        albums = AlbumData.getAlbumList();
      var path = NetUtil.CheckNet(NetUtil.GET_ALBUM_LIST, context);
      if (path != null) {
         NetUtil.get(path, null, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
               Log.i("log_net", "获取相册列表错", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
               if (!response.isSuccessful()) {
                  Log.i("log_net", "获取相册列表错:" + response.code()+response.message());
                  return;
               }
               try {
                  var parsed = new JSONObject(response.body().string());
                  if (!parsed.getBoolean("success")){
                     NetUtil.RemoveLogin(context);
                     return;
                  }
                  var albums = new ArrayList<AlbumData>();
                  JSONArray res = parsed.getJSONArray("albums");
                  for (int i = 0; i < res.length(); i++) {
                     var album = res.getJSONObject(i);
                     albums.add(new AlbumData(album.getInt("album_id"), album.getString("album_name")
                         , album.optString("cover_thumbnail_url", null)));
                  }
                  AlbumAdapter.this.albums = albums;
                  notifyDataSetChanged();
               } catch (JSONException|IOException e) {
                  Log.i("log_net", "获取相册列表错", e);
               }
            }
         });
      }
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
            var dialog = new AlertDialog.Builder(v.getContext()).setTitle("新建相册").setView(R.layout.dialog_ask_for_text)
                .setPositiveButton("确定", null).create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
               @Override
               public void onShow(DialogInterface dialog) {
                  Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                  button.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                        TextView inputView = ((AlertDialog) dialog).findViewById(R.id.dialog_input);
                        String input = inputView.getText().toString();
                        if (input.isBlank()) {
                           Toast.makeText(context, "请输入相册名", Toast.LENGTH_SHORT).show();
                        } else {
                           var path = NetUtil.CheckNet(NetUtil.CREATE_ALBUM, button.getContext());
                           if (path == null)
                              return;
                           NetUtil.post(path, Map.of("album_name", input), new Callback() {
                              @Override
                              public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                 Log.i("log_net", "创建新相册错", e);
                              }

                              @Override
                              public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                 if (!response.isSuccessful()) {
                                    Log.i("log_net", "创建新相册错:" + response.code()+response.message());
                                    return;
                                 }
                                 try {
                                    JSONObject parsed = new JSONObject(response.body().string());
                                    if (!parsed.getBoolean("success")) {
                                       NetUtil.RemoveLogin(context);
                                       return;
                                    }
                                 } catch (JSONException e) {
                                    Log.i("log_net", "创建新相册错", e);

                                 }
                                 context.runOnUiThread(()->{
                                    Toast.makeText(button.getContext(), "创建成功", Toast.LENGTH_SHORT).show();
                                 });
                                 refreshDataFromNet();
                              }
                           });
                           dialog.dismiss();
                        }
                     }
                  });
               }
            });
            dialog.show();
         });
      } else {
         --position;
         holder.albumText.setText(albums.get(position).albumName);
         Glide.with(context).load(albums.get(position).coverThumbnailUrl).fallback(R.drawable.total_grey).centerCrop().into(holder.albumCover);
         int finalPosition = position;
         holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Bundle bundle = new Bundle();
               bundle.putString("name", albums.get(finalPosition).albumName);
               bundle.putInt("id", albums.get(finalPosition).albumId);
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
