package com.example.accountmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import DataBase.SQL;

public class OtherSettingsActivity extends AppCompatActivity {


    public static final int RESULT_CHANGE = 1001;
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
        setContentView(R.layout.settings_activity);

        Toolbar myToolbar = findViewById(R.id.my_toolbar5);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);

        Wallpaper = findViewById(R.id.wallpaper);

        try {
            Functions.setWallpaper(this, Wallpaper);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == RESULT_OK && resultData != null) {
            try {
                Functions.setWallpaper(this, Wallpaper);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
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

    public static class SettingsFragment extends PreferenceFragmentCompat {

        int Choice;
        int Put;
        private SharedPreferences mPref;
        private SharedPreferences.Editor editor;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            mPref = PreferenceManager.getDefaultSharedPreferences(requireContext());
            editor = mPref.edit();


            SeekBarPreference seekBarPreference = findPreference("PasswordLength");

            if (seekBarPreference != null) {
                seekBarPreference.setShowSeekBarValue(true);
                seekBarPreference.setMax(30);
                seekBarPreference.setMin(5);

            }

            Preference radioPreference = findPreference("AccentColor");

            if (radioPreference != null) {
                radioPreference.setOnPreferenceClickListener(preference -> {
                    Put = mPref.getInt("Color_Choice", 0);
                    final String Title[] = new String[]{
                            "ピンク",
                            "シアン",
                            "オレンジ",
                            "ライトグリーン"};
                    new AlertDialog.Builder(requireContext())
                            .setTitle("テーマカラー選択")
                            .setSingleChoiceItems(Title, Put, (dialogInterface, button) -> {
                                if (button == 0) {
                                    Choice = 0;
                                }
                                if (button == 1) {
                                    Choice = 1;
                                }
                                if (button == 2) {
                                    Choice = 2;
                                }
                                if (button == 3) {
                                    Choice = 3;
                                }
                            })
                            .setPositiveButton("決定", (dialogInterface, i) -> {
                                if (Choice == 0) {
                                    editor.putInt("Color_Choice", 0);
                                    editor.putInt("accent", R.style.NoActionBar);
                                }
                                if (Choice == 1) {
                                    editor.putInt("Color_Choice", 1);
                                    editor.putInt("accent", R.style.NoActionBarCyan);
                                }
                                if (Choice == 2) {
                                    editor.putInt("Color_Choice", 2);
                                    editor.putInt("accent", R.style.NoActionBarOrange);
                                }
                                if (Choice == 3) {
                                    editor.putInt("Color_Choice", 3);
                                    editor.putInt("accent", R.style.NoActionBarGreen);
                                }
                                editor.apply();
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // 起動しているActivityをすべて削除し、新しいタスクでMainActivityを起動する
                                startActivity(intent);
                            })
                            .show();
                    return false;
                });
            }

            Preference SyncPreference = findPreference("Sync");
            if (SyncPreference != null) {
                SyncPreference.setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(getActivity(), CloudSyncActivity.class);
                    startActivity(intent);
                    return false;
                });
            }


            Preference ChangePinPreference = findPreference("PIN");
            if (ChangePinPreference != null) {
                ChangePinPreference.setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(getContext(), PasscodeActivity.class);
                    intent.putExtra("ChangePIN", true);
                    startActivity(intent);
                    return false;
                });
            }

            if (Functions.checkBiometricSupport(requireContext()).equals("使用可能")) {
                Preference bioAuthPreference = findPreference("BioAuth");
                if (bioAuthPreference != null) {
                    bioAuthPreference.setVisible(true);
                }
            }

            Preference AutoSyncPreference = findPreference("AutoSync");
            if (AutoSyncPreference != null) {
                AutoSyncPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean UnlockSetting = mPref.getBoolean("UnlockSetting", false);
                    if (!UnlockSetting) {
                        new AlertDialog.Builder(requireActivity())
                                //.setTitle("警告")
                                .setMessage("DropBoxクラウドサービスと接続していないため有効化できません。接続しますか？")
                                .setPositiveButton("接続", (dialogInterface, i) -> {
                                    Intent intent = new Intent(getContext(), CloudSyncActivity.class);
                                    startActivity(intent);
                                })
                                .setNegativeButton("キャンセル", (dialogInterface, i) -> {
                                    editor.putBoolean("AutoSync", false);
                                    editor.apply();
                                })
                                .show();
                    } else {
                        return true;
                    }

                    return false;
                });
            }

            Preference wallpaperPreference = findPreference("changeWallpaper");
            if (wallpaperPreference != null) {
                wallpaperPreference.setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(getContext(), FilterTestActivity.class);
                    startActivityForResult(intent,RESULT_CHANGE);
                    return false;
                });
            }


            Preference deletePreference = findPreference("Alldelete");
            if (deletePreference != null) {
                deletePreference.setOnPreferenceClickListener(preference -> {
                    new AlertDialog.Builder(requireContext())
                            .setMessage("本当に全て削除しますか？")
                            .setPositiveButton("削除", (dialogInterface, i) -> {
                                SQL dbAdapter = new SQL(getActivity()); //SQLデータベース定義
                                dbAdapter.openData(); //DBの読み込み(読み書きの方)
                                Cursor c = dbAdapter.getData();


                                if (c.moveToFirst()) {
                                    do {
                                        Log.d("削除したファイル", c.getString(0));
                                    } while (c.moveToNext());
                                }
                                c.close();
                                dbAdapter.allDelete();  // DBのレコードを全削除
                                File dir = new File(requireContext().getFilesDir().getPath());
                                if (allDeleteFiles(dir)) {
                                    Toast.makeText(getContext(), "全てのアカウント情報を削除しました", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "画像ファイルの削除に失敗しました", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("キャンセル", (dialogInterface, i) -> {
                            })
                            .show();

                    return false;
                });
            }

        }

        /**
         * @param dir 削除したいファイルが入ってるディレクトリのファイルパス
         * @return true 成功 / false 失敗
         */

        private boolean allDeleteFiles(File dir) {
            Context context = requireActivity().getApplicationContext();
            boolean Success = true;
            if (dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        if (!files[i].toString().equals(context.getFilesDir().getPath() + "/wallpaper.jpg"))
                            if (files[i].delete()) {
                                //削除成功
                            } else {
                                Success = false;
                            }
                    }
                }
            }
            return Success;
        }

    }
}