package com.example.accountmanager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.preference.PreferenceManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.FileMetadata;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;

public class CloudSync extends AppCompatActivity {

    private SQL dbAdapter = new SQL(this); //SQLデータベース定義
    private ImageView Wallpaper; //背景用イメージビューの定義
    private DropboxManager mDropboxManager;
    private SharedPreferences mPref;
    private ImageView cloudicon;
    private Button Backup_button;
    private Button Restore_button;
    private String token;
    private boolean Access = false;
    private CoordinatorLayout layout;
    private TextView timeStamp;
    private Bitmap bmp; //ビットマップ画像(背景用)の定義
    private SharedPreferences.Editor editor;
    private int theme;
    private boolean First = false;
    private boolean Restore = false;
    private boolean Failed = false;
    private boolean Canceled = false;
    private ArrayList<Boolean> JudgeList = new ArrayList();
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbAdapter.closeData();
        tmpFilesDelete();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbAdapter.openData();

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = mPref.edit();
        theme = mPref.getInt("accent", R.style.NoActionBar);
        setTheme(mPref.getInt("accent", R.style.NoActionBar));
        setContentView(R.layout.activity_cloud_sync);

        Toolbar myToolbar = findViewById(R.id.my_toolbar8);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        switch (theme) {
            case R.style.NoActionBar:
                progressDialog = new ProgressDialog(this, R.style.ProgressDialogPink);
                break;

            case R.style.NoActionBarCyan:
                progressDialog = new ProgressDialog(this, R.style.ProgressDialogCyan);
                break;

            case R.style.NoActionBarOrange:
                progressDialog = new ProgressDialog(this, R.style.ProgressDialogOrange);
                break;

            case R.style.NoActionBarGreen:
                progressDialog = new ProgressDialog(this, R.style.ProgressDialogGreen);
                break;
        }

        Backup_button = findViewById(R.id.Backup);
        Restore_button = findViewById(R.id.Restore);
        layout = findViewById(R.id.Layout_cloud);
        timeStamp = findViewById(R.id.Timestamp);
        Wallpaper = findViewById(R.id.wallpaper);
        cloudicon = findViewById(R.id.Cloudicon);
        progressBar = findViewById(R.id.progress_bar);


        First = getIntent().getBooleanExtra("STEP", false);
        Restore = getIntent().getBooleanExtra("Restore", false);

        setTitle("DropBoxクラウド");
        Backup_button.setEnabled(false);
        Restore_button.setEnabled(false);
        Backup_button.setTextColor(Color.argb(180, 120, 120, 120));
        Restore_button.setTextColor(Color.argb(180, 120, 120, 120));

