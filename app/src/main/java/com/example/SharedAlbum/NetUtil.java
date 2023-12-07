package com.example.SharedAlbum;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.SharedAlbum.Data.ConfigCentre;

import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class NetUtil {
   public static String LOGIN_IN = "login";
   public static String CREATE_ALBUM = "create-album";
   public static String CHANGE_ALBUM_NAME = "change-album-name";//暂未实现
   public static String CHANGE_USERNAME = "change-username";
   public static String GET_ALBUM_LIST = "list-albums";
   public static String GET_PHOTO_LIST = "list-photos";
   public static String GET_USERS_LIST = "list-non-users";
   public static String INVITE_USER = "invite-user";
   public static String QUIT_ALBUM = "quit-album";
   public static String DELETE_PHOTO = "remove-photo";

   public static String UPLOAD_PHOTO = "upload-photo";

   static volatile UploadService uploadService = null;
   static volatile DownloadService downloadService = null;
   static private ServiceConnection mConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
         uploadService = ((UploadService.LocalBinder) iBinder).getService();
      }

      @Override
      public void onServiceDisconnected(ComponentName componentName) {
         uploadService = null;
      }
   };

   static private ServiceConnection downConn = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
         downloadService = ((DownloadService.LocalBinder) service).getService();
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
         downloadService = null;
      }
   };

   public static final OkHttpClient okHttpClient = new OkHttpClient();

   public static ExecutorService executor = Executors.newSingleThreadExecutor();

   public static boolean checkLogin(Context context) {
      if (ConfigCentre.userId == -1) {
         Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show();
         return false;
      }
      return true;
   }

   public static UploadService GetUploadService(Activity a) {
      if (uploadService != null) {
         return uploadService;
      }
      a.startService(new Intent(a, UploadService.class));
      a.bindService(new Intent(a, UploadService.class), mConnection, Context.BIND_IMPORTANT);
      assert uploadService != null;
      return uploadService;
   }

   public static DownloadService GetDownloadService(Activity a) {
      if (downloadService != null) {
         return downloadService;
      }
      a.startService(new Intent(a, DownloadService.class));
      a.bindService(new Intent(a, DownloadService.class), downConn, Context.BIND_IMPORTANT);
      assert downloadService != null;
      return downloadService;
   }


   public static String CheckNet(String path, Context c) {
      ConnectivityManager systemService = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
      if (systemService.getActiveNetwork() == null) {
         Toast.makeText(c, "未连接网络", Toast.LENGTH_SHORT).show();
         return null;
      }
      if (ConfigCentre.userId < 0) {
         Toast.makeText(c, "请先登录", Toast.LENGTH_SHORT).show();
         return null;
      }
      if (ConfigCentre.netAddr == null || ConfigCentre.port < 0) {
         Toast.makeText(c, "请先在设置中配置IP和端口", Toast.LENGTH_SHORT).show();
         return null;
      }
      return "http://" + ConfigCentre.netAddr + ':' + ConfigCentre.port + '/' + path;
   }

   public static String CheckNetNoToast(String path, Context c) {
      ConnectivityManager systemService = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
      if (systemService.getActiveNetwork() == null) {
         return null;
      }
      if (ConfigCentre.userId < 0) {
         return null;
      }
      if (ConfigCentre.netAddr == null || ConfigCentre.port < 0) {
         return null;
      }
      return "http://" + ConfigCentre.netAddr + ':' + ConfigCentre.port + '/' + path;
   }

   public static void get(String url, Map<String, String> params, Callback responseCallback) {
      HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
      httpBuilder.addQueryParameter("uid", String.valueOf(ConfigCentre.userId));
      if (params != null) {
         for (Map.Entry<String, String> param : params.entrySet()) {
            httpBuilder.addQueryParameter(param.getKey(), param.getValue());
         }
      }
      Request request = new Request.Builder().url(httpBuilder.build()).build();
      okHttpClient.newCall(request).enqueue(responseCallback);
   }

   public static void post(String url, @NonNull Map<String, String> params, Callback responseCallback) {
      HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
      httpBuilder.addQueryParameter("uid", String.valueOf(ConfigCentre.userId));
      JSONObject body = new JSONObject(params);
      Request request = new Request.Builder().post(RequestBody.create(body.toString(), MediaType.get("JSON"))).url(httpBuilder.build()).build();
      okHttpClient.newCall(request).enqueue(responseCallback);
   }

   public static void RemoveLogin(Activity c) {
      c.runOnUiThread(() -> Toast.makeText(c, "登录失效，请重试", Toast.LENGTH_SHORT).show());
      ConfigCentre.userId = -1;
      ConfigCentre.userName = null;
      executor.submit(() -> c.getPreferences(Context.MODE_PRIVATE).edit().remove("userid").remove("username").commit());
   }
}
