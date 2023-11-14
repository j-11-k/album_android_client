package com.example.SharedAlbum.ui.local_frag;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionListenerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.SharedAlbum.PicDataCenter;
import com.example.SharedAlbum.databinding.PicViewFragBinding;
import com.example.SharedAlbum.databinding.PicViewLayoutBinding;
import com.example.SharedAlbum.ui.CustomVIew.PinchImageView;

import java.util.List;

public class ImagePagerActivity extends FragmentActivity {
    public final static String PagerText = "pager_transition_name";

    PicViewLayoutBinding binding;
    private TextView header, description;
    List<PicDataCenter.PicData> picDataList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Window window = getWindow();
//        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        picDataList = PicDataCenter.picToImageViewer;
        PicDataCenter.picToImageViewer = null;
        binding = PicViewLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewPager2 viewPager = binding.pager;

        header = binding.picHeader;
        description = binding.picDescription;
        postponeEnterTransition();
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this, this::startPostponedEnterTransition);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                PicDataCenter.PicData data = picDataList.get(position);
                header.setText(data.name);
                description.setText(data.description);

            }

        });
        viewPager.setAdapter(pagerAdapter);
        viewPager.getFocusedChild();

        Intent intent = getIntent();
        viewPager.setCurrentItem(intent.getIntExtra("position", 0), false);

        getWindow().getSharedElementEnterTransition().addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                super.onTransitionEnd(transition);
                viewPager.setOffscreenPageLimit(2);
            }
        });
        getWindow().setSharedElementReturnTransition(null);
        getWindow().setSharedElementReenterTransition(null);
    }

    @Override
    public void onBackPressed() {
        binding.pager.setTransitionName(null);
        super.onBackPressed();
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        Runnable r;

        public ScreenSlidePagerAdapter(FragmentActivity fa, Runnable r) {
            super(fa);
            this.r = r;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return new ScreenSlidePageFragment(picDataList.get(position).path, r);
        }

        @Override
        public int getItemCount() {
            return picDataList.size();
        }
    }

    public static class ScreenSlidePageFragment extends Fragment {
        final String picUri;
        PicViewFragBinding binding;
        Runnable r;
        public PinchImageView imageView;

        public ScreenSlidePageFragment(String picUri, Runnable r) {
            this.picUri = picUri;
            this.r = r;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            binding = PicViewFragBinding.inflate(inflater, container, false);
            View root = binding.getRoot();
            imageView = binding.fragmentContainingImage;
            Glide.with(this).load(picUri).listener(new RequestListener<>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    r.run();
                    return false;
                }
                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    r.run();
                    return false;
                }
            }).thumbnail(.25f).into(imageView);
            imageView.setOnClickListener(v -> getActivity().onBackPressed());
            return root;
        }

        @Override
        public void onPause() {
            super.onPause();
            imageView.reset();
        }
    }
}
