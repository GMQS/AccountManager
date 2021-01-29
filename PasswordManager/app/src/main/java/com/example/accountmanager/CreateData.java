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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import com.google.android.material.textfield.TextInputLayout;

public class CreateData extends AppCompatActivity {

    private final int REQUEST_CODE = 1; //ストレージアクセスパーミッション判定用
    private static final int RESULT_PICK_IMAGEFILE = 1000; //画像変更時のIDの監視用
    private static final int RESULT_CHANGE = 1100;
    private static final String SHOWCASE_ID = "create";
    private Button Accept;
    private EditText title;
    private EditText account;
    private EditText mail;
    private EditText password;
    private EditText url;
    private EditText memo;
    private ImageView preview;
    private ImageView preview_delete;
    private ImageView ErrorIconMail;
    private ImageView ErrorIconPass;
    private SQL dbAdapter = new SQL(this);
    private Drawable Customicon;
    private boolean ShowPassword;
    private int theme;
    private String strTitle;
    private String strAccount;
    private String strMail;
    private String strPassword;
    private String strUrl;
    private String strMemo;
    private TextInputLayout layoutTitle;
    private TextInputLayout layoutAccount;
    private TextInputLayout layoutMail;
    private TextInputLayout layoutPassword;
    private TextInputLayout layoutUrl;
    private TextInputLayout layoutMemo;
    private ConstraintLayout Layout;
    private SharedPreferences mPref;
    private SharedPreferences.Editor editor;
    private ImageView Wallpaper; //背景用イメージビューの定義
    private ImageView VisibleIcon;
    private Bitmap bitmap;
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
    private EditText CustomPassword_editText;
    private int RandomLength;
    private String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

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
            if (mail.getText().hashCode() == s.hashCode() && mail.getText().toString().equals("")) {
                layoutMail.setErrorEnabled(false);
                ErrorIconMail.setVisibility(View.GONE);
            } else if (password.getText().hashCode() == s.hashCode() && !password.getText().toString().equals("")) {
                layoutPassword.setErrorEnabled(false);
                ErrorIconPass.setVisibility(View.GONE);
            }
        }
        //ここまで
    };


    @Override
    public void onBackPressed() {
        if (!title.getText().toString().equals("") || !account.getText().toString().equals("") || !mail.getText().toString().equals("") || !password.getText().toString().equals("") || !url.getText().toString().equals("") || !memo.getText().toString().equals("")) {
            new AlertDialog.Builder(CreateData.this)
                    .setMessage("登録を中断しますか？")
                    .setPositiveButton("中断", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                            overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
                        }
                    })
                    .setNegativeButton("続行", null)
                    .show();
        } else {
            finish();
            overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbAdapter.openData();

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = mPref.edit();
        theme = mPref.getInt("accent", R.style.NoActionBar);
        setTheme(mPref.getInt("accent", R.style.NoActionBar));
        setContentView(R.layout.activity_create_data);
        Toolbar myToolbar = findViewById(R.id.my_toolbar6);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("アカウント情報登録");
        findViews();

        try {
            Functions.setWallpaper(getFilesDir(), this, Wallpaper, dbAdapter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (!mPref.getBoolean("Label", false)) {
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
                Accept.setTextColor(0xFFFF60A8);
                break;

            case R.style.NoActionBarCyan:
                Accept.setTextColor(0xFF00B8D4);
                break;

            case R.style.NoActionBarOrange:
                Accept.setTextColor(0xFFFF6D00);
                break;

            case R.style.NoActionBarGreen:
                Accept.setTextColor(0xFF64DD17);
                break;
        }


        ShowPassword = mPref.getBoolean("ShowPassword", false);

        passwordVisibility(ShowPassword);

        mail.addTextChangedListener(generalTextWatcher);
        password.addTextChangedListener(generalTextWatcher);

        init();

        boolean AutoPass = mPref.getBoolean("PassAutoComplete", false);

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

        preview_delete.setOnClickListener(v -> {
            preview.setImageBitmap(null);
            preview.setVisibility(View.GONE);
            preview_delete.setVisibility(View.GONE);

            File file = new File(getFilesDir().getPath() + "/tmp_image.jpg");
            if (file.exists()) {
                file.delete();
            }

        });

        //決定ボタンを押したときの動作
        Accept.setOnClickListener(v -> saveList());

    }

    @Override
    public boolean onSupportNavigateUp() {
        if (!title.getText().toString().equals("") || !account.getText().toString().equals("") || !mail.getText().toString().equals("") || !password.getText().toString().equals("") || !url.getText().toString().equals("") || !memo.getText().toString().equals("")) {
            new AlertDialog.Builder(CreateData.this)
                    .setMessage("登録を中断しますか？")
                    .setPositiveButton("中断", (dialogInterface, i) -> {
                        finish();
                        overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
                    })
                    .setNegativeButton("続行", null)
                    .show();
        } else {
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

        /*
        new Handler().post(() -> {
            //view
            View PASS = findViewById(R.id.random);
            View Pic = findViewById(R.id.picture);
            View Label = findViewById(R.id.HeadingEdit);

            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(500); // half second between each showcase view

            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(CreateData.this, SHOWCASE_ID);
            sequence.setConfig(config);

            //tutorial
            sequence.addSequenceItem(
                    new MaterialShowcaseView.Builder(CreateData.this)
                            .setTarget(PASS)
                            .setContentText("ランダムパスワードの生成ができます。")
                            .setDismissText("次へ")
                            .build()
            );

            sequence.addSequenceItem(
                    new MaterialShowcaseView.Builder(CreateData.this)
                            .setTarget(Pic)
                            .setContentText("アカウント情報に画像を追加できます。")
                            .setDismissText("次へ")
                            .build()
            );

            sequence.addSequenceItem(
                    new MaterialShowcaseView.Builder(CreateData.this)
                            .setTarget(Label)
                            .setContentText("登録する情報のラベルを編集できます。")
                            .setDismissText("確認")
                            .build()
            );
            sequence.start();
        });

         */

        return true;
    }

    //オプショナルボタンの動作
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //項目の分岐処理
        switch (item.getItemId()) {

            case R.id.random:

                LayoutInflater inflater = getLayoutInflater();
                View passwordDialogView = inflater.inflate(R.layout.password_castom, null);
                SeekBar seekBar = passwordDialogView.findViewById(R.id.password_seekbar);
                CustomPassword_editText = passwordDialogView.findViewById(R.id.password_edittext);
                ImageView imageView = passwordDialogView.findViewById(R.id.password_reflesh);
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
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(CreateData.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(CreateData.this)
                            .setMessage("権限がないためストレージの画像ファイルにアクセスできません。権限を許可するためにアプリの設定を開きますか？")
                            .setPositiveButton("開く", (dialogInterface, i) -> {
                                String uriString = "package:" + getPackageName();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(uriString));
                                startActivity(intent);
                            })
                            .setNegativeButton("キャンセル", null)
                            .show();
                } else {
                    new AlertDialog.Builder(CreateData.this)
                            .setMessage("画像ファイルにアクセスするために権限を付与してください。この権限は画像ファイルの読み込み以外には使用しません。")
                            .setPositiveButton("確認", (dialogInterface, i) -> ActivityCompat.requestPermissions(CreateData.this,
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

    private void findViews() {
        title = findViewById(R.id.editTextTextPersonName);
        account = findViewById(R.id.editTextTextPersonName2);
        mail = findViewById(R.id.editTextTextPersonName3);
        password = findViewById(R.id.editTextTextPassword);
        url = findViewById(R.id.editTextTextPersonName4);
        memo = findViewById(R.id.editTextMemo);
        Wallpaper = findViewById(R.id.wallpaper);
        Accept = findViewById(R.id.Decision);
        ErrorIconMail = findViewById(R.id.errorIcon_mail);
        ErrorIconPass = findViewById(R.id.errorIcon_password);
        layoutTitle = findViewById(R.id.textInputLayoutPersonName);
        layoutAccount = findViewById(R.id.textInputLayoutPersonName2);
        layoutMail = findViewById(R.id.textInputLayoutPersonName3);
        layoutPassword = findViewById(R.id.textInputLayoutPassword);
        layoutUrl = findViewById(R.id.textInputLayoutPersonName4);
        layoutMemo = findViewById(R.id.textInputLayoutMemo);
        preview = findViewById(R.id.Preview);
        Layout = findViewById(R.id.Layout);
        VisibleIcon = findViewById(R.id.visibleIcon);
        preview_delete = findViewById(R.id.Preview_close);
    }

    private void init() {
        title.setText("");
        account.setText("");
        mail.setText("");
        password.setText("");
        url.setText("");
        memo.setText("");
        title.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();

        AccountLabel = mPref.getString("AccountLabel", "アカウント");
        MailLabel = mPref.getString("MailLabel", "メールアドレス");
        PassLabel = mPref.getString("PassLabel", "パスワード");
        UrlLabel = mPref.getString("UrlLabel", "URL");
        MemoLabel = mPref.getString("MemoLabel", "メモ");

        layoutAccount.setHint(AccountLabel);
        layoutMail.setHint(MailLabel);
        layoutPassword.setHint(PassLabel);
        layoutUrl.setHint(UrlLabel);
        layoutMemo.setHint(MemoLabel);

        layoutAccount.setVisibility(View.VISIBLE);
        layoutMail.setVisibility(View.VISIBLE);
        layoutPassword.setVisibility(View.VISIBLE);
        layoutUrl.setVisibility(View.VISIBLE);
        layoutMemo.setVisibility(View.VISIBLE);
        VisibleIcon.setVisibility(View.VISIBLE);

        AccountVisible = 1;
        MailVisible = 1;
        PassVisible = 1;
        UrlVisible = 1;
        MemoVisible = 1;

        if (mPref.getBoolean("AccountHide", false)) {
            layoutAccount.setVisibility(View.GONE);
            AccountVisible = 0;
        }
        if (mPref.getBoolean("MailHide", false)) {
            layoutMail.setVisibility(View.GONE);
            MailVisible = 0;
            layoutMail.setErrorEnabled(false);
            ErrorIconMail.setVisibility(View.GONE);
        }
        if (mPref.getBoolean("PassHide", false)) {
            layoutPassword.setVisibility(View.GONE);
            VisibleIcon.setVisibility(View.GONE);
            PassVisible = 0;
            layoutPassword.setErrorEnabled(false);
            ErrorIconPass.setVisibility(View.GONE);
        }
        if (mPref.getBoolean("UrlHide", false)) {
            layoutUrl.setVisibility(View.GONE);
            UrlVisible = 0;
        }
        if (mPref.getBoolean("MemoHide", false)) {
            layoutMemo.setVisibility(View.GONE);
            MemoVisible = 0;
        }

    }

    private boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
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
        strTitle = title.getText().toString();
        strAccount = account.getText().toString();
        strMail = mail.getText().toString();
        strPassword = password.getText().toString();
        strUrl = url.getText().toString();
        strMemo = memo.getText().toString();


        String imageFilePath = "";
        int CheckColor = 0;
        int LastID = 0;

        boolean Validation = mPref.getBoolean("Validation", false);


        if (Validation) {
            boolean error = false;
            Animation iconPop = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.error_pop);
            if (strPassword.equals("") && PassVisible == 1) {
                password.requestFocus();
                layoutPassword.setError("入力してください");
                ErrorIconPass.setVisibility(View.VISIBLE);
                ErrorIconPass.startAnimation(iconPop);
                error = true;
            }
            if (!isValidEmail(strMail) && MailVisible == 1) {
                mail.requestFocus();
                layoutMail.setError("不正なアドレス形式");
                ErrorIconMail.setVisibility(View.VISIBLE);
                ErrorIconMail.startAnimation(iconPop);
                error = true;
            }
            if (strMail.equals("") && MailVisible == 1) {
                mail.requestFocus();
                layoutMail.setError("入力してください");
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
        Intent intent = new Intent(getApplicationContext(), CropImage.class);
        if (resultCode == RESULT_OK && resultData != null)
            if (requestCode == RESULT_PICK_IMAGEFILE) { //結果コードの変動なし　かつ　リザルトOK時に実行
                Uri uri; //URI変数の定義
                uri = resultData.getData(); //URIに結果データを代入

                int Width = Layout.getWidth();

                intent.putExtra("Width", Width);
                intent.putExtra("URI", Objects.requireNonNull(uri).toString());
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
                    //ビットマップ画像(背景用)の定義
                    bitmap = BitmapFactory.decodeStream(bis);
                    preview.setImageBitmap(bitmap);
                    preview.setVisibility(View.VISIBLE);
                    preview_delete.setVisibility(View.VISIBLE);
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

    private void autoRename() {
        if (strTitle.equals("")) {
            ArrayList<String> list = new ArrayList<>();
            Cursor getDataCursor = dbAdapter.getData();
            if (getDataCursor.moveToFirst()) {
                do {
                    list.add(getDataCursor.getString(1));
                } while (getDataCursor.moveToNext());
            }
            getDataCursor.close();
            for (int i = 1; ; i++) {
                String cast = Integer.toString(i);
                String str = "タイトル " + cast;
                if (!list.contains(str)) {
                    strTitle = str;
                    list.clear();
                    break;
                }
            }
        }
        if (strAccount.equals("")) {
            ArrayList<String> list = new ArrayList<>();
            Cursor searchTitleCursor = dbAdapter.searchTitle(strTitle);
            if (searchTitleCursor.moveToFirst()) {
                do {
                    list.add(searchTitleCursor.getString(1));
                } while (searchTitleCursor.moveToNext());
            }
            searchTitleCursor.close();
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

    private void autoBackup() {
        Intent serviceIntent = new Intent();
        serviceIntent.putExtra("Job", "ADD_DATA");
        AsyncWork.enqueueWork(getApplicationContext(), serviceIntent);
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
        intent.putExtra("TITLE_NAME", strTitle);
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
    }


}