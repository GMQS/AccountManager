package com.example.accountmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.oginotihiro.cropview.CropView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import Async.AsyncWork;
import DataBase.SQL;

public class CropImageActivity extends AppCompatActivity {

    private SQL dbAdapter = new SQL(this);
    private CropView cropView;
    private ImageView CropButton;
    private SharedPreferences mPref;
    private SharedPreferences.Editor editor;
    private String account;
    private String Title;
    private boolean CropWP;
    private ImageView close;
    private Uri uri;
    private int Width;
    private int Height;
    private boolean wallpaperOptions;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbAdapter.closeData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbAdapter.openData();
        setContentView(R.layout.activity_crop_image);
        cropView = findViewById(R.id.cropImageView);
        CropButton = findViewById(R.id.CropButton);
        close = findViewById(R.id.close);
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = mPref.edit();
        Intent intent = new Intent();

        account = getIntent().getStringExtra("PutAccount");
        CropWP = getIntent().getBooleanExtra("SetWallpaper", false);
        Title = getIntent().getStringExtra("TitleHeader");
        Width = getIntent().getIntExtra("Width", 0);
        Height = getIntent().getIntExtra("Height", 0);
        wallpaperOptions = getIntent().getBooleanExtra("OPTIONS", false);

        uri = Uri.parse(getIntent().getStringExtra("URI")); //test

        if (CropWP || wallpaperOptions) {

            cropView.of(uri)
                    .withAspect(Width, Height)
                    .withOutputSize(Width, Height)
                    .initialize(this);

        } else {
            cropView.of(uri)
                    .withAspect(16, 9)
                    .withOutputSize(Width, (int) (Width * 0.5625))
                    .initialize(this);

        }
        CropButton.setOnClickListener(view -> {
            //追加から飛んできたときの分岐
            if (!CropWP && Title == null) {
                Functions.tmpImageSave(cropView, getApplicationContext());

            } else if (Title != null) {
                Bitmap bmp;
                bmp = cropView.getOutput();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                try {
                    FileOutputStream fos = openFileOutput(Title + "_header.jpg", Context.MODE_PRIVATE);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    fos.write(baos.toByteArray());
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dbAdapter.changeHeader(Title, "", Title + "_header.jpg");
                editor.putBoolean("TitleHeader", true);

                if (mPref.getBoolean("AutoSync", false)) {
                    intent.putExtra("Job", "ADD_TITLE_HEADER");
                    intent.putExtra("TITLE_NAME", Title);
                    AsyncWork.enqueueWork(getApplicationContext(), intent);
                }
                intent.putExtra("CHANGE_TITLE_HEADER", true);


            } else if (wallpaperOptions) {

                Functions.tmpImageSave(cropView, getApplicationContext());

                //背景壁紙 MainActivity
            } else {
                Bitmap bmp;
                bmp = cropView.getOutput();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    FileOutputStream fos = openFileOutput("wallpaper.jpg", Context.MODE_PRIVATE);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    fos.write(baos.toByteArray());
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (mPref.getBoolean("AutoSync", false)) {
                    intent.putExtra("Job", "ADD_WALLPAPER");
                    AsyncWork.enqueueWork(getApplicationContext(), intent);
                }
                intent.putExtra("CHANGE_WALLPAPER", true);

            }
            //処理が正常に完了しましたよってリザルトに教える
            setResult(RESULT_OK, intent);
            editor.apply();
            finish();
        });


        close.setOnClickListener(view -> {
            finish();
        });

    }

}
