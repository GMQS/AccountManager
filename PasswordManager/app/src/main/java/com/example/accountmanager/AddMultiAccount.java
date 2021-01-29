package com.example.accountmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class AddMultiAccount extends AppCompatActivity {

    private final int REQUEST_CODE = 1; //ストレージアクセスパーミッション判定用
    private static final int RESULT_CHANGE = 1100;
    private ImageView preview;
    private ImageView preview_delete;
    private ImageView ErrorIconMail;
    private ImageView ErrorIconPass;
    private ImageView VisibleIcon;
    private EditText account;
    private EditText mail;
    private EditText password;
    private EditText url;
    private EditText memo;
    private Button button;
    private String strTitle = "";
    private String strAccount = "";
    private String strMail = "";
    private String strPassword = "";
    private String strUrl = "";
    private String strMemo = "";
    private SQL dbAdapter = new SQL(this);
    private String title;
    private ImageView Wallpaper;
    private Drawable Customicon;
    private int theme;
    private String BitmapStrings = "";
    private String LastID;
    private SharedPreferences mPref;
    private SharedPreferences.Editor editor;
    private TextInputLayout LayoutAccount;
    private TextInputLayout LayoutMail;
    private TextInputLayout LayoutPassword;

    private TextInputLayout LayoutUrl;
    private TextInputLayout LayoutMemo;
    private String TitleLabel;
    private String AccountLabel;
    private String MailLabel;
    private String PassLabel;
    private String UrlLabel;
    private String MemoLabel;
    private int AccountVisible;
    private int MailVisible;
    private int PassVisible;
    private int UrlVisible;
    private int MemoVisible;
    private boolean ShowPassword;
    private int RandomLength;
    private EditText CustomPassword_editText;
    private String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private ArrayList<String> SearchURL;
    private Bitmap bitmap;
    private static final int RESULT_PICK_IMAGEFILE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbAdapter.openData();

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = mPref.edit();

        theme = mPref.getInt("accent", R.style.NoActionBar);
        setTheme(mPref.getInt("accent", R.style.NoActionBar));
        setContentView(R.layout.activity_add_multi_account);

        Toolbar myToolbar = findViewById(R.id.my_toolbar7);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        strTitle = getIntent().getStringExtra("TITLE_STRING");
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button
        setTitle("アカウント追加");
        findViews();

        try {
            Functions.setWallpaper(getFilesDir(), this, Wallpaper, dbAdapter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (!mPref.getBoolean("Label", false)) {
            //editor.putString("TitleLabel", "タイトル");
            editor.putString("AccountLabel", "アカウント");
            editor.putString("MailLabel", "メールアドレス");
            editor.putString("PassLabel", "パスワード");
            editor.putString("UrlLabel", "URL");
            editor.putString("MemoLabel", "メモ");
            editor.remove("AccountHide");
            editor.remove("MailHide");
            editor.remove("PassHide");
            editor.remove("UrlHide");
            editor.remove("MemoHide");
            editor.apply();
        }


        switch (theme) {
            case R.style.NoActionBar:
                button.setTextColor(0xFFFF60A8);
                break;

            case R.style.NoActionBarCyan:
                button.setTextColor(0xFF00B8D4);
                break;

            case R.style.NoActionBarOrange:
                button.setTextColor(0xFFFF6D00);
                break;

            case R.style.NoActionBarGreen:
                button.setTextColor(0xFF64DD17);
                break;
        }

        account.setTypeface(Typeface.DEFAULT);
        mail.setTypeface(Typeface.DEFAULT);
        //password.setTypeface(Typeface.DEFAULT);
        url.setTypeface(Typeface.DEFAULT);
        memo.setTypeface(Typeface.DEFAULT);


        ShowPassword = mPref.getBoolean("ShowPassword", false);

        passwordVisibility(ShowPassword);

        init();

        //SharedPreferences pref = getSharedPreferences("com.example.passwordmanager_preferences", MODE_PRIVATE);
        boolean AutoURL = mPref.getBoolean("URLAutoComplete", true);
        boolean AutoPass = mPref.getBoolean("PassAutoComplete", false);

        if (AutoURL) {

            SearchURL = new ArrayList<>();

            //dbAdapter.openData();
            Cursor c = dbAdapter.searchTitle(strTitle);

            if (c.moveToFirst()) {
                do {
                    if (!c.getString(5).equals("")) {
                        Log.d("取得したURL", c.getString(5));
                        try {
                            SearchURL.add(c.getString(5));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } while (c.moveToNext());
            }
            c.close();
            //dbAdapter.closeData();

            if (!(SearchURL.size() == 0)) {
                String getURL = SearchURL.get(SearchURL.size() - 1);
                url.setText(getURL);
            }
        }


        if (AutoPass) {
            int RandomLength = mPref.getInt("PasswordLength", 15);
            password.setText(Functions.getRandomString(RandomLength, charSet));
        }


        VisibleIcon.setOnClickListener(view -> {

            password.requestFocus();
            if (ShowPassword) {
                passwordVisibility(false);
                ShowPassword = false;
            } else {
                passwordVisibility(true);
                ShowPassword = true;
            }
            password.setSelection(password.getText().length());

        });

        title = getIntent().getStringExtra("key3");

        //決定ボタンを押したときの動作
        button.setOnClickListener(v -> saveList());


        /*
        preview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(AddMultiAccount.this)
                        .setMessage("画像を削除しますか？")
                        .setPositiveButton("削除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                preview.setImageBitmap(null);
                                preview.setVisibility(View.GONE);
                                s2 = "";
                                editor.remove("BmpStrFlag");
                                editor.remove("BmpStr");
                                editor.remove("AfterCrop");
                                editor.apply();
                            }
                        })
                        .setNegativeButton("キャンセル", null)
                        .show();
                return true;
            }
        });

         */

        preview_delete.setOnClickListener(v -> {
            preview.setImageBitmap(null);
            preview.setVisibility(View.GONE);
            preview_delete.setVisibility(View.GONE);

            File file = new File(getFilesDir().getPath() + "/tmp_image.jpg");
            if (file.exists()) {
                file.delete();
            }
        });


        mail.addTextChangedListener(generalTextWatcher);
        password.addTextChangedListener(generalTextWatcher);

    }

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
            if (mail.getText().hashCode() == s.hashCode() && !mail.getText().toString().equals("")) {
                LayoutMail.setErrorEnabled(false);
                ErrorIconMail.setVisibility(View.GONE);
            } else if (password.getText().hashCode() == s.hashCode() && !password.getText().toString().equals("")) {
                LayoutPassword.setErrorEnabled(false);
                ErrorIconPass.setVisibility(View.GONE);
            }
        }
        //ここまで
    };


    @Override
    public void onBackPressed() {
        if (!account.getText().toString().equals("") || !mail.getText().toString().equals("") || !password.getText().toString().equals("") || !url.getText().toString().equals("") || !memo.getText().toString().equals("")) {
            new AlertDialog.Builder(AddMultiAccount.this)
                    .setMessage("登録を中断しますか？")
                    .setPositiveButton("中断", (dialogInterface, i) -> {
                        //Intent intent = new Intent(getApplication(), MultiAccountList.class);
                        //intent.putExtra("key", title);
                        //startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
                    })
                    .setNegativeButton("続行", null)
                    .show();
        } else {
            finish();
            overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (!account.getText().toString().equals("") || !mail.getText().toString().equals("") || !password.getText().toString().equals("") || !url.getText().toString().equals("") || !memo.getText().toString().equals("")) {
            new AlertDialog.Builder(AddMultiAccount.this)
                    .setMessage("登録を中断しますか？")
                    .setPositiveButton("中断", (dialogInterface, i) -> {
                        //Intent intent = new Intent(getApplication(), MultiAccountList.class);
                        //intent.putExtra("key", title);
                        //startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
                    })
                    .setNegativeButton("続行", null)
                    .show();
        } else {
            //Intent intent = new Intent(getApplication(), MultiAccountList.class);
            //intent.putExtra("key", title);
            //startActivity(intent);
            finish();
            overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
        }
        return true;
    }

    //オプショナルボタンの実装
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu3, menu);
        return true;
    }

    //オプショナルボタンの動作
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //項目の分岐処理
        switch (item.getItemId()) {

            case R.id.random:

                LayoutInflater inflater = getLayoutInflater();
                final View passwordDialogView = inflater.inflate(R.layout.password_castom, null);
                final SeekBar seekBar = passwordDialogView.findViewById(R.id.password_seekbar);
                CustomPassword_editText = passwordDialogView.findViewById(R.id.password_edittext);
                final ImageView imageView = passwordDialogView.findViewById(R.id.password_reflesh);
                final TextView textView = passwordDialogView.findViewById(R.id.password_seekbar_value);
                Spinner spinner = passwordDialogView.findViewById(R.id.password_spinner);
                RandomLength = mPref.getInt("PasswordLength", 15);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.password_array, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {
                            charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                        } else if (position == 1) {
                            charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$&*-+!?%^.:\\/{}[]=()<>";
                        } else if (position == 2) {
                            charSet = "0123456789";
                        }
                        CustomPassword_editText.setText(Functions.getRandomString(RandomLength, charSet));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        CustomPassword_editText.setText(Functions.getRandomString(RandomLength, charSet));
                    }
                });

                imageView.setOnClickListener(v -> CustomPassword_editText.setText(Functions.getRandomString(RandomLength, charSet)));


                seekBar.setProgress(RandomLength);
                textView.setText(String.valueOf(RandomLength));
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        RandomLength = progress;
                        CustomPassword_editText.setText(Functions.getRandomString(progress, charSet));
                        textView.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                new AlertDialog.Builder(this)
                        .setTitle("パスワード生成")
                        .setView(passwordDialogView)
                        .setPositiveButton("変更", (dialog, which) -> {
                            password.setText("");
                            password.setText(CustomPassword_editText.getText().toString());
                            password.requestFocus();
                            ShowPassword = true;
                            passwordVisibility(true);
                            CharSequence StrLength = password.getText();
                            password.setSelection(StrLength.length());
                        }).setNegativeButton("キャンセル", null)
                        .show();
                return true;

            case R.id.picture:

                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    //ファイル選択用のアクティビティの呼び出し&アクティビティ終了時の結果を受け取る処理
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, RESULT_PICK_IMAGEFILE);
                    } else {
                        Toast.makeText(getApplicationContext(), "画像選択アプリケーションが見つかりませんでした", Toast.LENGTH_SHORT).show();
                    }
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(AddMultiAccount.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(AddMultiAccount.this)
                            .setMessage("権限がないためストレージの画像ファイルにアクセスできません。権限を許可するためにアプリの設定を開きますか？")
                            .setPositiveButton("開く", (dialogInterface, i) -> {
                                String uriString = "package:" + getPackageName();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(uriString));
                                startActivity(intent);
                            })
                            .setNegativeButton("キャンセル", null)
                            .show();
                } else {
                    new AlertDialog.Builder(AddMultiAccount.this)
                            .setMessage("画像ファイルにアクセスするために権限を付与してください。この権限は画像ファイルの読み込み以外には使用しません。")
                            .setPositiveButton("確認", (dialogInterface, i) -> ActivityCompat.requestPermissions(AddMultiAccount.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_CODE))
                            .show();
                }
                return true;

            case R.id.HeadingEdit:
                Intent intent_ = new Intent(this, HeadingEditActivity.class);
                startActivity(intent_);

                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        File file = new File(getFilesDir().getPath() + "/tmp_image.jpg");
        if (file.exists()) {
            file.delete();
        }


        if (!mPref.getBoolean("Label", false)) {
            editor.remove("AccountHide");
            editor.remove("MailHide");
            editor.remove("PassHide");
            editor.remove("UrlHide");
            editor.remove("MemoHide");
        }
        editor.apply();
        dbAdapter.closeData();
    }


    @Override
    protected void onResume() {
        super.onResume();

        AccountLabel = mPref.getString("AccountLabel", "");
        MailLabel = mPref.getString("MailLabel", "");
        PassLabel = mPref.getString("PassLabel", "");
        UrlLabel = mPref.getString("UrlLabel", "");
        MemoLabel = mPref.getString("MemoLabel", "");

        LayoutAccount.setHint(AccountLabel);
        LayoutMail.setHint(MailLabel);
        LayoutPassword.setHint(PassLabel);
        LayoutUrl.setHint(UrlLabel);
        LayoutMemo.setHint(MemoLabel);

        LayoutAccount.setVisibility(View.VISIBLE);
        LayoutMail.setVisibility(View.VISIBLE);
        LayoutPassword.setVisibility(View.VISIBLE);
        LayoutUrl.setVisibility(View.VISIBLE);
        LayoutMemo.setVisibility(View.VISIBLE);
        VisibleIcon.setVisibility(View.VISIBLE);

        AccountVisible = 1;
        MailVisible = 1;
        PassVisible = 1;
        UrlVisible = 1;
        MemoVisible = 1;

        if (mPref.getBoolean("AccountHide", false)) {
            LayoutAccount.setVisibility(View.GONE);
            AccountVisible = 0;
        }
        if (mPref.getBoolean("MailHide", false)) {
            LayoutMail.setVisibility(View.GONE);
            MailVisible = 0;
            LayoutMail.setErrorEnabled(false);
            ErrorIconMail.setVisibility(View.GONE);
        }
        if (mPref.getBoolean("PassHide", false)) {
            LayoutPassword.setVisibility(View.GONE);
            VisibleIcon.setVisibility(View.GONE);
            PassVisible = 0;
            LayoutPassword.setErrorEnabled(false);
            ErrorIconPass.setVisibility(View.GONE);
        }
        if (mPref.getBoolean("UrlHide", false)) {
            LayoutUrl.setVisibility(View.GONE);
            UrlVisible = 0;
        }
        if (mPref.getBoolean("MemoHide", false)) {
            LayoutMemo.setVisibility(View.GONE);
            MemoVisible = 0;
        }


    }

    private void findViews() {
        account = findViewById(R.id.EditTextAccount);
        mail = findViewById(R.id.EditTextMail);
        password = findViewById(R.id.EditTextPassword);
        url = findViewById(R.id.EditTextUrl);
        memo = findViewById(R.id.EditTextMemo);
        LayoutAccount = findViewById(R.id.LayoutAccount);
        LayoutMail = findViewById(R.id.LayoutMail);
        LayoutPassword = findViewById(R.id.LayoutPassword);
        LayoutUrl = findViewById(R.id.textInputLayoutPersonName4);
        LayoutMemo = findViewById(R.id.LayoutMemo);
        preview = findViewById(R.id.Preview2);
        ErrorIconMail = findViewById(R.id.errorIcon_mail);
        ErrorIconPass = findViewById(R.id.errorIcon_password);
        button = findViewById(R.id.button);
        Wallpaper = findViewById(R.id.wallpaper);
        VisibleIcon = findViewById(R.id.visibleIcon);
        preview_delete = findViewById(R.id.Preview_close2);
    }

    private void init() {
        account.setText("");
        mail.setText("");
        password.setText("");
        memo.setText("");
        url.setText("");
        account.requestFocus();
    }

    private void passwordVisibility(final boolean visible) {
        if (visible) {
            password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            VisibleIcon.setImageResource(R.drawable.ic_baseline_visibility_off_24);
        } else {
            password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            VisibleIcon.setImageResource(R.drawable.ic_baseline_visibility_24);
        }

    }

    private void saveList() {
        Intent intent = new Intent();
        strAccount = account.getText().toString();
        strMail = mail.getText().toString();
        strPassword = password.getText().toString();
        strUrl = url.getText().toString();
        strMemo = memo.getText().toString();

        String imageFilePath = "";
        int CheckColor = 0;
        int LastID = 0;

        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean Validation = mPref.getBoolean("Validation", false);


        if (Validation) {
            boolean error = false;
            Animation iconPop = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.error_pop);
            if (strPassword.equals("") && PassVisible == 1) {
                password.requestFocus();
                LayoutPassword.setError("入力してください");
                ErrorIconPass.setVisibility(View.VISIBLE);
                ErrorIconPass.startAnimation(iconPop);
                error = true;
            }
            if (!Functions.isValidEmail(strMail) && MailVisible == 1) {
                mail.requestFocus();
                LayoutMail.setError("不正なアドレス形式");
                ErrorIconMail.setVisibility(View.VISIBLE);
                ErrorIconMail.startAnimation(iconPop);
                error = true;
            }
            if (strMail.equals("") && MailVisible == 1) {
                mail.requestFocus();
                LayoutMail.setError("入力してください");
                ErrorIconMail.setVisibility(View.VISIBLE);
                ErrorIconMail.startAnimation(iconPop);
                error = true;
            }

            if (!error) {
                addDatabase(imageFilePath, LastID, CheckColor, intent);
            }

        } else {
            addDatabase(imageFilePath, LastID, CheckColor, intent);
        }
    }


    //引数1:リクエスト時コード,引数2:結果コード,引数3:結果データ
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        Intent intent = new Intent(getApplication(), CropImage.class);
        if (resultCode == RESULT_OK && resultData != null) { //結果コードの変動なし　かつ　リザルトOK時に実行
            if (requestCode == RESULT_PICK_IMAGEFILE) {
                Uri uri; //URI変数の定義
                //結果データが空でないとき
                uri = resultData.getData(); //URIに結果データを代入
                ConstraintLayout Layout = findViewById(R.id.constraint_addAccount);

                int Width = Layout.getWidth();

                //Bitmap bitmap = Functions.decodeSampledBitmapFromFileDescriptor(uri, Width, Height, getContentResolver());

                //ByteArrayOutputStream baos = new ByteArrayOutputStream(); //ビットマップ画像をプリファレンスで保存するための準備 ファイル出力インスタンスの生成
                //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); //JPEG形式に圧縮
                //String bitmapStr = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT); //BAOSの文字列データを使用してビットマップ画像を文字列型変数に代入
                //editor.putString("BmpStr", bitmapStr);
                //editor.apply();
                intent.putExtra("URI", Objects.requireNonNull(uri).toString());
                intent.putExtra("Width", Width);
                startActivityForResult(intent, RESULT_CHANGE);
            } else if (requestCode == RESULT_CHANGE) {
                File file = new File(getFilesDir().getPath() + "/tmp_image.jpg");
                if (file.exists()) {
                    BufferedInputStream bis = null;
                    try {
                        bis = new BufferedInputStream(getApplicationContext().openFileInput("tmp_image.jpg"));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    bitmap = BitmapFactory.decodeStream(bis);
                    preview.setImageBitmap(bitmap); //ビットマップファイルを壁紙イメージビューにセットする
                    preview.setVisibility(View.VISIBLE);
                    preview_delete.setVisibility(View.VISIBLE);
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

                //ファイル選択用のアクティビティの呼び出し&アクティビティ終了時の結果を受け取る処理
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, RESULT_PICK_IMAGEFILE);
                } else {
                    Toast.makeText(getApplicationContext(), "画像選択アプリケーションが見つかりませんでした", Toast.LENGTH_SHORT).show();
                }
            } else {
                // パーミッションのリクエストに対して「許可しない」
                // または以前のリクエストで「二度と表示しない」にチェックを入れられた状態で
                // 「許可しない」を押されていると、必ずここに呼び出されます。

                new AlertDialog.Builder(getApplicationContext())
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

    private void addDatabase(String imageFilePath, int LastID, int CheckColor, Intent intent) {
        Random rand = new Random();

        int[] randColors = {0xFFD50000,
                0xFFC51162,
                0xFFAA00FF,
                0xFF304FFE,
                0xFF00B8D4,
                0xFF00BFA5,
                0xFF00C853,
                0xFFAEEA00,
                0xFFFFD600,
                0xFFFF6D00,
                0xFF3E2723,
                0xFF263238};

        int RandomColor = randColors[rand.nextInt(randColors.length)];

        File file = new File(getFilesDir().getPath() + "/" + strTitle + "_header.jpg");
        File imageFile = new File(getFilesDir().getPath() + "/tmp_image.jpg");
        if (file.exists()) {
            imageFilePath = strTitle + "_header.jpg";
        }

        Cursor lastIdCursor = dbAdapter.getLastID();
        if (lastIdCursor.moveToFirst()) {
            do {
                LastID = lastIdCursor.getInt(0);
                CheckColor = lastIdCursor.getInt(8);

            } while (lastIdCursor.moveToNext());
        }
        lastIdCursor.close();

        while (RandomColor == CheckColor) {
            RandomColor = randColors[rand.nextInt(randColors.length)];
        }
        String lastIdStr;
        if (LastID != 0) {
            int i = LastID + 1;
            lastIdStr = String.valueOf(i);
        } else {
            //最初のレコード登録時
            lastIdStr = "0";
        }

        if (imageFile.exists()) {
            File renameFile = new File(getFilesDir().getPath() + "/header" + lastIdStr + ".jpg");
            imageFile.renameTo(renameFile);
            imageFilePath = "header" + lastIdStr + ".jpg";
        }

        if (mPref.getBoolean("AutoName", false)) {
            autoRename();
        }

        dbAdapter.saveData(strTitle, strAccount, strMail, strPassword, strUrl, strMemo, imageFilePath, RandomColor);
        dbAdapter.saveLabel(AccountLabel, AccountVisible, MailLabel, MailVisible, PassLabel, PassVisible, UrlLabel, UrlVisible, MemoLabel, MemoVisible);

        if (mPref.getBoolean("AutoSync", false)) {
            autoBackup();
        }

        intent.putExtra("ACCOUNT_NAME", strAccount);
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);

    }

    private void autoBackup() {
        Intent serviceIntent = new Intent();
        serviceIntent.putExtra("Job", "ADD_MULTI_ACCOUNT");
        AsyncWork.enqueueWork(getApplicationContext(), serviceIntent);
    }

    private void autoRename() {
        if (strAccount.equals("")) {
            ArrayList<String> list = new ArrayList<>();
            Cursor c = dbAdapter.searchTitle(strTitle);
            if (c.moveToFirst()) {
                do {
                    list.add(c.getString(2));
                } while (c.moveToNext());
            }
            c.close();
            for (int i = 1; ; i++) {
                String cast = Integer.toString(i);
                String str = "アカウント " + cast;
                if (!list.contains(str)) {
                    strAccount = str;
                    list.clear();
                    break;
                }
            }
        }
    }

}