        try {
            Functions.setWallpaper(getFilesDir(),this,Wallpaper,dbAdapter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        String Timestamp = mPref.getString("TimeStamp", "----/--/-- --:--:--");
        timeStamp.setText("最終バックアップ" + "\n" + Timestamp);

        if (!mPref.contains("access_token")) {
            new AlertDialog.Builder(CloudSync.this)
                    .setMessage("最初にブラウザでDropBoxのフォルダアクセスを許可してください。")
                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mDropboxManager = new DropboxManager(CloudSync.this);
                            mDropboxManager.startAuthenticate();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            if (First && !Restore) {
                                Toast.makeText(CloudSync.this, "接続に失敗したためサービスを利用できません", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else if (Restore) {
                                Toast.makeText(CloudSync.this, "接続に失敗したためサービスを利用できません", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), Passcode.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    })
                    .show();
        } else {
            token = mPref.getString("access_token", null);
            mDropboxManager = new DropboxManager(CloudSync.this, token);
            Access = true;
        }
        Backup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backupProcess();
            }
        });
        Restore_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restoreProcess();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }


    //オプショナルボタンの実装
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cloud_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //項目の分岐処理
        switch (item.getItemId()) {
            case R.id.connect:

                new AlertDialog.Builder(CloudSync.this)
                        //.setTitle("接続")
                        .setMessage("DropBoxサービスと接続しますか？")
                        .setPositiveButton("接続", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mDropboxManager = new DropboxManager(CloudSync.this);
                                mDropboxManager.startAuthenticate();
                                editor.putBoolean("Connection", true);
                                editor.apply();
                            }
                        })
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
        Cursor c = dbAdapter.getWallpaper();
        if (c.moveToFirst()) {
            FilterValue = c.getInt(0);
        }
        c.close();

        File file = new File(getFilesDir().getPath() + "/wallpaper.jpg");
        if (file.exists()) {
            try {
                BufferedInputStream bis = new BufferedInputStream(this.openFileInput("wallpaper.jpg"));
                bmp = BitmapFactory.decodeStream(bis);
                Wallpaper.setImageBitmap(bmp);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        Wallpaper.setColorFilter(Color.argb(FilterValue, 0, 0, 0));

         */

        if (!(mDropboxManager == null) && token == null) {
            if (First) {
                if (!(mDropboxManager.getAccessToken() == null)) {
                    token = mDropboxManager.getAccessToken();
                    mDropboxManager = new DropboxManager(CloudSync.this, token);
                    editor.putString("access_token", token);
                    editor.putBoolean("AutoSync", true);
                    editor.putBoolean("UnlockSetting", true);
                    editor.apply();
                    Toast.makeText(this, "自動バックアップを有効にしました", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    mDropboxManager.getAccessToken();
                    editor.putBoolean("UnlockSetting", false);
                    editor.apply();
                    new AlertDialog.Builder(CloudSync.this)
                            //.setTitle("接続失敗")
                            .setMessage("DropBoxサービスとの接続に失敗しました。リトライしますか？")
                            .setPositiveButton("リトライ", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mDropboxManager.startAuthenticate();
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialogInterface) {
                                    Toast.makeText(CloudSync.this, "接続に失敗したためサービスを利用できません", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(CloudSync.this, "接続に失敗したためサービスを利用できません", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .show();
                }
            } else if (Restore) {
                if (!(mDropboxManager.getAccessToken() == null)) {
                    token = mDropboxManager.getAccessToken();
                    mDropboxManager = new DropboxManager(CloudSync.this, token);
                    editor.putString("access_token", token);
                    editor.putBoolean("UnlockSetting", true);
                    editor.apply();
                    progressDialog.setMessage("復元実行中。データが破損する恐れがあるためアプリを終了しないでください。");
                    progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {

                            Snackbar.make(layout, "復元を中断しました", Snackbar.LENGTH_SHORT).show();
                            Canceled = true;
                            editor.putBoolean("RestoreOK", false);
                            editor.apply();
                            finish();
                        }
                    });
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    concurrentExecutor(false);


                } else {
                    mDropboxManager.getAccessToken();
                    editor.putBoolean("UnlockSetting", false);
                    editor.apply();
                    new AlertDialog.Builder(CloudSync.this)
                            .setMessage("DropBoxサービスとの接続に失敗しました。リトライしますか？")
                            .setPositiveButton("リトライ", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mDropboxManager.startAuthenticate();
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialogInterface) {
                                    Toast.makeText(CloudSync.this, "接続に失敗したためサービスを利用できません", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(CloudSync.this, "接続に失敗したためサービスを利用できません", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    if (Restore) {
                                        intent = new Intent(getApplicationContext(), Passcode.class);
                                    }
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .show();
                }

            } else {
                if (mDropboxManager.getAccessToken() != null) {
                    token = mDropboxManager.getAccessToken();
                    mDropboxManager = new DropboxManager(CloudSync.this, token);
                    editor.putString("access_token", token);
                    editor.putBoolean("UnlockSetting", true);
                    editor.apply();
                    Access = true;
                    Snackbar.make(layout, "DropBoxに接続しました", Snackbar.LENGTH_SHORT).show();
                } else {
                    mDropboxManager.getAccessToken();
                    editor.putBoolean("UnlockSetting", false);
                    editor.apply();
                    Backup_button.setEnabled(false);
                    Restore_button.setEnabled(false);
                    Backup_button.setTextColor(Color.argb(180, 120, 120, 120));
                    Restore_button.setTextColor(Color.argb(180, 120, 120, 120));
                    cloudicon.setImageResource(R.drawable.ic_baseline_cloud_off_24);
                    new AlertDialog.Builder(CloudSync.this)
                            //.setTitle("接続失敗")
                            .setMessage("DropBoxサービスとの接続に失敗しました。リトライしますか？")
                            .setPositiveButton("リトライ", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mDropboxManager.startAuthenticate();
                                }
                            })
                            .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Snackbar.make(layout, "接続に失敗したためサービスを利用できません", Snackbar.LENGTH_SHORT).show();
                                }
                            })
                            .show();
                }
            }
        } else if (mPref.getBoolean("Connection", false) && mDropboxManager.getAccessToken() != null) {
            token = mDropboxManager.getAccessToken();
            mDropboxManager = new DropboxManager(CloudSync.this, token);
            editor.putString("access_token", token);
            editor.putBoolean("UnlockSetting", true);
            editor.apply();
            Access = true;
            editor.remove("Connection");
            editor.apply();
            Snackbar.make(layout, "DropBoxに接続しました", Snackbar.LENGTH_SHORT).show();
        } else if (mPref.getBoolean("Connection", false) && mDropboxManager.getAccessToken() == null) {
            new AlertDialog.Builder(this)
                    .setMessage("DropBoxサービスとの接続に失敗しました。リトライしますか？")
                    .setPositiveButton("リトライ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mDropboxManager.startAuthenticate();
                        }
                    })
                    .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Snackbar.make(layout, "接続に失敗したためサービスを利用できません", Snackbar.LENGTH_SHORT).show();
                            Backup_button.setEnabled(false);
                            Restore_button.setEnabled(false);
                            Backup_button.setTextColor(Color.argb(180, 120, 120, 120));
                            Restore_button.setTextColor(Color.argb(180, 120, 120, 120));
                        }
                    })
                    .show();
            editor.remove("Connection");
            editor.apply();
        }

        if (Access && token == null) {
            editor.putBoolean("UnlockSetting", false);
            editor.apply();
            Backup_button.setEnabled(false);
            Restore_button.setEnabled(false);
            Backup_button.setTextColor(Color.argb(180, 120, 120, 120));
            Restore_button.setTextColor(Color.argb(180, 120, 120, 120));
            cloudicon.setImageResource(R.drawable.ic_baseline_cloud_off_24);
            new AlertDialog.Builder(CloudSync.this)
                    //.setTitle("接続失敗")
                    .setMessage("DropBoxサービスとの接続に失敗しました。リトライしますか？")
                    .setPositiveButton("リトライ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mDropboxManager.startAuthenticate();
                        }
                    })
                    .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Snackbar.make(layout, "接続に失敗したためサービスを利用できません", Snackbar.LENGTH_SHORT).show();

                        }
                    })
                    .show();
        }

        if (Access && !(token == null)) {
            Backup_button.setEnabled(true);
            Restore_button.setEnabled(true);
            switch (theme) {
                case R.style.NoActionBar:
                    Backup_button.setTextColor(0xFFFF60A8);
                    Restore_button.setTextColor(0xFFFF60A8);
                    break;

                case R.style.NoActionBarCyan:
                    Backup_button.setTextColor(0xFF00B8D4);
                    Restore_button.setTextColor(0xFF00B8D4);
                    break;

                case R.style.NoActionBarOrange:
                    Backup_button.setTextColor(0xFFFF6D00);
                    Restore_button.setTextColor(0xFFFF6D00);
                    break;

                case R.style.NoActionBarGreen:
                    Backup_button.setTextColor(0xFF64DD17);
                    Restore_button.setTextColor(0xFF64DD17);
                    break;
            }
            cloudicon.setImageResource(R.drawable.ic_baseline_cloud_done_24);
        }
    }

    /**
     * @param Backup バックアップするか否か trueでバックアップ　falseでリストア
     */
    private void concurrentExecutor(final boolean Backup) {
        final Looper looper = Looper.getMainLooper();
        final Handler handler = new Handler(looper);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            //Do something background
            String deviceDatabaseFilePath = getDatabasePath("AccountDB.db").getPath(); // Android端末内のSQLiteのデータ
            String cloudDatabaseFilePath = "tmp/AccountDB.db"; //Dropboxに保存される際のファイル名

            //ダウンロード中のtmpファイル
            String tmpDatabaseFilePath = getDatabasePath("AccountDB.db.tmp").getPath();

            String deviceFilePath = getFilesDir().getPath() + "/";
            String deviceTmpFilePath = getFilesDir().getPath() + "/tmp/";
            String deviceWallpaperFilePath = getFilesDir().getPath() + "/wallpaper.jpg"; // Android端末内の壁紙データ
            String cloudWallpaperFilePath = "tmp/wallpaper.jpg";

            if (Backup) {

                Cursor c = dbAdapter.getData();
                if (c.moveToFirst() && !Canceled) {
                    do {
                        File file = new File(getFilesDir().getPath() + "/header" + c.getString(0) + ".jpg");
                        File file_ = new File(getFilesDir().getPath() + "/" + c.getString(1) + "_header.jpg");
                        if (file_.exists()) {
                            String InternalFilePath = getFilesDir().getPath() + "/" + c.getString(1) + "_header.jpg";
                            String DbxFilePath = "tmp/" + c.getString(1) + "_header.jpg";
                            JudgeList.add(mDropboxManager.backup(InternalFilePath, DbxFilePath));
                        }
                        if (file.exists()) {
                            String deviceFilePath_Header = getFilesDir().getPath() + "/header" + c.getString(0) + ".jpg";
                            String cloudFilePath_Header = "tmp/header" + c.getString(0) + ".jpg";
                            //全てのアップロードが成功したか確認するために配列に戻り値を格納
                            JudgeList.add(mDropboxManager.backup(deviceFilePath_Header, cloudFilePath_Header));
                        }

                    } while (c.moveToNext());
                }
                c.close();
                JudgeList.add(mDropboxManager.backup(deviceDatabaseFilePath, cloudDatabaseFilePath));
                JudgeList.add(mDropboxManager.backup(deviceWallpaperFilePath, cloudWallpaperFilePath));
                //整合チェック&不整合ファイル削除
                JudgeList.add(mDropboxManager.SearchDelete(deviceFilePath));

                if (!Canceled) {
                    JudgeList.add(mDropboxManager.moveFiles());
                }

                for (int i = 0; i < JudgeList.size(); i++) {
                    if (!JudgeList.get(i)) {
                        Failed = true;
                        break;
                    }
                }
            } else {
                JudgeList.add(mDropboxManager.restore(deviceTmpFilePath, tmpDatabaseFilePath));

                for (int i = 0; i < JudgeList.size(); i++) {
                    if (!JudgeList.get(i)) {
                        Failed = true;
                        break;
                    }
                }
            }
            JudgeList.clear();

            handler.post(() -> {
                //タスク終了時処理 UI更新
                if (!Canceled) {

                    if (Backup) { //バックアップ処理
                        if (!Failed) {
                            //success
                            progressDialog.dismiss();
                            Date date = new Date();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            String date2 = simpleDateFormat.format(date);
                            editor.putString("TimeStamp", date2);
                            editor.apply();
                            String Timestamp = mPref.getString("TimeStamp", "----/--/-- --:--:--");
                            timeStamp.setText("最終バックアップ" + "\n" + Timestamp);
                            //Toast.makeText(getApplicationContext(), "アカウント情報をバックアップしました", Toast.LENGTH_SHORT).show();
                            Snackbar.make(layout, "アカウント情報をバックアップしました", Snackbar.LENGTH_SHORT).show();

                        } else {
                            //failed
                            progressDialog.dismiss();
                            new AlertDialog.Builder(CloudSync.this)
                                    //.setTitle("接続")
                                    .setMessage("アカウント情報のバックアップに失敗しました。リトライしますか？")
                                    .setPositiveButton("リトライ", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            backupProcess();
                                        }
                                    })
                                    .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Snackbar.make(layout, "アカウント情報のバックアップに失敗しました", Snackbar.LENGTH_SHORT).show();
                                        }
                                    })
                                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialogInterface) {
                                            Snackbar.make(layout, "アカウント情報のバックアップに失敗しました", Snackbar.LENGTH_SHORT).show();
                                        }
                                    })
                                    .show();

                        }
                    } else { //復元処理
                        if (!Failed) {
                            //success
                            renameFiles_Device();
                            progressDialog.dismiss();

                            Functions.brokenFilesDelete(getFilesDir().getPath(),dbAdapter);

                            new AlertDialog.Builder(CloudSync.this)
                                    //.setTitle("接続")
                                    .setMessage("アカウント情報の復元に成功しました。再起動します。")
                                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Intent intent = new Intent(getApplicationContext(), Passcode.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 起動しているActivityをすべて削除し、新しいタスクで起動する
                                            if (Restore) {
                                                editor.putBoolean("RestoreOK", true);
                                                editor.apply();
                                            }
                                            startActivity(intent);
                                        }
                                    })
                                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialogInterface) {
                                            Intent intent = new Intent(getApplicationContext(), Passcode.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 起動しているActivityをすべて削除し、新しいタスクで起動する
                                            if (Restore) {
                                                editor.putBoolean("RestoreOK", true);
                                                editor.apply();
                                            }
                                            startActivity(intent);
                                        }
                                    })
                                    .show();

                        } else {
                            //復元失敗時処理
                            progressDialog.dismiss();
                            new AlertDialog.Builder(CloudSync.this)
                                    //.setTitle("接続")
                                    .setMessage("アカウント情報の復元に失敗しました。リトライしますか？")
                                    .setPositiveButton("リトライ", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            restoreProcess();
                                        }
                                    })
                                    .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Snackbar.make(layout, "アカウント情報の復元に失敗しました", Snackbar.LENGTH_SHORT).show();
                                        }
                                    })
                                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialogInterface) {
                                            Snackbar.make(layout, "アカウント情報の復元に失敗しました", Snackbar.LENGTH_SHORT).show();
                                        }
                                    })
                                    .show();
                            if (Restore) {
                                editor.putBoolean("RestoreOK", false);
                                editor.apply();
                            }
                        }
                    }
                }
                Failed = false;
            });
        });
    }

    private void backupProcess() {

        Canceled = false;
        progressDialog.setMessage("バックアップ実行中。データが破損する恐れがあるためアプリを終了しないでください。");
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                dbAdapter.openData();
                Snackbar.make(layout, "バックアップを中断しました", Snackbar.LENGTH_SHORT).show();
                Canceled = true;
            }
        });
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        concurrentExecutor(true);

    }

    private void restoreProcess() {
        Canceled = false;
        progressDialog.setMessage("復元実行中。データが破損する恐れがあるためアプリを終了しないでください。");
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Snackbar.make(layout, "復元を中断しました", Snackbar.LENGTH_SHORT).show();
                Canceled = true;
            }
        });
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        concurrentExecutor(false);
    }

    //一時ファイル削除
    private void tmpFilesDelete() {
        /*
        File[] existingFilesList = existingFiles.listFiles();
        for (int i = 0; i < existingFilesList.length; i++) {
            if (existingFilesList[i].toString().contains("tmp")) {
                File DeleteFile = new File(existingFilesList[i].getPath());
                DeleteFile.delete();
            }
        }
         */

        File evacDB = new File(getDatabasePath("AccountDB.db.tmp").getPath());
        File existingFiles = new File(getFilesDir().getPath() + "/tmp");
        File[] checkFiles = existingFiles.listFiles();
        if (checkFiles != null) {
            for (File checkFile : checkFiles) {
                if (checkFile.exists()) {
                    checkFile.delete();
                }
            }
            existingFiles.delete();
        }
        evacDB.delete();

        File file = new File(getFilesDir().getPath() + "/AccountDB.db"); //なんか謎にファイル生成されるバグ用
        if (file.exists()) {
            JudgeList.add(file.delete());
        }
    }

    //ファイル巻き戻しメソッド(デバイス内)
    private void renameFiles_Device() {
        File evacDB = new File(getDatabasePath("AccountDB.db.tmp").getPath());
        File existingDB = new File(getDatabasePath("AccountDB.db").getPath());
        //File existingFiles = new File(getFilesDir().getPath());
        File existingFiles = new File(getFilesDir().getPath() + "/tmp");
        File[] existingFilesList = existingFiles.listFiles();

        for (int i = 0; i < existingFilesList.length; i++) {
            /*
            if (existingFilesList[i].toString().contains("tmp")) {
                File file = new File(existingFilesList[i].getPath());
                String fileName = file.getName();
                fileName = file.getParent() + File.separator + fileName.replaceFirst("\\..*", ".jpg");
                file.renameTo(new File(fileName));
            }
             */

            File file = new File(existingFilesList[i].getPath());
            File file_ = new File(getFilesDir().getPath() + "/" + existingFilesList[i].getName());
            file.renameTo(file_);
        }
        evacDB.renameTo(existingDB);
    }
}

