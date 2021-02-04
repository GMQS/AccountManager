package com.example.accountmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;

import DataBase.SQL;

public class FilterTestActivity extends AppCompatActivity {

    private final int REQUEST_CODE = 1; //ストレージアクセスパーミッション判定用
    private static final int RESULT_CHANGE = 3000;
    private static final int RESULT_PICK_WALLPAPER = 1000; //画像変更時のIDの監視用 壁紙
    private ImageView wallpaper;
    private ImageView canvas;
    private ImageView confirmBG;
    private SeekBar seekBar;
    private SQL dbAdapter = new SQL(this);
    private Button setWPButton;
    private Button deleteWPButton;
    private Button confirmButton;
    private TextView value;
    private androidx.appcompat.widget.Toolbar toolbar;
    private int filterValue;
    private boolean change = false;
    private boolean delete = false;
    private Intent intent;
    File file;
    File tmpFile;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed(){
        if (change) {
            new AlertDialog.Builder(this)
                    .setMessage("変更内容を保存しますか？")
                    .setPositiveButton("保存", (dialogInterface12, i) -> {
                        confirm();
                        setResult(RESULT_OK,intent);
                        finish();
                    })
                    .setNegativeButton("キャンセル", (dialogInterface1, i) -> {
                        finish();
                    })
                    .show();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tmpFile.exists()){
            tmpFile.delete();
        }
        dbAdapter.closeData();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_test);
        dbAdapter.openData();
        findViews();
        file = new File(getFilesDir().getPath() + "/wallpaper.jpg");
        tmpFile = new File(getFilesDir().getPath() + "/tmp_image.jpg");


        try {
            filterValue = Functions.setWallpaper(getApplicationContext(), wallpaper);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        seekBar.setMax(255);
        seekBar.setProgress(filterValue);
        value.setText(String.valueOf(filterValue));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                wallpaper.setColorFilter(Color.argb(progress, 0, 0, 0));
                value.setText(String.valueOf(progress));
                filterValue = progress;
                change = true;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ConfirmButtonVisibility();
            }
        });

        setWPButton.setOnClickListener(v -> {

            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, RESULT_PICK_WALLPAPER);
                } else {
                    Toast.makeText(getApplicationContext(), "画像選択アプリケーションが見つかりませんでした", Toast.LENGTH_SHORT).show();
                }
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setMessage("権限がないためストレージの画像ファイルにアクセスできません。権限を許可するためにアプリの設定を開きますか？")
                        .setPositiveButton("開く", (dialogInterface141, i1) -> {
                            String uriString = "package:" + getPackageName();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(uriString));
                            startActivity(intent);
                        })
                        .setNegativeButton("キャンセル", null)
                        .show();

            } else {
                new AlertDialog.Builder(this)
                        .setMessage("画像ファイルにアクセスするために権限を付与してください。この権限は画像ファイルの読み込み以外には使用しません。")
                        .setPositiveButton("確認", (dialogInterface1412, i12) -> ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_CODE))
                        .show();
            }

        });
        if (file.exists() || tmpFile.exists()) {
            deleteWPButton.setVisibility(View.VISIBLE);
        }

        deleteWPButton.setOnClickListener(v -> {
            Resources r = getResources();
            Bitmap defaultBmp = BitmapFactory.decodeResource(r, R.drawable.header_default);
            defaultBmp = Bitmap.createScaledBitmap(defaultBmp, wallpaper.getWidth(), wallpaper.getHeight(),false);
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            wallpaper.setImageBitmap(defaultBmp);
            wallpaper.setColorFilter(Color.argb(filterValue, 0, 0, 0));
            Animation goneAnimation = AnimationUtils.loadAnimation(this, R.anim.delete_fab);
            deleteWPButton.startAnimation(goneAnimation);
            deleteWPButton.setVisibility(View.GONE);
            setWPButton.setText("壁紙登録");
            delete = true;
            change = true;
            ConfirmButtonVisibility();
        });

        confirmButton.setOnClickListener(v -> {
            confirm();
            setResult(RESULT_OK, intent);
            finish();
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        Intent intent = new Intent(getApplicationContext(), CropImageActivity.class);
        if (resultCode == RESULT_OK && resultData != null) {
            if (requestCode == RESULT_PICK_WALLPAPER) { //結果コードの変動なし　かつ　リザルトOK時に実行

                Uri uri; //URI変数の定義
                //結果データが空でないとき
                uri = resultData.getData(); //URIに結果データを代入
                int Width = canvas.getWidth();
                int Height = canvas.getHeight();
                Log.d("Viewのサイズ幅", String.valueOf(canvas.getWidth()));
                Log.d("Viewのサイズ高さ", String.valueOf(canvas.getHeight()));

                intent.putExtra("OPTIONS", true);
                intent.putExtra("Width", Width);
                intent.putExtra("Height", Height);
                intent.putExtra("URI", Objects.requireNonNull(uri).toString());//test

                startActivityForResult(intent, RESULT_CHANGE);
            } else if (requestCode == RESULT_CHANGE) {
                try {
                    Functions.setTmpWallpaper(this, wallpaper, filterValue);
                    deleteWPButton.setVisibility(View.VISIBLE);
                    Animation visibleAnimation = AnimationUtils.loadAnimation(this, R.anim.fab_pop);
                    deleteWPButton.startAnimation(visibleAnimation);
                    setWPButton.setText("壁紙変更");
                    change = true;
                    delete = false;
                    ConfirmButtonVisibility();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE) {
            // requestPermissionsで設定した順番で結果が格納されています。
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 許可されたので処理を続行

                // You can use the API that requires the permission.
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, RESULT_PICK_WALLPAPER);
                } else {
                    Toast.makeText(getApplicationContext(), "画像選択アプリケーションが見つかりませんでした", Toast.LENGTH_SHORT).show();
                }


            } else {
                // パーミッションのリクエストに対して「許可しない」
                // または以前のリクエストで「二度と表示しない」にチェックを入れられた状態で
                // 「許可しない」を押されていると、必ずここに呼び出されます。

                new AlertDialog.Builder(this)
                        .setMessage("権限がないためストレージの画像ファイルにアクセスできません。権限を許可するためにアプリの設定を開きますか？")
                        .setPositiveButton("開く", (dialogInterface, i) -> {
                            String uriString = "package:" + getPackageName();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(uriString));
                            startActivity(intent);
                        })
                        .setNegativeButton("キャンセル", null)
                        .show();

            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void findViews() {
        wallpaper = findViewById(R.id.wallpaper);
        seekBar = findViewById(R.id.alphaSeekbar);
        setWPButton = findViewById(R.id.wallpaperButton);
        deleteWPButton = findViewById(R.id.deleteButton);
        confirmButton = findViewById(R.id.confirmButton);
        value = findViewById(R.id.value);
        toolbar = findViewById(R.id.toolbar);
        confirmBG = findViewById(R.id.confirmButtonBG);
        canvas = findViewById(R.id.canvas);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        setTitle("壁紙設定");
        intent = new Intent();
    }

    private void confirm() {
        dbAdapter.saveWallpaper(null, filterValue, "FILTER", false);
        if (tmpFile.exists()) {
            tmpFile.renameTo(file);
        }
        if (delete) {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private void ConfirmButtonVisibility(){
        if(confirmBG.getVisibility() == View.GONE) {
            confirmBG.setVisibility(View.VISIBLE);
            confirmButton.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.fab_pop);
            confirmBG.startAnimation(animation);
            confirmButton.startAnimation(animation);
        }
    }
}