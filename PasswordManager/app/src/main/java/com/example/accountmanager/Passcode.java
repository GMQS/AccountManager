package com.example.accountmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.stephentuso.welcome.WelcomeHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executor;

public class Passcode extends AppCompatActivity implements TextWatcher {


    private SQL dbAdapter = new SQL(this);
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private EditText PassInput;
    private TextInputLayout PassLayout;
    private TextView Explanation;
    private Button Set;


    private boolean BioAuthlogin = false;
    private boolean first_step = false;
    private boolean Login = false;
    private boolean Change = false;
    private boolean Success = false;
    private boolean Success2 = false;

    private boolean BioAuthVersionErr = false;
    private boolean BioAuthHardwareErr = false;
    private boolean BioAuthSettingErr = false;
    private boolean BioAuthAccessErr = false;
    private boolean BioAuthSuccess = false;


    private String PIN = "";
    private String BitmapStrings = "";
    private int FilterValue = 127;

    private String input;
    private String input2;
    private ImageView Wallpaper;
    private int Choice;
    private int theme;
    //private Animation PopButton;

    private SharedPreferences mPref;
    private SharedPreferences.Editor editor;
    //private int SetFilter;
    private boolean PinChange;

    //private String Register = "";

    WelcomeHelper welcomeScreen;

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
        theme = mPref.getInt("accent", R.style.NoActionBar);
        editor.remove("BmpStrFlag");
        editor.remove("BmpStr");
        editor.remove("AfterCrop");
        editor.apply();

        Functions.brokenFilesDelete(getFilesDir().getPath(),dbAdapter);


        //壁紙用レコードの作成　インサート文
        if (mPref.getBoolean("FirstBoot", true)) {
            //引数1:ビットマップ文字列 引数2:フィルタ濃度数値 引数3:"ALL"=すべてをput,"WALLPAPER"=壁紙をput,"FILTER"=フィルタ濃度をput 引数4:インサートするか更新するかの真偽値
            dbAdapter.saveWallpaper("", 127, "ALL", true);
            dbAdapter.savePin("", true);
        }


        setContentView(R.layout.activity_passcode);

        Toolbar myToolbar = findViewById(R.id.my_toolbar4);
        setSupportActionBar(myToolbar);

        checkBiometricSupport();

        welcomeScreen = new WelcomeHelper(this, Tutorial.class);
        welcomeScreen.show(savedInstanceState);


        findViews();
        setTitle("PIN登録");

        Set.setEnabled(false);
        Set.setTextColor(Color.argb(180, 120, 120, 120));

        //SetFilter = mPref.getInt("Filter", 127);

        BioAuthlogin = mPref.getBoolean("BioAuth", false);

        if (BioAuthlogin) {
            editor.putInt("Choice", 1);
        } else {
            editor.putInt("Choice", 0);
        }
        editor.commit();

        PassInput.addTextChangedListener(this);

