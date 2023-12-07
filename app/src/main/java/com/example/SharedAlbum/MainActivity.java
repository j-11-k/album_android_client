package com.example.SharedAlbum;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.SharedAlbum.Data.ConfigCentre;
import com.google.android.material.bottomappbar.BottomAppBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.SharedAlbum.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

   private ActivityMainBinding binding;
   private Runnable onFragBack;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      NetUtil.executor.submit(() -> {
         SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
         ConfigCentre.userId = sharedPref.getInt("userId", -1);
         ConfigCentre.userName = sharedPref.getString("userName", null);
         ConfigCentre.netAddr = sharedPref.getString("netAddr", null);
         ConfigCentre.port = sharedPref.getInt("port", -1);
         if (ConfigCentre.userId >= 0 && ConfigCentre.netAddr != null && ConfigCentre.port >= 0) {
            var path = NetUtil.CheckNet(NetUtil.LOGIN_IN, MainActivity.this);
            if (path != null) {
               NetUtil.get(path, null, new Callback() {
                  @Override
                  public void onFailure(@NonNull Call call, @NonNull IOException e) {
                     Log.i("log_net", "开机登陆时登录错误", e);
                  }

                  @Override
                  public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                     if (response.isSuccessful()) {
                        try {
                           var body = response.body();
                           assert body != null;
                           JSONObject parsed = new JSONObject(body.string());
                           if (parsed.getBoolean("success")) {
                              ConfigCentre.userName = parsed.getString("username");
                              NetUtil.executor.submit(() -> {
                                 SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                                 sharedPref.edit()
                                     .putString("userName", ConfigCentre.userName)
                                     .commit();
                              });
                           } else {
                              NetUtil.RemoveLogin(MainActivity.this);
                           }
                        } catch (JSONException e) {
                           Log.i("log_net", "开机登陆时登录错误", e);
                        }
                     } else {
                        Log.i("log_net", "开机登陆时登录错误:" + response.code() + response.message());
                     }
                  }
               });
            }
         }
      });
      binding = ActivityMainBinding.inflate(getLayoutInflater());
      setContentView(binding.getRoot());

      NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
      NavigationUI.setupWithNavController(binding.navView, navController, false);

      Toolbar myToolbar = findViewById(R.id.main_toolbar);
      setSupportActionBar(myToolbar);
      if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
          || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
         ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE
             , Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
      }
   }

   public void switchBottomBar(boolean showAction) {
      if (showAction) {
         binding.bottomActionBar.setVisibility(View.VISIBLE);
         binding.navView.setVisibility(View.GONE);
      } else {
         binding.navView.setVisibility(View.VISIBLE);
         binding.bottomActionBar.setVisibility(View.GONE);
      }
   }

   public BottomAppBar getBottomBar() {
      return binding.bottomActionBar;
   }

   public void setOnFragBack(Runnable onFragBack) {
      this.onFragBack = onFragBack;
   }

   @Override
   public void onBackPressed() {
      if (onFragBack != null) {
         onFragBack.run();
         return;
      }
      super.onBackPressed();
   }
}