package com.example.SharedAlbum.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SharedAlbum.MainActivity;
import com.example.SharedAlbum.PicDataCenter;
import com.example.SharedAlbum.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    RecyclerView recycler;
    ImageAdapter adapter;

    ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            adapter.selected = null;
            getActionBar().setTitle("图片");
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getMainActivity().switchBottomBar(false);
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setHasOptionsMenu(true);
        recycler = binding.localPhotoList;
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 4));
        adapter = new ImageAdapter(getActivity(), PicDataCenter.GetAllPicData(getContext()), new ItemClickCallback() {
            @Override
            @SuppressLint("DefaultLocale")
            public void notifySizeChanged() {
                getActionBar().setTitle(String.format("已选择 %d 项", adapter.selected.size()));
            }
            @Override
            public void notifyModeChanged(boolean multiMode) {
                if (multiMode) {
                    getActionBar().setTitle(String.format("已选择 %d 项", adapter.selected.size()));
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                    getMainActivity().switchBottomBar(true);
                } else {
                    getActionBar().setTitle("图片");
                    getActionBar().setDisplayHomeAsUpEnabled(false);
                    getMainActivity().switchBottomBar(false);
                }
            }
        });
        recycler.setAdapter(adapter);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}

interface ItemClickCallback {
    void notifySizeChanged();

    void notifyModeChanged(boolean multiMode);
}