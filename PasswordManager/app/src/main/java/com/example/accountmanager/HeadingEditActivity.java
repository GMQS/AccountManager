package com.example.accountmanager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.FileNotFoundException;

import DataBase.SQL;

public class HeadingEditActivity extends AppCompatActivity {

    private SQL dbAdapter = new SQL(this);
    private EditText AccountLabel;
    private EditText MailLabel;
    private EditText PassLabel;
    private EditText UrlLabel;
    private EditText MemoLabel;
    private CheckBox AccountHide;
    private CheckBox MailHide;
    private CheckBox PassHide;
    private CheckBox UrlHide;
    private CheckBox MemoHide;
    private SharedPreferences mPref;
    private SharedPreferences.Editor editor;
    private String ID;
    private String AccountStr;
    private String MailStr;
    private String PassStr;
    private String UrlStr;
    private String MemoStr;
    private int AccountVisible = 1;
    private int MailVisible = 1;
    private int PassVisible = 1;
    private int UrlVisible = 1;
    private int MemoVisible = 1;
    private ImageView Wallpaper; //背景用イメージビューの定義
    private Bitmap bmp;

    private TextWatcher generalTextWatcher = new TextWatcher() {

        //ここからテキストウォッチャーメソッド
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (AccountLabel.getText().hashCode() == s.hashCode() && !AccountLabel.equals("")) {

            } else if (MailLabel.getText().hashCode() == s.hashCode() && !MailLabel.equals("")) {

            } else if (PassLabel.getText().hashCode() == s.hashCode() && !PassLabel.equals("")) {

            } else if (UrlLabel.getText().hashCode() == s.hashCode() && !UrlLabel.equals("")) {

            } else if (MemoLabel.getText().hashCode() == s.hashCode() && !MemoLabel.equals("")) {

            }
        }
        //ここまで
    };

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
        editor = mPref.edit();
        setTheme(mPref.getInt("accent", R.style.NoActionBar));
        setContentView(R.layout.activity_heading_edit);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        findViews();

        Functions.setWallpaper(this,Wallpaper);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("ラベル編集");

        ID = getIntent().getStringExtra("SendLabel");


        if (ID == null) {
            AccountLabel.setText(mPref.getString("AccountLabel", "アカウント"));
            MailLabel.setText(mPref.getString("MailLabel", "メールアドレス"));
            PassLabel.setText(mPref.getString("PassLabel", "パスワード"));
            UrlLabel.setText(mPref.getString("UrlLabel", "URL"));
            MemoLabel.setText(mPref.getString("MemoLabel", "メモ"));
        } else {
            getLabelInfo();
        }

        AccountLabel.addTextChangedListener(generalTextWatcher);
        MailLabel.addTextChangedListener(generalTextWatcher);
        PassLabel.addTextChangedListener(generalTextWatcher);
        UrlLabel.addTextChangedListener(generalTextWatcher);
        MemoLabel.addTextChangedListener(generalTextWatcher);


        if (ID == null) {
            if (mPref.getBoolean("AccountHide", false)) {
                AccountVisible = 0;
                AccountHide.setChecked(false);
            }
            if (mPref.getBoolean("MailHide", false)) {
                MailVisible = 0;
                MailHide.setChecked(false);
            }
            if (mPref.getBoolean("PassHide", false)) {
                PassVisible = 0;
                PassHide.setChecked(false);
            }
            if (mPref.getBoolean("UrlHide", false)) {
                UrlVisible = 0;
                UrlHide.setChecked(false);
            }
            if (mPref.getBoolean("MemoHide", false)) {
                MemoVisible = 0;
                MemoHide.setChecked(false);
            }
        } else {
            if (AccountVisible == 0) {
                AccountHide.setChecked(false);
            }
            if (MailVisible == 0) {
                MailHide.setChecked(false);
            }
            if (PassVisible == 0) {
                PassHide.setChecked(false);
            }
            if (UrlVisible == 0) {
                UrlHide.setChecked(false);
            }
            if (MemoVisible == 0) {
                MemoHide.setChecked(false);
            }
        }


        AccountHide.setOnClickListener(view -> {
            boolean check = AccountHide.isChecked();
            if (!check) {
                AccountVisible = 0;
            } else {
                AccountVisible = 1;
            }
        });

        MailHide.setOnClickListener(view -> {
            boolean check = MailHide.isChecked();
            if (!check) {
                MailVisible = 0;
            } else {
                MailVisible = 1;
            }
        });

        PassHide.setOnClickListener(view -> {
            boolean check = PassHide.isChecked();
            if (!check) {
                PassVisible = 0;
            } else {
                PassVisible = 1;
            }
        });

        UrlHide.setOnClickListener(view -> {
            boolean check = UrlHide.isChecked();
            if (!check) {
                UrlVisible = 0;
            } else {
                UrlVisible = 1;
            }
        });

        MemoHide.setOnClickListener(view -> {
            boolean check = MemoHide.isChecked();
            if (!check) {
                MemoVisible = 0;
            } else {
                MemoVisible = 1;
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    //オプショナルボタンの実装
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.label_edit, menu);
        return true;
    }

    //オプショナルボタンの動作
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //項目の分岐処理
        switch (item.getItemId()) {

            case R.id.Confirm:
                if (ID == null) {
                    editor.putString("AccountLabel", AccountLabel.getText().toString());
                    editor.putString("MailLabel", MailLabel.getText().toString());
                    editor.putString("PassLabel", PassLabel.getText().toString());
                    editor.putString("UrlLabel", UrlLabel.getText().toString());
                    editor.putString("MemoLabel", MemoLabel.getText().toString());
                    if (AccountVisible == 0) {
                        editor.putBoolean("AccountHide", true);
                    }
                    if (MailVisible == 0) {
                        editor.putBoolean("MailHide", true);
                    }
                    if (PassVisible == 0) {
                        editor.putBoolean("PassHide", true);
                    }
                    if (UrlVisible == 0) {
                        editor.putBoolean("UrlHide", true);
                    }
                    if (MemoVisible == 0) {
                        editor.putBoolean("MemoHide", true);
                    }
                    if (AccountVisible == 1) {
                        editor.remove("AccountHide");
                    }
                    if (MailVisible == 1) {
                        editor.remove("MailHide");
                    }
                    if (PassVisible == 1) {
                        editor.remove("PassHide");
                    }
                    if (UrlVisible == 1) {
                        editor.remove("UrlHide");
                    }
                    if (MemoVisible == 1) {
                        editor.remove("MemoHide");
                    }

                    editor.apply();
                } else {
                    getLabelString();
                    dbAdapter.changeLabel(AccountStr, AccountVisible, MailStr, MailVisible, PassStr, PassVisible, UrlStr, UrlVisible, MemoStr, MemoVisible, ID);
                }
                finish();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


    private void findViews() {
        AccountLabel = findViewById(R.id.AccountLabel);
        MailLabel = findViewById(R.id.MailLabel);
        PassLabel = findViewById(R.id.PassLabel);
        UrlLabel = findViewById(R.id.UrlLabel);
        MemoLabel = findViewById(R.id.MemoLabel);
        AccountHide = findViewById(R.id.checkBox_account);
        MailHide = findViewById(R.id.checkBox_mail);
        PassHide = findViewById(R.id.checkBox_pass);
        UrlHide = findViewById(R.id.checkBox_url);
        MemoHide = findViewById(R.id.checkBox_memo);
        Wallpaper = findViewById(R.id.wallpaper);

    }

    private void getLabelInfo() {
        Cursor c = dbAdapter.ShowLabelInfo(ID);
        if (c.moveToFirst()) {

            do {
                AccountLabel.setText(c.getString(1));
                MailLabel.setText(c.getString(2));
                PassLabel.setText(c.getString(3));
                UrlLabel.setText(c.getString(4));
                MemoLabel.setText(c.getString(5));
                AccountVisible = c.getInt(6);
                MailVisible = c.getInt(7);
                PassVisible = c.getInt(8);
                UrlVisible = c.getInt(9);
                MemoVisible = c.getInt(10);

            } while (c.moveToNext());
        }
        c.close();
    }

    private void getLabelString() {
        AccountStr = AccountLabel.getText().toString();
        MailStr = MailLabel.getText().toString();
        PassStr = PassLabel.getText().toString();
        UrlStr = UrlLabel.getText().toString();
        MemoStr = MemoLabel.getText().toString();
    }
}