        try {
            Functions.setWallpaper(getFilesDir(), this, Wallpaper, FilterValue, dbAdapter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Cursor cursor = dbAdapter.getPin();
        if (cursor.moveToFirst()) {
            PIN = cursor.getString(0);
        }
        cursor.close();

        PinChange = getIntent().getBooleanExtra("ChangePIN", false);
        if (PinChange) {

            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            Login = true;
            Change = true;
            Explanation.setText("現在のPINを入力してください");
            Set.setText("変更");
            setTitle("登録変更");
            init();
            PassLayout.setErrorEnabled(false);
        }


        if (!PIN.equals("") && !PinChange) {
            Login = true;
            Set.setText("ログイン");
            setTitle("ログイン");
            Explanation.setText("PINを入力してください");
        }

        PassInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!(PassInput.getText().toString().length() < 4)) {
                        PINAuthentication();
                    } else {
                        PassLayout.setError("4桁以上入力してください");
                    }
                }
                return true;
            }
        });

        Set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set.startAnimation(PopButton);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                PINAuthentication();
            }
        });

        if (Login && BioAuthlogin && BioAuthSuccess && !PinChange) {
            Authentication();//セキュリティ認証
        }

        if (PIN.equals("") && mPref.getBoolean("FirstBoot", true)) {
            new AlertDialog.Builder(Passcode.this)
                    .setMessage("DropBoxクラウドからアカウント情報を復元しますか？")
                    .setPositiveButton("復元", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            editor.putBoolean("FirstBoot", false);
                            editor.apply();
                            Intent intent = new Intent(getApplication(), CloudSync.class);
                            intent.putExtra("Restore", true);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("新規作成", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            editor.putBoolean("FirstBoot", false);
                            editor.apply();
                        }
                    })
                    .show();
        }

    }

    @Override
    public void onBackPressed() {
        if (Change) {
            new AlertDialog.Builder(Passcode.this)
                    //.setTitle("警告")
                    .setMessage("PINの変更をキャンセルしますか？")
                    .setPositiveButton("中断", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Success = false;
                            Success2 = false;
                            Change = false;
                            init();
                            if (PinChange) {
                                PinChange = false;
                                finish();
                            } else {
                                Explanation.setText("PINを入力してください");
                                Set.setText("ログイン");
                                setTitle("ログイン");
                                PassLayout.setErrorEnabled(false);
                            }
                        }
                    })
                    .setNegativeButton("続行", null)
                    .show();
        } else {
            finish();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        String inputStr = s.toString();
        if (inputStr.length() > 3) {
            Set.setEnabled(true);
            switch (theme) {
                case R.style.NoActionBar:
                    Set.setTextColor(0xFFFF60A8);
                    break;

                case R.style.NoActionBarCyan:
                    Set.setTextColor(0xFF00B8D4);
                    break;

                case R.style.NoActionBarOrange:
                    Set.setTextColor(0xFFFF6D00);
                    break;

                case R.style.NoActionBarGreen:
                    Set.setTextColor(0xFF64DD17);
                    break;
            }
            PassLayout.setErrorEnabled(false);

        } else if (inputStr.length() < 4 && inputStr.length() > 0) {
            PassLayout.setError("4桁以上入力してください");
            Set.setEnabled(false);
            Set.setTextColor(Color.argb(180, 120, 120, 120));
        } else if (inputStr.length() == 0) {
            Set.setEnabled(false);
            Set.setTextColor(Color.argb(180, 120, 120, 120));
        }
    }

    //オプショナルボタンの実装
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.passcode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //項目の分岐処理
        switch (item.getItemId()) {

            case R.id.single:

                if (Login && !Change) {
                    Change = true;
                    Explanation.setText("現在のPINを入力してください");
                    Set.setText("変更");
                    setTitle("登録変更");
                    init();
                    PassLayout.setErrorEnabled(false);

                } else if (!Login) {
                    new AlertDialog.Builder(Passcode.this)
                            //.setTitle("警告")
                            .setMessage("最初にPINを登録してください。")
                            .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .show();
                } else if (Change) {
                    new AlertDialog.Builder(Passcode.this)
                            //.setTitle("警告")
                            .setMessage("PINの変更をキャンセルしますか？")
                            .setPositiveButton("中断", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Success = false;
                                    Success2 = false;
                                    Change = false;
                                    init();
                                    if (PinChange) {
                                        PinChange = false;
                                        finish();
                                    } else {
                                        Explanation.setText("PINを入力してください");
                                        Set.setText("ログイン");
                                        setTitle("ログイン");
                                        PassLayout.setErrorEnabled(false);
                                    }
                                }
                            })
                            .setNegativeButton("続行", null)
                            .show();
                }

                return true;

            case R.id.bio:
                if (Login) {
                    if (BioAuthSuccess && !Change) {

                        Authentication();//セキュリティ生体認証

                    }
                    if (BioAuthVersionErr) {
                        new AlertDialog.Builder(Passcode.this)
                                //.setTitle("警告")
                                .setMessage("端末のアンドロイドバージョンが生体認証をサポートしていません。")
                                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                })
                                .show();

                    } else if (BioAuthHardwareErr) {
                        new AlertDialog.Builder(Passcode.this)
                                //.setTitle("警告")
                                .setMessage("端末に生体認証機能が搭載されていないため利用できません。")
                                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                })
                                .show();
                    } else if (BioAuthSettingErr | BioAuthAccessErr) {
                        new AlertDialog.Builder(Passcode.this)
                                //.setTitle("警告")
                                .setMessage("生体認証が利用できません。端末のセキュリティ設定を確認してください。")
                                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                })
                                .show();

                    } else if (BioAuthVersionErr) {
                        new AlertDialog.Builder(Passcode.this)
                                //.setTitle("警告")
                                .setMessage("端末のアンドロイドバージョンが生体認証をサポートしていません。")
                                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                })
                                .show();

                    } else if (BioAuthHardwareErr) {
                        new AlertDialog.Builder(Passcode.this)
                                //.setTitle("警告")
                                .setMessage("端末に生体認証機能が搭載されていないため利用できません。")
                                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                })
                                .show();
                    } else if (BioAuthSettingErr | BioAuthAccessErr) {
                        new AlertDialog.Builder(Passcode.this)
                                //.setTitle("警告")
                                .setMessage("生体認証が利用できません。端末のセキュリティ設定を確認してください。")
                                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                })
                                .show();
                    }
                } else if (Change) {
                    //処理をしない
                } else {
                    new AlertDialog.Builder(Passcode.this)
                            //.setTitle("警告")
                            .setMessage("最初にPINを登録してください。")
                            .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .show();
                }
                return true;

            case R.id.login:


                int put = mPref.getInt("Choice", 0);
                final String[] Title = new String[]{
                        "PINでログイン",
                        "生体認証でログイン"};
                new AlertDialog.Builder(Passcode.this)
                        .setTitle("起動時ログイン方法選択")
                        .setSingleChoiceItems(Title, put, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int button) {
                                if (button == 0) {
                                    Choice = 0;
                                }
                                if (button == 1) {
                                    Choice = 1;
                                }
                            }
                        })
                        .setPositiveButton("決定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (Choice == 0) {
                                    //editor.putBoolean("BioAuth", false);
                                    editor.putBoolean("BioAuth", false);
                                    editor.putInt("Choice", 0);
                                    BioAuthlogin = mPref.getBoolean("BioAuth", false);
                                }
                                if (Choice == 1) {
                                    //editor.putBoolean("BioAuth", true);
                                    editor.putBoolean("BioAuth", true);
                                    editor.putInt("Choice", 1);
                                    BioAuthlogin = mPref.getBoolean("BioAuth", false);
                                }
                                editor.apply();
                                //editor2.commit();
                            }
                        })
                        .show();


                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        welcomeScreen.onSaveInstanceState(outState);
    }


    private void checkBiometricSupport() {

        KeyguardManager keyguardManager =
                (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

        PackageManager packageManager = this.getPackageManager();

        //指紋認証センサーが搭載されていない
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            BioAuthHardwareErr = true;
        }
        //アンドロイドのバージョンが指紋認証をサポートしていない
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            BioAuthVersionErr = true;
        }
        //セキュリティ設定をしていない
        if (!keyguardManager.isKeyguardSecure()) {
            BioAuthSettingErr = true;
        }
        //指紋認証ログインを許可していない
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.USE_BIOMETRIC) !=
                PackageManager.PERMISSION_GRANTED) {
            BioAuthAccessErr = true;
        } else {
            BioAuthSuccess = true;
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        new AlertDialog.Builder(Passcode.this)
                //.setTitle("警告")
                .setMessage("PINの変更をキャンセルしますか？")
                .setPositiveButton("中断", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Success = false;
                        Success2 = false;
                        Change = false;
                        PinChange = false;
                        PassLayout.setErrorEnabled(false);
                        init();
                        finish();
                    }
                })
                .setNegativeButton("続行", null)
                .show();
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void init() {
        PassInput.setText("");
    }

    private void Authentication() {
        //生体認証ここから
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);

            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Intent intent = new Intent(getApplication(), MainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "認証に失敗しました",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("認証してログイン")
                .setNegativeButtonText("PINを使用")
                .build();

        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.
        biometricPrompt.authenticate(promptInfo);
    }

    private void findViews() {
        PassInput = findViewById(R.id.PasswordInput);
        PassLayout = findViewById(R.id.textInputLayoutPasscode);
        Explanation = findViewById(R.id.textView2);
        Set = findViewById(R.id.button2);
        Wallpaper = findViewById(R.id.wallpaper);
    }

    private void PINAuthentication() {
        if (!first_step && !Login) {
            input = PassInput.getText().toString();
            first_step = true;
            init();
            Explanation.setText("確認のため再度入力してください");
        } else if (first_step && !Login) {
            if (PassInput.getText().toString().equals(input)) {

                dbAdapter.savePin(input, false);

                Toast.makeText(Passcode.this, "PINを登録しました", Toast.LENGTH_SHORT).show();

                View v = findViewById(R.id.Layout);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                if (BioAuthSuccess) {

                    new AlertDialog.Builder(Passcode.this)
                            //.setTitle("確認")
                            .setMessage("次回から端末の生体認証を使用してログインしますか？")
                            .setPositiveButton("使用する", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    editor.putBoolean("BioAuth", true);
                                    editor.putInt("Choice", 1);
                                    editor.apply();

                                    AskCloudSync();

                                }
                            })
                            .setNegativeButton("使用しない", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {


                                    AskCloudSync();

                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialogInterface) {
                                    AskCloudSync();
                                }
                            })
                            .show();
                } else {


                    //クラウド利用確認ダイアログ
                    AskCloudSync();

                }

            } else if (!PassInput.getText().toString().equals(input) && !Login) {
                init();
                PassLayout.setError("PINが違います");
            }
        } else if (Login && !Change) {
            if (PIN.equals(PassInput.getText().toString())) {
                if (mPref.getBoolean("RestoreOK", false)) {
                    View v = findViewById(R.id.Layout);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    editor.remove("RestoreOK");
                    editor.apply();
                    if (BioAuthSuccess) {

                        new AlertDialog.Builder(Passcode.this)
                                //.setTitle("確認")
                                .setMessage("次回から端末の生体認証を使用してログインしますか？")
                                .setPositiveButton("使用する", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        editor.putBoolean("BioAuth", true);
                                        editor.putInt("Choice", 1);
                                        editor.apply();
                                        //editor2.commit();


                                        //クラウド利用確認ダイアログ
                                        AskCloudSync();

                                    }
                                })
                                .setNegativeButton("使用しない", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {


                                        //クラウド利用確認ダイアログ
                                        AskCloudSync();

                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialogInterface) {
                                        AskCloudSync();
                                    }
                                })
                                .show();
                    } else {


                        //クラウド利用確認ダイアログ
                        AskCloudSync();

                    }
                } else {
                    Intent intent = new Intent(getApplication(), MainActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            } else {
                PassLayout.setError("PINが違います");
                init();
            }
        } else if (Change && !Success) {
            if (PIN.equals(PassInput.getText().toString())) {
                Success = true;
                init();
                Explanation.setText("新しいPINを入力してください");
            } else {
                init();
                PassLayout.setError("PINが違います");
            }
        } else if (Success && !Success2) {
            input2 = PassInput.getText().toString();
            Success2 = true;
            init();
            Explanation.setText("確認のため再度入力してください");
        } else if (Success2) {
            if (PassInput.getText().toString().equals(input2)) {
                dbAdapter.savePin(input2, false);
                Toast.makeText(Passcode.this, "PINを変更しました", Toast.LENGTH_SHORT).show();
                Success = false;
                Success2 = false;
                Change = false;
                init();
                if (PinChange) {
                    PinChange = false;
                    finish();
                } else {
                    Explanation.setText("PINを入力してください");

                    //dbAdapter.openData();
                    Cursor cursor = dbAdapter.getPin();
                    if (cursor.moveToFirst()) {
                        PIN = cursor.getString(0);
                    }
                    cursor.close();
                    Set.setText("ログイン");
                    setTitle("ログイン");
                }
            } else if (!PassInput.getText().toString().equals(input2)) {
                init();
                PassLayout.setError("PINが違います");
            }
        }
    }

    private void AskCloudSync() {
        new AlertDialog.Builder(Passcode.this)
                .setMessage("DropBoxクラウドサービスを使用してアカウント情報の自動バックアップを有効にしますか？")
                .setPositiveButton("使用する", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mPref.getBoolean("UnlockSetting", false)) {
                            editor.putBoolean("AutoSync", true);
                            editor.apply();
                            Intent intent = new Intent(getApplication(), MainActivity.class);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        } else {
                            Intent intent = new Intent(getApplication(), CloudSync.class);
                            intent.putExtra("STEP", true);
                            startActivity(intent);
                        }
                        finish();
                    }
                })
                .setNegativeButton("使用しない", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editor.putBoolean("AutoSync", false);
                        editor.apply();
                        Intent intent = new Intent(getApplication(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        editor.putBoolean("AutoSync", false);
                        editor.apply();
                        Intent intent = new Intent(getApplication(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }
}