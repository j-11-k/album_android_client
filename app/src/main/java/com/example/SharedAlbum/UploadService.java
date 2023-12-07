package com.example.SharedAlbum;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.SharedAlbum.Data.ConfigCentre;
import com.example.SharedAlbum.Data.MyDBHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

public class UploadService extends Service {
   private static final String ChannelId = "upload_channel";
   private static final int NoteID = 1;
   private final IBinder binder = new LocalBinder();
   private final LinkedBlockingQueue<ToUpload> queue = new LinkedBlockingQueue<>();
   Thread t;
   NotificationManager notificationManager;

   public static class ToUpload {
      public final int targetAlbum;
      public final String path;
      public final String name;
      public final long createTime;
      public final int sz;

      public ToUpload(int targetAlbum, String path, String name, long createTime, int sz) {
         this.targetAlbum = targetAlbum;
         this.path = path;
         this.name = name;
         this.createTime = createTime;
         this.sz = sz;
      }
   }

   public class LocalBinder extends Binder {
      public UploadService getService() {
         return UploadService.this;
      }
   }

   public void enqueuePhoto(int targetAlbum, String path, String name, long createTime, int sz) {
      queue.offer(new ToUpload(targetAlbum, path, name, createTime, sz));
   }

   public void enqueuePhotos(List<ToUpload> l) {
      queue.addAll(l);
   }


   @Nullable
   @Override
   public IBinder onBind(Intent intent) {
      return binder;
   }

   @Override
   public void onCreate() {
      super.onCreate();
      notificationManager = getSystemService(NotificationManager.class);
      NotificationChannel channel = new NotificationChannel(ChannelId,
          "Upload Progress", NotificationManager.IMPORTANCE_DEFAULT);
      notificationManager.createNotificationChannel(channel);

      t = new Thread(() -> {
         int size = -1;
         try (var db = new MyDBHelper(UploadService.this); var handle = db.getWritableDatabase()) {
            ToUpload toUpload;
            while (!t.isInterrupted()) {
               toUpload = queue.take();
               if (size == -1) {
                  size = 0;
                  startForeground(NoteID, refreshNote(queue.size() + 1, 0));
               } else {
                  notificationManager.notify(NoteID, refreshNote(size + queue.size() + 1, size));
               }
               var path = NetUtil.CheckNetNoToast(NetUtil.UPLOAD_PHOTO, UploadService.this);
               if (path == null) {
                  return;
               }
               File file = new File(toUpload.path);
               if (!file.canRead()) {
                  handle.delete("UPLOAD", "PATH = ?", new String[]{toUpload.path});
                  refreshNote(size + queue.size(), size);
                  continue;
               }
               HttpUrl.Builder httpBuilder = HttpUrl.parse(path).newBuilder();
               httpBuilder.addQueryParameter("uid", String.valueOf(ConfigCentre.userId));
               httpBuilder.addQueryParameter("name", toUpload.name);
               httpBuilder.addQueryParameter("create", String.valueOf(toUpload.createTime));
               httpBuilder.addQueryParameter("size", String.valueOf(toUpload.sz));
               httpBuilder.addQueryParameter("album", String.valueOf(toUpload.targetAlbum));
               Request request = new Request.Builder().post(RequestBody.create(file, null)).url(httpBuilder.build()).build();
               try (var response = NetUtil.okHttpClient.newCall(request).execute()) {
                  if (!response.isSuccessful()) {
                     Log.i("log_net", "上传图片错:" + response.code() + response.message());
                     return;
                  }
                  //todo 检查返回值并修改数据库
                  var toput = new ContentValues();
                  toput.put("PID", 3/*todo 改成pid*/);
                  handle.update("UPLOAD", toput, "AID = ? AND PATH = ?", new String[]{String.valueOf(toUpload.targetAlbum), toUpload.path});

               } catch (IOException e) {
                  Log.i("log_net", "上传图片错", e);
                  return;
               }
               ++size;
               if (queue.isEmpty()) {
                  notificationManager.notify(NoteID, new Notification.Builder(this, ChannelId)
                      .setContentTitle("上传完成，成功上传了" + size + "张图片")
                      .setSmallIcon(R.drawable.ic_ok)
                      .build());
                  stopForeground(STOP_FOREGROUND_DETACH);
                  size = -1;
               } else {
                  refreshNote(size + queue.size(), size);
               }
            }
         } catch (InterruptedException e) {
            Log.i("interrupt", "上传服务被中断", e);
            stopSelf();
         }
      });
   }

   private Notification refreshNote(int max, int now) {
      return new Notification.Builder(this, ChannelId)
          .setContentTitle("上传中...")
          .setSmallIcon(R.drawable.ic_upload_24)
          .setProgress(max, now, false)
          .build();
   }


   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      return super.onStartCommand(intent, flags, startId);
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      t.interrupt();
   }
}
