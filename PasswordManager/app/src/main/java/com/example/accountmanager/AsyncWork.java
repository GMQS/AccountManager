package com.example.accountmanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Image;
import android.os.Build;
import android.widget.Toast;

import com.dropbox.core.DbxException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

public class AsyncWork extends JobIntentService {
    static final int JOB_ID = 1000;
    private SharedPreferences mPref;
    private SharedPreferences.Editor editor;
    private ArrayList<Boolean> Judgelist = new ArrayList<>();
    private SQL dbAdapter = new SQL(this);
    private boolean Failed = false;
    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder builder;

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, AsyncWork.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        String selectJob = intent.getStringExtra("Job");
        String title = intent.getStringExtra("TITLE_NAME");
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        String token = mPref.getString("access_token", null);
        String deviceFilePath = getFilesDir().getPath() + "/";
        DropboxManager mDropboxManager = new DropboxManager(AsyncWork.this, token);

        notificationManager = NotificationManagerCompat.from(this);
        builder = new NotificationCompat.Builder(this, "2");


        String srcFilePath_WP = getFilesDir().getPath() + "/wallpaper.jpg"; // Android端末内の壁紙ファイルパス
        String dstFilePath_WP = "tmp/wallpaper.jpg"; // DropBoxに保存される壁紙ファイルパス
        String srcFilePath = getDatabasePath("AccountDB.db").getPath(); // Android端末内のSQLiteファイルパス
        String dstFilePath = "tmp/AccountDB.db"; // Dropboxに保存されるSQLiteファイルパス
        createProgressNotificationChannel();
        progressNotification(builder, notificationManager);
        dbAdapter.openData();
        //壁紙変更時
        switch (Objects.requireNonNull(selectJob)) {
            case "ADD_WALLPAPER":
                Judgelist.add(mDropboxManager.backup(srcFilePath_WP, dstFilePath_WP));
                updateProgress(50, 2);
                //アカウント新規追加時
                break;
            case "ADD_DATA": {
                Cursor c = dbAdapter.getLastID();
                if (c.moveToFirst()) {
                    File file = new File(getFilesDir().getPath() + "/header" + c.getString(0) + ".jpg");
                    if (file.exists()) {
                        String srcFilePath_Header = getFilesDir().getPath() + "/header" + c.getString(0) + ".jpg";
                        String dstFilePath_Header = "tmp/header" + c.getString(0) + ".jpg";
                        Judgelist.add(mDropboxManager.backup(srcFilePath_Header, dstFilePath_Header));
                        updateProgress(50, 2);
                    }
                }
                c.close();
                break;
            }
            case "ADD_MULTI_ACCOUNT": {
                Cursor c = dbAdapter.getLastID();
                if (c.moveToFirst()) {
                    File file = new File(getFilesDir().getPath() + "/header" + c.getString(0) + ".jpg");
                    if (file.exists()) {
                        String srcFilePath_Header = getFilesDir().getPath() + "/header" + c.getString(0) + ".jpg";
                        String dstFilePath_Header = "tmp/header" + c.getString(0) + ".jpg";
                        mDropboxManager.backup(srcFilePath_Header, dstFilePath_Header);
                        updateProgress(50, 2);
                    }
                }
                c.close();
                break;
            }
            case "CHANGE_DATA_INFO":
                String myID = intent.getStringExtra("id");
                String srcFilePath_Header = getFilesDir().getPath() + "/header" + myID + ".jpg";
                String dstFilePath_Header = "tmp/header" + myID + ".jpg"; // Dropboxに保存される際のファイル名

                File file = new File(getFilesDir().getPath() + "/header" + myID + ".jpg");
                if (file.exists()) {
                    Judgelist.add(mDropboxManager.backup(srcFilePath_Header, dstFilePath_Header));
                    updateProgress(50, 2);
                }
                break;
            case "ADD_TITLE_HEADER":
                File ImageFile = new File(getFilesDir().getPath() + "/" + title + "_header.jpg");
                if (ImageFile.exists()) {
                    String InternalFilePathStr = ImageFile.getPath();
                    String DbxFilePathStr = "tmp/" + title + "_header.jpg";
                    Judgelist.add(mDropboxManager.backup(InternalFilePathStr, DbxFilePathStr));
                    updateProgress(50, 2);
                }
                break;
        }
        Judgelist.add(mDropboxManager.backup(srcFilePath, dstFilePath));
        updateProgress(70, 2);
        mDropboxManager.moveFiles();
        updateProgress(90, 2);
        //整合チェック&不整合ファイル削除
        Judgelist.add(mDropboxManager.SearchDelete(deviceFilePath));

        for (int i = 0; i < Judgelist.size(); i++) {
            if (!Judgelist.get(i)) {
                Failed = true;
                break;
            }
        }
        Judgelist.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //ワーク終了時処理
        progressDismiss(builder, notificationManager, 2);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "バックアップ完了";
            String description = "アカウント情報をDropboxにバックアップしました。";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createProgressNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "自動バックアップを開始";
            String description = "バックアップ中...";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("2", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void progressNotification(NotificationCompat.Builder builder, NotificationManagerCompat notificationManager) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClassName(getApplicationContext().getPackageName(), Passcode.class.getName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setContentTitle("自動バックアップを開始しました")
                .setContentText("バックアップ中...")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_baseline_backup_24)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        int PROGRESS_MAX = 100;
        int PROGRESS_CURRENT = 0;
        builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
        notificationManager.notify(2, builder.build());
        //progressDismiss(builder,notificationManager,2);

    }

    private void progressDismiss(NotificationCompat.Builder builder, NotificationManagerCompat notificationManager, int notificationId) {
        if (!Failed) {
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String date2 = simpleDateFormat.format(date);
            editor = mPref.edit();
            editor.putString("TimeStamp", date2);
            editor.apply();

            builder.setContentTitle("バックアップが終了しました")
                    .setContentText("ステータス:バックアップ完了")
                    .setSmallIcon(R.drawable.ic_baseline_cloud_done_24)
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setProgress(0, 0, false);
        } else {
            builder.setContentTitle("バックアップが終了しました")
                    .setContentText("ステータス:バックアップ失敗")
                    .setSmallIcon(R.drawable.ic_baseline_cloud_done_24)
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setProgress(0, 0, false);
        }
        notificationManager.notify(notificationId, builder.build());
    }

    private void updateProgress(int progress, int notifyId) {
        builder.setProgress(100, progress, false);
        notificationManager.notify(notifyId, builder.build());
    }

    private void finishNotification() {
        if (!Failed) {
            //Success
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String date2 = simpleDateFormat.format(date);
            editor = mPref.edit();
            editor.putString("TimeStamp", date2);
            editor.apply();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName(getApplicationContext().getPackageName(), Passcode.class.getName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "1")
                    .setSmallIcon(R.drawable.ic_baseline_cloud_done_24)
                    .setContentTitle("バックアップ完了")
                    .setContentText("アカウント情報をDropboxにバックアップしました。")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationManager.notify(1, builder.build());

        } else {
            //Failed
            Toast.makeText(getApplicationContext(), "アカウント情報のバックアップに失敗しました", Toast.LENGTH_SHORT).show();
        }
    }


}
