package com.example.SharedAlbum.ui.cloud_frag;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.SharedAlbum.MainActivity;
import com.example.SharedAlbum.NetUtil;
import com.example.SharedAlbum.R;
import com.example.SharedAlbum.databinding.AlbumDetailsFragBinding;
import com.example.SharedAlbum.ui.local_frag.ImageAdapter;
import com.example.SharedAlbum.ItemClickCallback;

import java.util.ArrayList;
import java.util.List;

public class AlbumDetailFrag extends Fragment {
    private AlbumDetailsFragBinding binding;
    ImageAdapter adapter;
    String albumName;
    int albumId;

    private void exitMultiMode(){
        adapter.selected = null;
        getActionBar().setTitle(albumName);
        getMainActivity().switchBottomBar(false);
        adapter.notifyDataSetChanged();
        getMainActivity().invalidateOptionsMenu();
        getMainActivity().setOnFragBack(null);
    }

    private void enterMultiMode(){
        getActionBar().setTitle(String.format("已选择 %d 项", adapter.selected.size()));
        getMainActivity().switchBottomBar(true);
        adapter.notifyDataSetChanged();
        getMainActivity().invalidateOptionsMenu();
        getMainActivity().setOnFragBack(this::exitMultiMode);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AlbumDetailsFragBinding.inflate(inflater,container,false);
        View root = binding.getRoot();
        setHasOptionsMenu(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        albumName = getArguments().getString("name");
        albumId = getArguments().getInt("id");
        adapter = new ImageAdapter(getContext(),new ArrayList<>(),new ItemClickCallback() {
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
        binding.albumRecycler.setLayoutManager(new GridLayoutManager(getContext(),3));

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        getMainActivity().getBottomBar().getMenu().findItem(R.id.menu_bottom_album_download).setVisible(true);
        getMainActivity().getBottomBar().getMenu().findItem(R.id.add_to_gallery).setVisible(false);
        getMainActivity().getBottomBar().setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.menu_bottom_album_delete){
                        //todo 广新
                    } else if (item.getItemId() == R.id.menu_bottom_album_download){

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
        } else if (item.getItemId() == R.id.quit_album){
            new AlertDialog.Builder(getContext()).setTitle("确定要退出相册吗").setPositiveButton("确定", (dialog, which) -> {
                getParentFragmentManager().popBackStackImmediate();
            }).setNegativeButton("取消", (dialog, which) -> {}).show();
        } else if (item.getItemId() == R.id.invite_other_to_album) {
            int nums = 3;
            String[] names = new String[]{"用户1", "用户2", "用户3"};
            boolean[] checked = new boolean[nums];
            new AlertDialog.Builder(getContext()).setTitle("请选择要邀请的人").setMultiChoiceItems(names,checked,null)
                    .setPositiveButton("确定", (dialog, which) -> {
                        int cnt = 0;
                        SparseBooleanArray checkedItemPositions =
                                ((AlertDialog) dialog).getListView().getCheckedItemPositions();
                        for (int i = 0; i < checked.length; i++) {
                            if (checkedItemPositions.get(i))
                                ++cnt;
                        }
                        Toast.makeText(getContext(), "邀请成功"+cnt, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", (dialog, which) -> {}).show();
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
