package com.example.accountmanager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.util.Objects;

import DataBase.SQL;

public class AppSettingsActivity extends AppCompatActivity {

    public ImageView Wallpaper;
    private SharedPreferences mPref;
    private SQL dbAdapter = new SQL(this);


    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbAdapter.closeData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbAdapter.openData();
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(mPref.getInt("accent", R.style.NoActionBar));
        setTitle("アプリの設定");
        setContentView(R.layout.activity_app_settings);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);

        Wallpaper = findViewById(R.id.wallpaper);

        Functions.setWallpaper(this, Wallpaper);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == RESULT_OK && resultData != null) {
            Functions.setWallpaper( this, Wallpaper);
        }


    }

    //オプショナルボタンの実装
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.other_settings, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
    }
}