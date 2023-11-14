package com.example.SharedAlbum.ui.cloud_frag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SharedAlbum.databinding.FragmentDashboardBinding;
import com.example.SharedAlbum.ui.local_frag.ImageAdapter;

public class AlbumFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private AlbumAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.albumRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new AlbumAdapter(getActivity());
//        binding.albumRecycler.addItemDecoration(new DividerItemDecoration(binding.albumRecycler.getContext(), DividerItemDecoration.HORIZONTAL));
        binding.albumRecycler.setAdapter(adapter);
        binding.refreshAlbumList.setOnClickListener(v -> adapter.refreshDataFromNet());
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("相册");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        adapter.refreshDataFromNet();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}