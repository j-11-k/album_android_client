package com.example.SharedAlbum.ui.local_frag;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.SharedAlbum.AlbumData;
import com.example.SharedAlbum.ItemClickCallback;
import com.example.SharedAlbum.MainActivity;
import com.example.SharedAlbum.OnFragBack;
import com.example.SharedAlbum.PicDataCenter;
import com.example.SharedAlbum.R;
import com.example.SharedAlbum.databinding.FragmentHomeBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class LocalFragment extends Fragment {

    private FragmentHomeBinding binding;
    ImageAdapter adapter;

    ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void exitMultiMode(){
        adapter.selected = null;
        getActionBar().setTitle("图片");
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getMainActivity().switchBottomBar(false);
        adapter.notifyDataSetChanged();
        getMainActivity().invalidateOptionsMenu();
        getMainActivity().setOnFragBack(null);
    }

    private void enterMultiMode(){
        getActionBar().setTitle(String.format("已选择 %d 项", adapter.selected.size()));
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getMainActivity().switchBottomBar(true);
        adapter.notifyDataSetChanged();
        getMainActivity().invalidateOptionsMenu();
        getMainActivity().setOnFragBack(new OnFragBack() {
            @Override
            public void onBackPressedFromFrag() {
                exitMultiMode();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            exitMultiMode();
            return true;
        } else if (item.getItemId() == R.id.menu_top_select_all) {
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.top_actionbar_local, menu);
        menu.findItem(R.id.menu_top_select_all).setVisible(adapter.selected != null);
    }

    MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setHasOptionsMenu(true);
        binding.localPhotoList.setLayoutManager(new GridLayoutManager(getContext(), 4));
        adapter = new ImageAdapter(getActivity(), PicDataCenter.GetAllPicData(getContext()), new ItemClickCallback() {
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
        binding.localPhotoList.setAdapter(adapter);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getMainActivity().getBottomBar().getMenu().findItem(R.id.menu_bottom_album_download).setVisible(false);
        getMainActivity().getBottomBar().getMenu().findItem(R.id.add_to_gallery).setVisible(true);
        getMainActivity().getBottomBar().setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.add_to_gallery) {
                if (adapter.selected.isEmpty()) {
                    Toast.makeText(getContext(),"请选择至少一张图片", Toast.LENGTH_SHORT).show();
                } else {
                    createShareToDialog().show();
                }
            } else if (item.getItemId() == R.id.menu_bottom_album_delete){
                //todo 广新
            }
            return true;
        }
        );
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("图片");
    }

    private BottomSheetDialog createShareToDialog(){
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View view = getLayoutInflater().inflate(R.layout.choose_album_dialog_bottom_sheet, null);
        List<AlbumData> albums = AlbumData.getAlbumList();
        if (albums.isEmpty()){
            TextView textView = view.findViewById(R.id.share_to_album_hint);
            textView.setText("尚未加入任何相册，请先创建相册");
        } else {
            RecyclerView recyclerView = view.findViewById(R.id.albums_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),DividerItemDecoration.HORIZONTAL));
            recyclerView.setAdapter(new RecyclerView.Adapter<AlbumHolder>() {
                @NonNull
                @Override
                public AlbumHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_cover_holder,parent,false);
                    view.setLayoutParams(new ViewGroup.LayoutParams(450, 510));
                    return new AlbumHolder(view);
                }
                @Override
                public void onBindViewHolder(@NonNull AlbumHolder holder, int position) {
                    holder.albumText.setText(albums.get(position).albumName);
                    Glide.with(getContext()).load(albums.get(position).coverThumbnailUrl).centerCrop().into(holder.albumCover);
                    holder.itemView.setOnClickListener(v -> {
                        Toast.makeText(getContext(), String.format("成功添加%d项照片",adapter.selected.size()), Toast.LENGTH_SHORT).show();//todo
                        dialog.dismiss();
                        adapter.selected = null;
                        getActionBar().setTitle("图片");
                        getActionBar().setDisplayHomeAsUpEnabled(false);
                        getMainActivity().switchBottomBar(false);
                        getActivity().invalidateOptionsMenu();
                        adapter.notifyDataSetChanged();
                    });
                }
                @Override
                public int getItemCount() {
                    return albums.size();
                }
            });
        }
        dialog.setContentView(view);
        return dialog;
    }

    static class AlbumHolder extends RecyclerView.ViewHolder{
        public final ImageView albumCover;
        public final TextView albumText;

        public AlbumHolder(@NonNull View itemView) {
            super(itemView);
            albumCover = itemView.findViewById(R.id.album_cover);
            albumText = itemView.findViewById(R.id.album_name);
        }
    }

}

