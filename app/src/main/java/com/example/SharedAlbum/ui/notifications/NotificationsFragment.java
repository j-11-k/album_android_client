package com.example.SharedAlbum.ui.notifications;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.SharedAlbum.NetUtil;
import com.example.SharedAlbum.R;
import com.example.SharedAlbum.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    private void updateWelcome() {
        if (NetUtil.userName == null) {
            binding.textWelcome.setText("您好，请登录");
            binding.loginIn.setText("  登录");
            binding.loginIn.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                    .setTitle("请输入ID").setView(R.layout.dialog_ask_for_num)
                    .setPositiveButton("确定", (dialog, which) -> {
                        TextView inputView = ((AlertDialog) dialog).findViewById(R.id.dialog_input);
                        String input = inputView.getText().toString();
                        if (input.isBlank()) {
                            Toast.makeText(getContext(), "ID不能为空", Toast.LENGTH_SHORT).show();
                        } else {
                            int id = Integer.parseInt(input);
                            //todo login
                            Toast.makeText(getContext(), "登录成功", Toast.LENGTH_SHORT).show();
                            NetUtil.userId = id;
                            NetUtil.userName = "test";
                            updateWelcome();
                        }
                    }).setNegativeButton("取消", (dialog, which) -> {
                    }).show());
        } else {
            binding.textWelcome.setText("欢迎，" + NetUtil.userName);
            binding.loginIn.setText("  登出");
            binding.loginIn.setOnClickListener(v -> {
                new AlertDialog.Builder(v.getContext()).setTitle("确定要登出吗").setPositiveButton("确定", (dialog, which) -> {
                    NetUtil.userName = null;
                    NetUtil.userId = null;
                    updateWelcome();
                }).setNegativeButton("取消", (dialog, which) -> {}).show();
            });
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.changeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetUtil.checkLogin(getContext())) {
                    new AlertDialog.Builder(v.getContext()).setTitle("请输入新用户名").setView(R.layout.dialog_ask_for_text)
                            .setPositiveButton("确定", (dialog, which) -> {
                                TextView inputView = ((AlertDialog) dialog).findViewById(R.id.dialog_input);
                                String input = inputView.getText().toString();
                                if (input.isBlank()) {
                                    Toast.makeText(getContext(), "用户名不能为空", Toast.LENGTH_SHORT).show();
                                } else {
                                    //todo login
                                    Toast.makeText(getContext(), "改名成功", Toast.LENGTH_SHORT).show();
                                    NetUtil.userName = input;
                                    updateWelcome();
                                }
                            }).setNegativeButton("取消", (dialog, which) -> {
                            }).show();
                }
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