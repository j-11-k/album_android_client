package com.example.SharedAlbum.ui.cloud_frag;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.SharedAlbum.Data.AlbumData;
import com.example.SharedAlbum.Data.MyDBHelper;
import com.example.SharedAlbum.MainActivity;
import com.example.SharedAlbum.NetUtil;
import com.example.SharedAlbum.Data.PicDataCenter;
import com.example.SharedAlbum.R;
import com.example.SharedAlbum.UploadService;
import com.example.SharedAlbum.databinding.AlbumDetailsFragBinding;
import com.example.SharedAlbum.ui.local_frag.ImageAdapter;
import com.example.SharedAlbum.ItemClickCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AlbumDetailFrag extends Fragment {
   private AlbumDetailsFragBinding binding;
   ImageAdapter adapter;
   String albumName;
   int albumId;

   private void exitMultiMode() {
      adapter.selected = null;
      getActionBar().setTitle(albumName);
      getMainActivity().switchBottomBar(false);
      adapter.notifyDataSetChanged();
      getMainActivity().invalidateOptionsMenu();
      getMainActivity().setOnFragBack(null);
   }

   private void enterMultiMode() {
      getActionBar().setTitle(String.format("已选择 %d 项", adapter.selected.size()));
      getMainActivity().switchBottomBar(true);
      adapter.notifyDataSetChanged();
      getMainActivity().invalidateOptionsMenu();
      getMainActivity().setOnFragBack(this::exitMultiMode);
   }

   private void refreshPhotos() {
      var path = NetUtil.CheckNet(NetUtil.GET_PHOTO_LIST, getContext());
      if (path == null) return;
      NetUtil.get(path, Map.of("album_id", Integer.toString(albumId)), new Callback() {
         @Override
         public void onFailure(@NonNull Call call, @NonNull IOException e) {
            Log.i("log_net", "获取图片列表错", e);
         }

         @Override
         public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            if (!response.isSuccessful()) {
               Log.i("log_net", "获取图片列表错:" + response.code()+response.message());
               return;
            }
            try {
               var parsed = new JSONObject(response.body().string());
               if (!parsed.getBoolean("success")) {
                  NetUtil.RemoveLogin(getActivity());
                  return;
               }
               JSONArray array = parsed.getJSONArray("photos");
               List<PicDataCenter.PicData> photos = new ArrayList<>(parsed.length());
               for (int i = 0; i < parsed.length(); i++) {
                  var o = array.getJSONObject(i);
                  photos.add(new AlbumData.RemotePicData(o.getString("photo_name"), o.getString("clearimage_url"),
                      new Date(o.getLong("shooting_time")), o.getString("thumbnail_url"),
                      o.getString("super_thumbnail_url"), o.getString("uploader_name"), o.getInt("pid")));
               }
               adapter.paths = photos;
               adapter.notifyDataSetChanged();
            } catch (JSONException e) {
               Log.i("log_net", "获取图片列表错", e);
            }
         }
      });
   }

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      binding = AlbumDetailsFragBinding.inflate(inflater, container, false);
      View root = binding.getRoot();
      setHasOptionsMenu(true);
      getActionBar().setDisplayHomeAsUpEnabled(true);
      albumName = getArguments().getString("name");
      albumId = getArguments().getInt("id");
      adapter = new ImageAdapter(getContext(),  new ItemClickCallback() {
         @Override
         @SuppressLint("DefaultLocale")
         public void notifySizeChanged() {
            getActionBar().setTitle(String.format("已选择 %d 项", adapter.selected.size()));
         }

         @Override
         public void notifyModeChanged(boolean multiMode) {
            if (multiMode) {
               enterMultiMode();
            } else {
               exitMultiMode();
            }
         }
      });
      binding.albumRecycler.setAdapter(adapter);
      binding.albumRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
      refreshPhotos();
      return root;
   }

   @Override
   public void onStart() {
      super.onStart();
      getMainActivity().getBottomBar().getMenu().findItem(R.id.menu_bottom_album_download).setVisible(true);
      getMainActivity().getBottomBar().getMenu().findItem(R.id.add_to_gallery).setVisible(false);
      getMainActivity().getBottomBar().setOnMenuItemClickListener(item -> {
             if (adapter.selected.isEmpty()) {
                Toast.makeText(getContext(), "请选择至少一张图片", Toast.LENGTH_SHORT).show();
             } else if (item.getItemId() == R.id.menu_bottom_album_delete) {
                var path = NetUtil.CheckNet(NetUtil.DELETE_PHOTO, getContext());
                if (path == null) return true;
                JSONArray toDelete = new JSONArray();
                for (var p : adapter.selected) {
                   toDelete.put(((AlbumData.RemotePicData) (adapter.paths.get(p))).pid);
                }
                NetUtil.post(path, Map.of("album_id",String.valueOf(albumId),"pid", toDelete.toString()), new Callback() {
                   @Override
                   public void onFailure(@NonNull Call call, @NonNull IOException e) {
                      Log.i("log_net", "删除图片错", e);
                   }

                   @Override
                   public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                      if (!response.isSuccessful()) {
                         Log.i("log_net", "删除图片错" + response.code()+response.message());
                         return;
                      }
                      try {
                         var parsed = new JSONObject(response.body().string());
                         boolean success = parsed.getBoolean("success");
                         if (!success) {
                            NetUtil.RemoveLogin(getActivity());
                            return;
                         }
                         refreshPhotos();
                         getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                         });
                      } catch (JSONException e) {
                         Log.i("log_net", "删除图片错", e);
                      }
                   }
                });
                exitMultiMode();
             } else if (item.getItemId() == R.id.menu_bottom_album_download) {
                ArrayList<Pair<Integer,String>> toDownloads = new ArrayList<>(adapter.selected.size());
                try (var db = new MyDBHelper(getContext()); var handle = db.getWritableDatabase();) {
                   for (int i : adapter.selected) {
                      var data = (AlbumData.RemotePicData) adapter.paths.get(i);
                      handle.execSQL(String.format("REPLACE INTO DOWNLOAD (PID, PATH, UNFINISHED) VALUES (%d,'%s',COALESCE((SELECT UNFINISHED FROM DOWNLOAD WHERE PID = %d) + 1, 1)"
                          ,data.pid,data.path,data.pid));
                      toDownloads.add(new Pair<>(data.pid,data.path));
                   }
                }
                var downloader = NetUtil.GetDownloadService(getActivity());
                downloader.enqueuePhotos(toDownloads);
                exitMultiMode();
             }
             return true;
          }
      );
      getActionBar().setTitle(albumName);
   }

   @Override
   public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
      super.onCreateOptionsMenu(menu, inflater);
      getActivity().getMenuInflater().inflate(R.menu.top_actionbar_album, menu);
      menu.findItem(R.id.menu_top_album_select_all).setVisible(adapter.selected != null);
   }

   @Override
   public boolean onOptionsItemSelected(@NonNull MenuItem item) {
      if (item.getItemId() == android.R.id.home) {
         if (adapter.selected != null) {
            exitMultiMode();
         } else {
            getActivity().onBackPressed();
         }
         return true;
      }
      if (item.getItemId() == R.id.menu_top_album_select_all) {
         if (adapter.selected.size() < adapter.paths.size()) {
            adapter.selected.clear();
            for (int i = 0; i < adapter.paths.size(); i++) {
               adapter.selected.add(i);
            }
         } else {
            adapter.selected.clear();
         }
         adapter.notifyDataSetChanged();
         getActionBar().setTitle(String.format("已选择 %d 项", adapter.selected.size()));
      } else if (item.getItemId() == R.id.quit_album) {
         new AlertDialog.Builder(getContext()).setTitle("确定要退出相册吗").setPositiveButton("确定", (dialog, which) -> {
            var path = NetUtil.CheckNet(NetUtil.QUIT_ALBUM, getContext());
            if (path == null)
               return;
            NetUtil.post(path, Map.of("album_id", Integer.toString(albumId)), new Callback() {
               @Override
               public void onFailure(@NonNull Call call, @NonNull IOException e) {
                  Log.i("log_net", "退出相册错误", e);
               }

               @Override
               public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                  if (!response.isSuccessful()) {
                     Log.i("log_net", "退出相册错误:" + response.code()+response.message());
                     return;
                  }
                  try {
                     JSONObject parsed = new JSONObject(response.body().string());
                     if (parsed.getBoolean("success")) {
                        getActivity().runOnUiThread(()->{
                           Toast.makeText(getContext(), "退出成功", Toast.LENGTH_SHORT).show();
                           getParentFragmentManager().popBackStackImmediate();//fixme 这个可能有问题
                        });
                     } else {
                        NetUtil.RemoveLogin(getActivity());
                     }
                  } catch (JSONException e) {
                     Log.i("log_net", "退出相册错误", e);
                  }
               }
            });
         }).setNegativeButton("取消", null).show();
      } else if (item.getItemId() == R.id.invite_other_to_album) {
         var path = NetUtil.CheckNet(NetUtil.GET_USERS_LIST, getContext());
         if (path == null)
            return true;
         NetUtil.get(path, Map.of("album_id", Integer.toString(albumId)), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
               Log.i("log_net", "获取可邀请人员列表错", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
               if (!response.isSuccessful()) {
                  Log.i("log_net", "获取可邀请人员列表错:" + response.code()+response.message());
                  return;
               }
               int nums;
               int[] ids;
               String[] names;
               try {
                  var parsed = new JSONObject(response.body().string());
                  if (!parsed.getBoolean("success")) {
                     NetUtil.RemoveLogin(getActivity());
                     return;
                  }
                  JSONArray jsonArray = parsed.getJSONArray("users");
                  nums = jsonArray.length();
                  ids = new int[nums];
                  names = new String[nums];
                  for (int i = 0; i < nums; i++) {
                     var user = jsonArray.getJSONObject(i);
                     ids[i] = user.getInt("uid");
                     names[i] = user.getString("uname");
                  }
               } catch (JSONException e) {
                  Log.i("log_net", "获取可邀请人员列表错", e);
                  return;
               }
               var dialog = new AlertDialog.Builder(getContext())
                   .setTitle("请选择要邀请的人").setMultiChoiceItems(names, new boolean[nums], null)
                   .setPositiveButton("确定", null)
                   .setNegativeButton("取消", null).create();
               dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                  @Override
                  public void onShow(DialogInterface dialog) {
                     Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                     button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                           JSONArray toInvite = new JSONArray();
                           SparseBooleanArray checkedItemPositions =
                               ((AlertDialog) dialog).getListView().getCheckedItemPositions();
                           for (int i = 0; i < names.length; i++) {
                              if (checkedItemPositions.get(i)) {
                                 toInvite.put(ids[i]);
                              }
                           }
                           if (toInvite.length() == 0) {
                              Toast.makeText(getContext(), "请选择要邀请的人", Toast.LENGTH_SHORT).show();
                           }

                           var path = NetUtil.CheckNet(NetUtil.INVITE_USER, getContext());
                           if (path == null)
                              return;
                           NetUtil.post(path, Map.of("album_id", Integer.toString(albumId), "to_invite", toInvite.toString()), new Callback() {
                              @Override
                              public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                 Log.i("log_net", "邀请进相册错", e);
                              }

                              @Override
                              public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                 if (!response.isSuccessful()) {
                                    Log.i("log_net", "邀请进相册错:" + response.code()+response.message());
                                    return;
                                 }
                                 try {
                                    JSONObject parsed = new JSONObject(response.body().string());
                                    if (parsed.getBoolean("success")) {
                                       getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "邀请成功", Toast.LENGTH_SHORT).show());
                                    } else {
                                       NetUtil.RemoveLogin(getActivity());
                                    }
                                 } catch (JSONException e) {
                                    Log.i("log_net", "邀请进相册错", e);
                                 }
                              }
                           });
                           dialog.dismiss();
                        }
                     });
                  }
               });
               getActivity().runOnUiThread(dialog::show);
            }
         });
      }
      return super.onOptionsItemSelected(item);
   }

   MainActivity getMainActivity() {
      return (MainActivity) getActivity();
   }

   ActionBar getActionBar() {
      return ((AppCompatActivity) getActivity()).getSupportActionBar();
   }
}
