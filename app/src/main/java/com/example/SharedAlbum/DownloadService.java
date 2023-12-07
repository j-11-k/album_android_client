package com.example.SharedAlbum;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.example.SharedAlbum.Data.ConfigCentre;
import com.example.SharedAlbum.Data.MyDBHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

public class DownloadService extends Service {
   private static final String ChannelId = "download_channel";
   private static final int NoteID = 2;
   Thread t;
   private final LinkedBlockingQueue<Pair<Integer/*pid*/,String>> queue = new LinkedBlockingQueue<>();
   private final IBinder binder = new LocalBinder();
   NotificationManager notificationManager;

   public class LocalBinder extends Binder {
      public DownloadService getService() {
         return DownloadService.this;
      }
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
          "Download Progress", NotificationManager.IMPORTANCE_DEFAULT);
      notificationManager.createNotificationChannel(channel);
      t = new Thread(()->{
         int size = -1;
         try (var db = new MyDBHelper(DownloadService.this); var handle = db.getWritableDatabase()) {
            while (!t.isInterrupted()) {
               Pair<Integer,String> toDownload = queue.take();
               if (size == -1) {
                  size = 0;
                  startForeground(NoteID, refreshNote(queue.size() + 1, 0));
               } else {
                  notificationManager.notify(NoteID, refreshNote(size + queue.size() + 1, size));
               }

               try (var response = NetUtil.okHttpClient.newCall(new Request.Builder().get().url(toDownload.second).build()).execute()) {
                  if (!response.isSuccessful()) {
                     Log.i("log_net", "下载图片错:" + response.code() + response.message());
                     return;
                  }
                  handle.execSQL("UPDATE DOWNLOAD SET UNFINISHED = UNFINISHED - 1 WHERE PID ="+toDownload.first);

                  File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                  File imageFile = new File(storageDir, toDownload.second.substring(toDownload.second.lastIndexOf('/')+1)+System.currentTimeMillis());
                  assert !imageFile.exists();
                  try (OutputStream output = new FileOutputStream(imageFile)) {
                     output.write(response.body().bytes());
                  } catch (IOException e){
                     Log.i("log_net", "刷新图片到磁盘错", e);
                  }
                  MediaScannerConnection.scanFile(getApplicationContext(), new String[]{imageFile.getPath()}, null, null);
               } catch (IOException e) {
                  Log.i("log_net", "下载图片错", e);
                  return;
               }
               ++size;
               if (queue.isEmpty()) {
                  notificationManager.notify(NoteID, new Notification.Builder(this, ChannelId)
                      .setContentTitle("下载完成，成功下载了" + size + "张图片")
                      .setSmallIcon(R.drawable.ic_ok)
                      .build());
                  handle.delete("DOWNLOAD","UNFINISHED=?",new String[]{"0"});
                  stopForeground(STOP_FOREGROUND_DETACH);
                  size = -1;
               } else {
                  refreshNote(size + queue.size(), size);
               }
            }
         } catch (InterruptedException e) {
            Log.i("interrupt", "下载服务被中断", e);
            stopSelf();
         }
      });
   }

   private Notification refreshNote(int max, int now) {
      return new Notification.Builder(this, ChannelId)
          .setContentTitle("下载中...")
          .setSmallIcon(R.drawable.ic_download_24)
          .setProgress(max, now, false)
          .build();
   }

   public void enqueuePhotos(List<Pair<Integer/*pid*/,String>> toAdd){
      queue.addAll(toAdd);
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      t.interrupt();
   }
}
