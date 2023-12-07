package com.example.SharedAlbum.ui.notifications;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.SharedAlbum.Data.ConfigCentre;
import com.example.SharedAlbum.NetUtil;
import com.example.SharedAlbum.R;
import com.example.SharedAlbum.databinding.FragmentNotificationsBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class NotificationsFragment extends Fragment {

   private FragmentNotificationsBinding binding;

   private void updateWelcome() {
      if (ConfigCentre.userName == null) {
         binding.textWelcome.setText("您好，请登录");
         binding.loginIn.setText("  登录");
         binding.loginIn.setOnClickListener(v -> {
            var dialog = new AlertDialog.Builder(v.getContext())
                .setTitle("请输入ID").setView(R.layout.dialog_ask_for_num)
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", null)
                .create();
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
                           Toast.makeText(getContext(), "ID不能为空", Toast.LENGTH_SHORT).show();
                        } else {
                           ConfigCentre.userId = Integer.parseInt(input);
                           var path = NetUtil.CheckNet(NetUtil.LOGIN_IN, getContext());
                           if (path != null) {
                              NetUtil.get(path, null, new Callback() {
                                 @Override
                                 public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    Log.i("log_net", "手动登陆时错误", e);
                                 }

                                 @Override
                                 public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                       try {
                                          var body = response.body();
                                          assert body != null;
                                          JSONObject parsed = new JSONObject(body.string());
                                          if (parsed.getBoolean("success")) {
                                             ConfigCentre.userName = parsed.getString("data");//fixme
                                             NetUtil.executor.submit(() -> {
                                                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                                                sharedPref.edit().putInt("userId", ConfigCentre.userId)
                                                    .putString("userName", ConfigCentre.userName)
                                                    .commit();
                                             });
                                             getActivity().runOnUiThread(() -> {
                                                Toast.makeText(getContext(), "登录成功", Toast.LENGTH_SHORT).show();
                                                updateWelcome();
                                                dialog.dismiss();
                                             });
                                          } else {
                                             NetUtil.RemoveLogin(getActivity());
                                          }
                                       } catch (JSONException e) {
                                          Log.i("log_net", "手动登陆时错误", e);
                                       }
                                    } else {
                                       Log.i("log_net", "手动登陆时错误:" + response.code()+response.message());
                                    }
                                 }
                              });
                           }
                        }
                     }
                  });
               }
            });
            dialog.show();
         });
      } else {
         binding.textWelcome.setText("欢迎，" + ConfigCentre.userName);
         binding.loginIn.setText("  登出");
         binding.loginIn.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext()).setTitle("确定要登出吗").setPositiveButton("确定", (dialog, which) -> {
               ConfigCentre.userName = null;
               ConfigCentre.userId = -1;
               NetUtil.executor.submit(() -> getActivity().getPreferences(Context.MODE_PRIVATE).edit().remove("userid").remove("username").commit());
               updateWelcome();
            }).setNegativeButton("取消", null).show();
         });
      }
   }

   public View onCreateView(@NonNull LayoutInflater inflater,
                            ViewGroup container, Bundle savedInstanceState) {

      binding = FragmentNotificationsBinding.inflate(inflater, container, false);
      View root = binding.getRoot();

      binding.changeName.setOnClickListener(v -> {
             if (NetUtil.checkLogin(getContext())) {
                var dialog = new AlertDialog.Builder(v.getContext()).setTitle("请输入新用户名").setView(R.layout.dialog_ask_for_text)
                    .setPositiveButton("确定", null).setNegativeButton("取消", null).create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                   @Override
                   public void onShow(DialogInterface dialog) {
                      if (ConfigCentre.userName != null) {
                         EditText editText = ((AlertDialog) dialog).findViewById(R.id.dialog_input);
                         editText.setText(ConfigCentre.userName);
                      }
                      Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                      button.setOnClickListener(new View.OnClickListener() {

                         @Override
                         public void onClick(View v) {
                            TextView inputView = ((AlertDialog) dialog).findViewById(R.id.dialog_input);
                            String input = inputView.getText().toString();
                            if (input.isBlank()) {
                               Toast.makeText(getContext(), "用户名不能为空", Toast.LENGTH_SHORT).show();
                            } else {
                               var path = NetUtil.CheckNet(NetUtil.CHANGE_USERNAME, getContext());
                               if (path != null) {
                                  NetUtil.post(path, Map.of("uname", input), new Callback() {
                                     @Override
                                     public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                        Log.i("log_net", "改名时错误", e);
                                     }

                                     @Override
                                     public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                        if (!response.isSuccessful()) {
                                           Log.i("log_net", "改名时错误:" + response.code()+response.message());
                                           return;
                                        }
                                        try {
                                           JSONObject parsed = new JSONObject(response.body().string());
                                           if (parsed.getBoolean("success")) {
                                              ConfigCentre.userName = input;
                                              NetUtil.executor.submit(() -> {
                                                 SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                                                 sharedPref.edit()
                                                     .putString("userName", ConfigCentre.userName)
                                                     .commit();
                                              });
                                              getActivity().runOnUiThread(() -> {
                                                 Toast.makeText(getContext(), "改名成功", Toast.LENGTH_SHORT).show();
                                                 updateWelcome();
                                              });
                                           } else {
                                              NetUtil.RemoveLogin(getActivity());
                                           }
                                        } catch (JSONException e) {
                                           Log.i("log_net", "改名时错误", e);
                                        }
                                     }
                                  });
                               }
                               dialog.dismiss();
                            }
                         }
                      });
                   }
                });
                dialog.show();
             }
          }
      );

      binding.changeIp.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            var dialog = new AlertDialog.Builder(v.getContext()).setTitle("请输入服务器IP地址").setView(R.layout.dialog_ask_for_text)
                .setPositiveButton("确定", null).setNegativeButton("取消", null).create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
               @Override
               public void onShow(DialogInterface dialog) {
                  if (ConfigCentre.netAddr != null) {
                     TextView inputView = ((AlertDialog) dialog).findViewById(R.id.dialog_input);
                     inputView.setText(ConfigCentre.netAddr);
                  }
                  Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                  button.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                        TextView inputView = ((AlertDialog) dialog).findViewById(R.id.dialog_input);
                        String input = inputView.getText().toString();
                        if (input.isBlank()) {
                           Toast.makeText(getContext(), "IP地址不能为空", Toast.LENGTH_SHORT).show();
                        } else {
                           try {
                              URI.create(input);//fixme no use
                              ConfigCentre.netAddr = input;
                              NetUtil.executor.submit(() -> getActivity().getPreferences(Context.MODE_PRIVATE).edit()
                                  .putString("netAddr", input).commit());
                              Toast.makeText(getContext(), "修改成功", Toast.LENGTH_SHORT).show();
                              dialog.dismiss();
                           } catch (IllegalArgumentException e) {
                              Toast.makeText(getContext(), "请输入有效IP地址", Toast.LENGTH_SHORT).show();
                           }
                        }
                     }
                  });
               }
            });
            dialog.show();
         }
      });

      binding.changePort.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            var dialog = new AlertDialog.Builder(v.getContext()).setTitle("请输入服务器端口号").setView(R.layout.dialog_ask_for_num)
                .setPositiveButton("确定", null).setNegativeButton("取消", null).create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
               @Override
               public void onShow(DialogInterface dialog) {
                  if (ConfigCentre.port != -1) {
                     TextView inputView = ((AlertDialog) dialog).findViewById(R.id.dialog_input);
                     inputView.setText(Integer.toString(ConfigCentre.port));
                  }
                  Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                  button.setOnClickListener(v1 -> {
                     TextView inputView = ((AlertDialog) dialog).findViewById(R.id.dialog_input);
                     if (inputView.getText().toString().isBlank()) {
                        Toast.makeText(getContext(), "端口号不能为空", Toast.LENGTH_SHORT).show();
                        return;
                     }
                     int input = Integer.parseInt(inputView.getText().toString());
                     if (input < 0 || input > 65535) {
                        Toast.makeText(getContext(), "请输入有效端口号", Toast.LENGTH_SHORT).show();
                     } else {
                        ConfigCentre.port = input;
                        NetUtil.executor.submit(() -> getActivity().getPreferences(Context.MODE_PRIVATE).edit()
                            .putInt("port", input).commit());
                        Toast.makeText(getContext(), "修改成功", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                     }
                  });
               }
            });
            dialog.show();
         }
      });
      return root;
   }

   @Override
   public void onStart() {
      super.onStart();
      getActionBar().setTitle("设置");
      getActionBar().setDisplayHomeAsUpEnabled(false);
      updateWelcome();
   }

   ActionBar getActionBar() {
      return ((AppCompatActivity) getActivity()).getSupportActionBar();
   }

   @Override
   public void onDestroyView() {
      super.onDestroyView();
      binding = null;
   }
}