package com.example.SharedAlbum;

import android.content.Context;
import android.widget.Toast;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class NetUtil {
    public static final OkHttpClient okHttpClient = new OkHttpClient();

    public static String userName;
    public static Integer userId;

    public static boolean checkLogin(Context context){
        if (userId == null){
            Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
