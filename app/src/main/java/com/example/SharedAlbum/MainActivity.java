package com.example.SharedAlbum;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.SharedAlbum.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private OnFragBack onFragBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController,false);

        Toolbar myToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    public void switchBottomBar(boolean showAction){
        if (showAction){
            binding.bottomActionBar.setVisibility(View.VISIBLE);
            binding.navView.setVisibility(View.GONE);
        } else {
            binding.navView.setVisibility(View.VISIBLE);
            binding.bottomActionBar.setVisibility(View.GONE);
        }
    }

    public BottomAppBar getBottomBar(){
        return binding.bottomActionBar;
    }

    public void setOnFragBack(OnFragBack onFragBack) {
        this.onFragBack = onFragBack;
    }

    @Override
    public void onBackPressed() {
        if (onFragBack != null){
            onFragBack.onBackPressedFromFrag();
            return;
        }
        super.onBackPressed();
    }
}