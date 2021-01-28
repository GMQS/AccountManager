package com.example.accountmanager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class AccountInfo extends AppCompatActivity {

    private static final int RESULT_CHANGE = 4000;
    private static final int RESULT_PICK_COLOR = 3000;
    private static final int RESULT_PICK_IMAGEFILE = 1000;
    private final int REQUEST_CODE = 1; //ストレージアクセスパーミッション判定用
    private static final String SHOWCASE_ID = "edit";
    private SQL dbAdapter = new SQL(this);
    private EditText AccountInfo;
    private EditText MailInfo;
    private EditText PasswordInfo;
    private EditText UrlInfo;
    private EditText MemoInfo;
    private TextView AccountHeading;
    private TextView MailHeading;
    private TextView PassHeading;
    private TextView UrlHeading;
    private TextView MemoHeading;
    private TextView ToolbarText;
    private ImageView headerView;
    private ImageView headerMask;

    private ImageView CopyIcon1;
    private ImageView CopyIcon2;
    private ImageView CopyIcon3;
    private ImageView CopyIcon4;
    private ImageView CopyIcon5;
    private ImageView WebIcon;
    private ImageView VisibleIcon;

    private View MailDivider;
    private View PassDivider;
    private View UrlDivider;
    private View MemoDivider;
    private View BottomDivider;
    private int AccountVisible;
    private int MailVisible;
    private int PassVisible;
    private int UrlVisible;
    private int MemoVisible;
    //private Bitmap bmp; //ビットマップ画像(背景用)の定義
    private String sendTitle, sendAccount, sendMail, sendPass, sendUrl, sendMemo;
    private String outPut = "";
    private String ID;
    private String myID;
    private int lastCheck = 0;
    private int TemporaryColor;
    private String myPic;
    private int myColor;
    private String getTitle;
    private Animation FabPush;
    private boolean ShowPassword;
    private boolean EditMode = false;
    private CoordinatorLayout coordinatorLayout;
    private SharedPreferences mPref;
    private SharedPreferences.Editor editor;
    private FloatingActionButton fab;
    private String token;
    private String AccountStr;
    private String MailStr;
    private String PassStr;
    private String UrlStr;
    private String MemoStr;
    private ArrayList<Integer> selectedItems;
    private ArrayList<String> ListItems;
    private ArrayList<String> ChoiceValue;
    private boolean delete;
    private EditText CustomPassword_editText;
    private int RandomLength;
    private String charSet;
    private int getWidth;

    private File titleHeaderFile;
    private File headerFile;
    private File tmpHeaderFile;


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
            if (AccountInfo.getText().hashCode() == s.hashCode() && !AccountInfo.equals("")) {
                clearError(AccountInfo);

            } else if (MailInfo.getText().hashCode() == s.hashCode() && !MailInfo.equals("")) {
                clearError(MailInfo);

            } else if (PasswordInfo.getText().hashCode() == s.hashCode() && !PasswordInfo.equals("")) {
                clearError(PasswordInfo);

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
        editor.putBoolean("Start", true);
        editor.apply();
        changeTheme();
        setupView();
        getInfo();
        getDisplayWidth();

        titleHeaderFile = new File(getFilesDir().getPath() + "/" + sendTitle + "_header.jpg");
        headerFile = new File(getFilesDir().getPath() + "/" + myPic);
        tmpHeaderFile = new File(getFilesDir().getPath() + "/tmp_image.jpg");

        Bitmap bitmap;
        if (myPic.equals("")) {
            Resources r = getResources();
            bitmap = BitmapFactory.decodeResource(r, R.drawable.header_default);
            bitmap = Bitmap.createScaledBitmap(bitmap, getWidth, (int) (getWidth * 0.5625), false);
            headerView.setImageBitmap(bitmap);
            headerView.setColorFilter(myColor);
        } else {
            BufferedInputStream bis = null;
            try {
                bis = new BufferedInputStream(getApplicationContext().openFileInput(myPic));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bitmap = BitmapFactory.decodeStream(bis);
            headerView.setImageBitmap(bitmap);
            headerView.setColorFilter(Color.argb(0, 0, 0, 0));
        }


        changeTextColor();
        Animation fabPop = AnimationUtils.loadAnimation(AccountInfo.this, R.anim.fab_pop);
        FabPush = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_push_anim);
        ShowPassword = mPref.getBoolean("ShowPassword", false);

        passwordVisibility(ShowPassword);

        AccountInfo.addTextChangedListener(generalTextWatcher);
        MailInfo.addTextChangedListener(generalTextWatcher);
        PasswordInfo.addTextChangedListener(generalTextWatcher);


        fab.setOnClickListener(v -> {
            if (!EditMode) {
                changeMode("EDIT_MODE");
                AccountInfo.requestFocus();
                CharSequence StrLength = AccountInfo.getText();
                AccountInfo.setSelection(StrLength.length());

            } else {
                new AlertDialog.Builder(AccountInfo.this)
                        .setMessage("変更内容を保存しますか？")
                        .setPositiveButton("保存", (dialogInterface, i) -> {
                            changeMode("NORMAL_MODE");
                            saveInfo();
                            boolean AutoSync = mPref.getBoolean("AutoSync", false);
                            if (AutoSync) {
                                token = mPref.getString("access_token", null);
                                new DropboxManager(AccountInfo.this, token);
                                Intent serviceIntent = new Intent();
                                serviceIntent.putExtra("title", sendTitle);
                                serviceIntent.putExtra("id", myID);
                                serviceIntent.putExtra("Job", "CHANGE_DATA_INFO");
                                AsyncWork.enqueueWork(getApplicationContext(), serviceIntent);
                            }
                        })
                        .setNegativeButton("保存しない", (dialogInterface, i) -> {
                            changeMode("NORMAL_MODE");
                            saveCancel();
                        })
                        .show();

            }

        });

        headerView.setOnClickListener(view -> {
            if (EditMode) {
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add("フォルダから画像を選択");
                if (!myPic.equals(sendTitle + "_header.jpg") && titleHeaderFile.exists()) {
                    arrayList.add("タイトルヘッダーを適応");
                }
                if (myPic.equals("")) {
                    arrayList.add("ヘッダーカラーを変更");
                } else {
                    arrayList.add("ヘッダー画像を削除");
                }
                String[] CastingList = arrayList.toArray(new String[0]);

                new AlertDialog.Builder(AccountInfo.this)
                        .setItems(CastingList, (dialogInterface, i) -> {
                            if (i == 0) {
                                openImageFile();
                            } else if (i == 1) {
                                if (CastingList[1].equals("タイトルヘッダーを適応")) {
                                    setTitleHeader();
                                } else if (CastingList[1].equals("ヘッダーカラーを変更")) {
                                    pickColor();
                                } else if (CastingList[1].equals("ヘッダー画像を削除")) {
                                    deleteHeader();
                                }
                            }else if (i == 2){
                                if (CastingList[2].equals("ヘッダーカラーを変更")){
                                    pickColor();
                                }
                            }
                        })
                        .show();
            }
        });


        CopyIcon1.setOnClickListener(view -> {
            Functions.clipBoard(getApplicationContext(), AccountInfo.getText().toString());
            Toast.makeText(getApplicationContext(), "クリップボードに保存しました", Toast.LENGTH_SHORT).show();
        });

        CopyIcon2.setOnClickListener(view -> {
            Functions.clipBoard(getApplicationContext(), MailInfo.getText().toString());
            Toast.makeText(getApplicationContext(), "クリップボードに保存しました", Toast.LENGTH_SHORT).show();
        });

        CopyIcon3.setOnClickListener(view -> {
            Functions.clipBoard(getApplicationContext(), PasswordInfo.getText().toString());
            Toast.makeText(getApplicationContext(), "クリップボードに保存しました", Toast.LENGTH_SHORT).show();
        });

        CopyIcon4.setOnClickListener(view -> {
            Functions.clipBoard(getApplicationContext(), UrlInfo.getText().toString());
            Toast.makeText(getApplicationContext(), "クリップボードに保存しました", Toast.LENGTH_SHORT).show();
        });

        CopyIcon5.setOnClickListener(view -> {
            Functions.clipBoard(getApplicationContext(), MemoInfo.getText().toString());
            Toast.makeText(getApplicationContext(), "クリップボードに保存しました", Toast.LENGTH_SHORT).show();
        });

        WebIcon.setOnClickListener(view -> {
            String url = UrlInfo.getText().toString();
            if (!UrlInfo.getText().toString().startsWith("https://") && !UrlInfo.getText().toString().startsWith("http://")) {
                url = "http://" + url;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(intent, "ブラウザで開く"));
            } else {
                Toast.makeText(getApplicationContext(), "ブラウザアプリケーションが見つかりませんでした", Toast.LENGTH_SHORT).show();
            }
        });

        VisibleIcon.setOnClickListener(view -> {
            int cursorPos = PasswordInfo.getSelectionStart();
            PasswordInfo.requestFocus();
            CharSequence StrLength = PasswordInfo.getText();
            PasswordInfo.setSelection(StrLength.length());
            if (ShowPassword) {
                passwordVisibility(false);
                ShowPassword = false;
            } else {
                passwordVisibility(true);
                ShowPassword = true;
            }
            PasswordInfo.setSelection(cursorPos);
        });

        fab.startAnimation(fabPop);
    }

    @Override
    public void onBackPressed() {
        if (EditMode) {
            new AlertDialog.Builder(AccountInfo.this)
                    .setMessage("変更内容を保存しますか？")
                    .setPositiveButton("保存", (dialogInterface, i) -> {
                        changeMode("NORMAL_MODE");
                        saveInfo();
                        boolean AutoSync = mPref.getBoolean("AutoSync", false);
                        if (AutoSync) {
                            token = mPref.getString("access_token", null);
                            new DropboxManager(AccountInfo.this, token);
                            Intent serviceIntent = new Intent();
                            serviceIntent.putExtra("title", sendTitle);
                            serviceIntent.putExtra("id", myID);
                            serviceIntent.putExtra("Job", "CHANGE_DATA_INFO");
                            AsyncWork.enqueueWork(getApplicationContext(), serviceIntent);

                        }
                    })
                    .setNegativeButton("保存しない", (dialogInterface, i) -> {
                        changeMode("NORMAL_MODE");
                        saveCancel();
                    })
                    .show();
        } else {
            finish();
            overridePendingTransition(R.anim.flowup_in, R.anim.left_slide_out);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (EditMode) {
            new AlertDialog.Builder(AccountInfo.this)
                    .setMessage("変更内容を保存しますか？")
                    .setPositiveButton("保存", (dialogInterface, i) -> {

                        changeMode("NORMAL_MODE");
                        saveInfo();

                        boolean AutoSync = mPref.getBoolean("AutoSync", false);
                        if (AutoSync) {
                            token = mPref.getString("access_token", null);
                            new DropboxManager(AccountInfo.this, token);
                            Intent serviceIntent = new Intent();
                            serviceIntent.putExtra("title", sendTitle);
                            serviceIntent.putExtra("id", myID);
                            serviceIntent.putExtra("Job", "CHANGE_DATA_INFO");
                            AsyncWork.enqueueWork(getApplicationContext(), serviceIntent);
                        }
                    })
                    .setNegativeButton("保存しない", (dialogInterface, i) -> {
                        changeMode("NORMAL_MODE");
                        saveCancel();
                    })
                    .show();
        } else {
            finish();
            overridePendingTransition(R.anim.flowup_in, R.anim.left_slide_out);
        }
        return true;
    }

    //オプショナルボタンの実装
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.last, menu);

        new Handler().post(() -> {
            //view

            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(500); // half second between each showcase view

            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(AccountInfo.this, SHOWCASE_ID);
            sequence.setConfig(config);

            //tutorial
            sequence.addSequenceItem(
                    new MaterialShowcaseView.Builder(AccountInfo.this)
                            .setTarget(fab)
                            .setContentText("このボタンで登録情報の編集モードに切り替えられます。")
                            .setDismissText("確認")
                            .build()
            );
            sequence.start();
        });
        return true;
    }

    //オプショナルボタンの動作
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //項目の分岐処理
        switch (item.getItemId()) {

            case R.id.single2:
                new AlertDialog.Builder(AccountInfo.this)
                        //.setTitle("削除")
                        .setMessage("アカウント情報を1件削除しますか？")
                        .setPositiveButton("削除", (dialogInterface, i) -> {
                            delete = true;
                            lastCheck = 0;
                            Intent intent = new Intent();
                            int pos = getIntent().getIntExtra("POSITION", 0);
                            String Account = getIntent().getStringExtra("ACCOUNT");
                            String Mail = getIntent().getStringExtra("MAIL");

                            intent.putExtra("ID", myID);
                            intent.putExtra("ACCOUNT", Account);
                            intent.putExtra("MAIL", Mail);
                            intent.putExtra("POSITION", pos);
                            intent.putExtra("LAST_CHECK", lastCheck);
                            intent.putExtra("DELETE", delete);
                            intent.putExtra("TITLE", getTitle);
                            setResult(RESULT_OK, intent);
                            finish();
                            overridePendingTransition(R.anim.flowup_in, R.anim.left_slide_out);
                        })
                        .setNegativeButton("キャンセル", (dialogInterface, i) -> {
                        })
                        .show();
                return true;

            case R.id.single3:
                if (EditMode) {
                    passwordDialog();
                } else {
                    new AlertDialog.Builder(AccountInfo.this)
                            .setMessage("編集モードではないためパスワードを変更できません。編集モードに切り替えてパスワードを変更しますか？")
                            .setPositiveButton("編集モード", (dialogInterface, i) -> {
                                changeMode("EDIT_MODE");
                                passwordDialog();
                            })
                            .setNegativeButton("キャンセル", (dialogInterface, i) -> {
                            })
                            .show();

                }
                return true;

            case R.id.share:

                if (EditMode) {
                    new AlertDialog.Builder(AccountInfo.this)
                            .setMessage("変更内容を保存しますか？")
                            .setPositiveButton("保存", (dialogInterface, i) -> {
                                changeMode("NORMAL_MODE");
                                saveInfo();
                                boolean AutoSync = mPref.getBoolean("AutoSync", false);
                                if (AutoSync) {
                                    token = mPref.getString("access_token", null);
                                    new DropboxManager(AccountInfo.this, token);
                                    Intent serviceIntent = new Intent();
                                    serviceIntent.putExtra("title", sendTitle);
                                    serviceIntent.putExtra("id", myID);
                                    serviceIntent.putExtra("Job", "CHANGE_DATA_INFO");
                                    AsyncWork.enqueueWork(getApplicationContext(), serviceIntent);
                                }
                                if (!mPref.getBoolean("ShareWarning", false)) {
                                    warningDialog();
                                } else {
                                    shareDialog();
                                }

                            })
                            .setNegativeButton("保存しない", (dialogInterface, i) -> {
                                changeMode("NORMAL_MODE");
                                saveCancel();
                                if (!mPref.getBoolean("ShareWarning", false)) {
                                    warningDialog();
                                } else {
                                    shareDialog();
                                }
                            })
                            .show();
                } else {
                    if (!mPref.getBoolean("ShareWarning", false)) {
                        warningDialog();
                    } else {
                        shareDialog();
                    }
                }


                return true;

            case R.id.HeadingEdit:
                Intent intent_ = new Intent(this, HeadingEditActivity.class);
                intent_.putExtra("SendLabel", myID);
                startActivity(intent_);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void textJoint() {
        getInfo();
        boolean TitleSend = false;
        boolean AccountSend = false;
        boolean MailSend = false;
        boolean PassSend = false;
        boolean UrlSend = false;
        boolean MemoSend = false;

        if (selectedItems.contains(0)) {
            TitleSend = true;
        }

        for (int i = 0; i < ChoiceValue.size(); i++) {
            if (ChoiceValue.get(i).equals(AccountStr)) {
                AccountSend = true;

            } else if (ChoiceValue.get(i).equals(MailStr)) {
                MailSend = true;

            } else if (ChoiceValue.get(i).equals(PassStr)) {
                PassSend = true;

            } else if (ChoiceValue.get(i).equals(UrlStr)) {
                UrlSend = true;

            } else if (ChoiceValue.get(i).equals(MemoStr)) {
                MemoSend = true;

            }

        }

        String TitleLine;
        String AccountLine;
        String MailLine;
        String PassLine;
        String UrlLine;
        String MemoLine;

        if (mPref.getBoolean("ShareOption", true)) {
            TitleLine = "【タイトル】\n" + sendTitle;
            AccountLine = "【" + AccountHeading.getText().toString() + "】" + "\n" + sendAccount;
            MailLine = "【" + MailHeading.getText().toString() + "】" + "\n" + sendMail;
            PassLine = "【" + PassHeading.getText().toString() + "】" + "\n" + sendPass;
            UrlLine = "【" + UrlHeading.getText().toString() + "】" + "\n" + sendUrl;
            MemoLine = "【" + MemoHeading.getText().toString() + "】" + "\n" + sendMemo;
        } else {
            TitleLine = sendTitle;
            AccountLine = sendAccount;
            MailLine = sendMail;
            PassLine = sendPass;
            UrlLine = sendUrl;
            MemoLine = sendMemo;
        }


        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(TitleLine);
        arrayList.add(AccountLine);
        arrayList.add(MailLine);
        arrayList.add(PassLine);
        arrayList.add(UrlLine);
        arrayList.add(MemoLine);


        if (!TitleSend) {
            arrayList.remove(TitleLine);
        }

        if (AccountVisible == 0 || !AccountSend) {
            arrayList.remove(AccountLine);
        }

        if (MailVisible == 0 || !MailSend) {
            arrayList.remove(MailLine);
        }
        if (PassVisible == 0 || !PassSend) {
            arrayList.remove(PassLine);
        }
        if (UrlVisible == 0 || !UrlSend) {
            arrayList.remove(UrlLine);
        }
        if (MemoVisible == 0 || !MemoSend) {
            arrayList.remove(MemoLine);
        }


        StringBuilder sb = new StringBuilder();
        for (Iterator<String> iterator = arrayList.iterator(); iterator.hasNext(); ) {
            String tmpStr = iterator.next();
            sb.append(tmpStr);
            if (iterator.hasNext()) {
                sb.append("\n");
            }
        }
        outPut = new String(sb);

        arrayList.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getLabelInfo();
        viewVisible();

        if (AccountVisible == 0) {
            AccountInfo.setVisibility(View.GONE);
            AccountHeading.setVisibility(View.GONE);
            MailDivider.setVisibility(View.GONE);
            CopyIcon1.setVisibility(View.GONE);
        }
        if (MailVisible == 0) {
            MailInfo.setVisibility(View.GONE);
            MailHeading.setVisibility(View.GONE);
            PassDivider.setVisibility(View.GONE);
            CopyIcon2.setVisibility(View.GONE);
        }
        if (PassVisible == 0) {
            PasswordInfo.setVisibility(View.GONE);
            PassHeading.setVisibility(View.GONE);
            UrlDivider.setVisibility(View.GONE);
            CopyIcon3.setVisibility(View.GONE);
            VisibleIcon.setVisibility(View.GONE);
        }
        if (UrlVisible == 0) {
            UrlInfo.setVisibility(View.GONE);
            UrlHeading.setVisibility(View.GONE);
            MemoDivider.setVisibility(View.GONE);
            CopyIcon4.setVisibility(View.GONE);
            WebIcon.setVisibility(View.GONE);
        }
        if (MemoVisible == 0) {
            MemoInfo.setVisibility(View.GONE);
            MemoHeading.setVisibility(View.GONE);
            BottomDivider.setVisibility(View.GONE);
            CopyIcon5.setVisibility(View.GONE);
        }


    }

    private void saveInfo() {
        String changeAc = AccountInfo.getText().toString();
        String changeMa = MailInfo.getText().toString();
        String changePa = PasswordInfo.getText().toString();
        String changeUr = UrlInfo.getText().toString();
        String changeMe = MemoInfo.getText().toString();

        File file = new File(getFilesDir().getPath() + "/header" + myID + ".jpg");
        if (tmpHeaderFile.exists()) {
            tmpHeaderFile.renameTo(file);
        }


        if (TemporaryColor == 0) {
            dbAdapter.changeData(sendTitle, changeAc, changeMa, changePa, changeUr, changeMe, myColor, myPic, ID);
        } else {
            dbAdapter.changeData(sendTitle, changeAc, changeMa, changePa, changeUr, changeMe, TemporaryColor, myPic, ID);
            myColor = TemporaryColor;
        }
    }

    private void saveCancel() {
        getInfo();
        if (tmpHeaderFile.exists()) {
            tmpHeaderFile.delete();
        }
        Bitmap bitmap;
        if (myPic.equals("")) {
            Resources r = getResources();
            bitmap = BitmapFactory.decodeResource(r, R.drawable.header_default);
            bitmap = Bitmap.createScaledBitmap(bitmap, getWidth, (int) (getWidth * 0.5625), false);
            headerView.setImageBitmap(bitmap);
            headerView.setColorFilter(myColor);
        } else {
            BufferedInputStream bis = null;
            try {
                bis = new BufferedInputStream(getApplicationContext().openFileInput(myPic));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bitmap = BitmapFactory.decodeStream(bis);
            headerView.setImageBitmap(bitmap);
            headerView.setColorFilter(Color.argb(0, 0, 0, 0));
        }
        TemporaryColor = 0;

    }


    private void getLabelInfo() {
        Cursor c = dbAdapter.ShowLabelInfo(myID);
        if (c.moveToFirst()) {

            do {
                AccountHeading.setText(c.getString(1));
                MailHeading.setText(c.getString(2));
                PassHeading.setText(c.getString(3));
                UrlHeading.setText(c.getString(4));
                MemoHeading.setText(c.getString(5));
                AccountVisible = c.getInt(6);
                MailVisible = c.getInt(7);
                PassVisible = c.getInt(8);
                UrlVisible = c.getInt(9);
                MemoVisible = c.getInt(10);

            } while (c.moveToNext());
        }
        c.close();
    }

    public void getInfo() {
        ID = getIntent().getStringExtra("id");
        findViews();
        Cursor c = dbAdapter.ShowInfo(ID);
        if (c.moveToFirst()) {

            do {
                //表示用
                ToolbarText.setText(c.getString(1));
                AccountInfo.setText(c.getString(2));
                MailInfo.setText(c.getString(3));
                PasswordInfo.setText(c.getString(4));
                UrlInfo.setText(c.getString(5));
                MemoInfo.setText(c.getString(6));


                //シェア用
                sendTitle = c.getString(1);
                sendAccount = c.getString(2);
                sendMail = c.getString(3);
                sendPass = c.getString(4);
                sendUrl = c.getString(5);
                sendMemo = c.getString(6);

                myID = c.getString(0);
                getTitle = c.getString(1);


                myPic = c.getString(7);//ヘッダー画像の文字列
                myColor = c.getInt(8);

                Log.d("画像ファイルパス", c.getString(7));


            } while (c.moveToNext());
        }
        c.close();
    }

    private String getHeaderStr() {
        String getFilename = "";
        Cursor c = dbAdapter.ShowInfo(myID);
        if (c.moveToFirst()) {
            do {
                getFilename = c.getString(7);//ヘッダー画像の文字列
            } while (c.moveToNext());
        }
        c.close();

        return getFilename;
    }

    private void getColor() {
        Cursor c = dbAdapter.ShowInfo(myID);
        if (c.moveToFirst()) {
            do {
                myColor = c.getInt(8);//ヘッダーカラーの文字列
            } while (c.moveToNext());
        }
        c.close();
    }

    public void findViews() {
        ToolbarText = findViewById(R.id.Toolbar_text);
        AccountInfo = findViewById(R.id.editAccount);
        MailInfo = findViewById(R.id.editMail);
        PasswordInfo = findViewById(R.id.editPass);
        UrlInfo = findViewById(R.id.editUrl);
        MemoInfo = findViewById(R.id.editMemo);
        headerView = findViewById(R.id.header);
        headerMask = findViewById(R.id.header_focus);
        AccountHeading = findViewById(R.id.AccountView);
        MailHeading = findViewById(R.id.MailView);
        PassHeading = findViewById(R.id.PassView);
        UrlHeading = findViewById(R.id.UrlView);
        MemoHeading = findViewById(R.id.MemoView);
        coordinatorLayout = findViewById(R.id.coordinatorlayout);

        MailDivider = findViewById(R.id.divider_mail);
        PassDivider = findViewById(R.id.divider_Pass);
        UrlDivider = findViewById(R.id.divider_Url);
        MemoDivider = findViewById(R.id.divider_Memo);
        BottomDivider = findViewById(R.id.divider_Last);

        CopyIcon1 = findViewById(R.id.copyIcon1);
        CopyIcon2 = findViewById(R.id.copyIcon2);
        CopyIcon3 = findViewById(R.id.copyIcon3);
        CopyIcon4 = findViewById(R.id.copyIcon4);
        CopyIcon5 = findViewById(R.id.copyIcon5);

        WebIcon = findViewById(R.id.webIcon);
        VisibleIcon = findViewById(R.id.visibleIcon);


        fab = findViewById(R.id.edit_fab);
    }

    public static void clearError(EditText editText) {
        editText.setError(null);
    }

    private void changeMode(String mode) {
        if (mode.equals("EDIT_MODE")) {
            Animation iconPop = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.error_pop);
            headerMask.setVisibility(View.VISIBLE);
            headerMask.startAnimation(iconPop);
            fab.startAnimation(FabPush);
            ToolbarText.setText("編集モード");
            Snackbar.make(coordinatorLayout, "編集モード", BaseTransientBottomBar.LENGTH_SHORT).show();
            fab.setImageResource(R.drawable.ic_baseline_check_24);

            AccountInfo.setEnabled(true);
            MailInfo.setEnabled(true);
            PasswordInfo.setEnabled(true);
            UrlInfo.setEnabled(true);
            MemoInfo.setEnabled(true);

            EditMode = true;
        } else {
            Animation iconGone = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_pop);
            headerMask.startAnimation(iconGone);
            headerMask.setVisibility(View.GONE);
            ToolbarText.setText(sendTitle);
            fab.startAnimation(FabPush);
            fab.setImageResource(R.drawable.ic_baseline_create_24);

            AccountInfo.setEnabled(false);
            MailInfo.setEnabled(false);
            PasswordInfo.setEnabled(false);
            UrlInfo.setEnabled(false);
            MemoInfo.setEnabled(false);

            if (ShowPassword && !mPref.getBoolean("ShowPassword", false)) {
                passwordVisibility(false);
                ShowPassword = false;
            }
            EditMode = false;
        }
    }

    private void viewVisible() {
        AccountInfo.setVisibility(View.VISIBLE);
        AccountHeading.setVisibility(View.VISIBLE);
        MailInfo.setVisibility(View.VISIBLE);
        MailHeading.setVisibility(View.VISIBLE);
        MailDivider.setVisibility(View.VISIBLE);
        PasswordInfo.setVisibility(View.VISIBLE);
        PassHeading.setVisibility(View.VISIBLE);
        PassDivider.setVisibility(View.VISIBLE);
        UrlInfo.setVisibility(View.VISIBLE);
        UrlHeading.setVisibility(View.VISIBLE);
        UrlDivider.setVisibility(View.VISIBLE);
        MemoInfo.setVisibility(View.VISIBLE);
        MemoHeading.setVisibility(View.VISIBLE);
        MemoDivider.setVisibility(View.VISIBLE);
        BottomDivider.setVisibility(View.VISIBLE);
        CopyIcon1.setVisibility(View.VISIBLE);
        CopyIcon2.setVisibility(View.VISIBLE);
        CopyIcon3.setVisibility(View.VISIBLE);
        CopyIcon4.setVisibility(View.VISIBLE);
        CopyIcon5.setVisibility(View.VISIBLE);
        WebIcon.setVisibility(View.VISIBLE);
        VisibleIcon.setVisibility(View.VISIBLE);
    }

    private void passwordVisibility(boolean visible) {
        if (visible) {
            PasswordInfo.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            VisibleIcon.setImageResource(R.drawable.ic_baseline_visibility_off_24);
        } else {
            PasswordInfo.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            VisibleIcon.setImageResource(R.drawable.ic_baseline_visibility_24);
        }
    }

    private void shareDialog() {
        AccountStr = AccountHeading.getText().toString();
        MailStr = MailHeading.getText().toString();
        PassStr = PassHeading.getText().toString();
        UrlStr = UrlHeading.getText().toString();
        MemoStr = MemoHeading.getText().toString();

        selectedItems = new ArrayList<>();//選択アイテム追跡用リスト Integer
        ListItems = new ArrayList<>();//表示文字列用リスト String
        ChoiceValue = new ArrayList<>();//選択要素格納用リスト String

        ListItems.add("タイトル");
        ListItems.add(AccountStr);
        ListItems.add(MailStr);
        ListItems.add(PassStr);
        ListItems.add(UrlStr);
        ListItems.add(MemoStr);


        if (AccountVisible == 0) {
            ListItems.remove(AccountStr);
        }
        if (MailVisible == 0) {
            ListItems.remove(MailStr);
        }
        if (PassVisible == 0) {
            ListItems.remove(PassStr);
        }
        if (UrlVisible == 0) {
            ListItems.remove(UrlStr);
        }
        if (MemoVisible == 0) {
            ListItems.remove(MemoStr);
        }

        String[] items = ListItems.toArray(new String[0]);
        boolean[] DefaultValue = {true, true, true, true, true, true};
        for (int j = 0; j < ListItems.size(); j++) {
            selectedItems.add(j);
        }
        new AlertDialog.Builder(AccountInfo.this)
                .setTitle("共有する項目を選択")
                .setMultiChoiceItems(items, DefaultValue, (dialogInterface, which, check) -> {

                    if (check) {
                        selectedItems.add(which);
                    } else if (selectedItems.contains(which)) {
                        selectedItems.remove(Integer.valueOf(which));
                    }

                })
                .setPositiveButton("決定", (dialogInterface, select) -> {


                    for (int i = 0; i < selectedItems.size(); i++) {
                        String addItem = ListItems.get(selectedItems.get(i));
                        ChoiceValue.add(addItem);
                    }

                    if (selectedItems.get(0).equals(0)) {
                        ChoiceValue.remove(0);
                    }


                    textJoint();
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, outPut);
                    if (shareIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(Intent.createChooser(shareIntent, "title"));
                    } else {
                        Toast.makeText(getApplicationContext(), "共有可能アプリケーションが見つかりませんでした", Toast.LENGTH_SHORT).show();
                    }
                    selectedItems.clear();
                    ChoiceValue.clear();
                    outPut = "";
                })
                .setNegativeButton("キャンセル", (dialogInterface, i) -> {
                    selectedItems.clear();
                    ChoiceValue.clear();
                    outPut = "";
                })
                .setOnCancelListener(dialogInterface -> {
                    selectedItems.clear();
                    ChoiceValue.clear();
                    outPut = "";
                })
                .show();
    }

    //引数1:リクエスト時コード,引数2:結果コード,引数3:結果データ
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        Intent intent = new Intent(getApplication(), CropImage.class);
        if (resultCode == RESULT_OK && resultData != null) {
            if (requestCode == RESULT_PICK_IMAGEFILE) { //結果コードの変動なし　かつ　リザルトOK時に実行
                Uri uri; //URI変数の定義
                //結果データが空でないとき
                uri = resultData.getData(); //URIに結果データを代入

                int Width = coordinatorLayout.getWidth();
                Log.d("Viewのサイズ幅", String.valueOf(Width));

                //Bitmap bitmap = decodeSampledBitmapFromFileDescriptor(uri, Width, Height);
                //Bitmap bitmap = Functions.decodeSampledBitmapFromFileDescriptor(uri, Width, Height, getContentResolver());


                //ByteArrayOutputStream baos = new ByteArrayOutputStream(); //ビットマップ画像をプリファレンスで保存するための準備 ファイル出力インスタンスの生成
                //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); //JPEG形式に圧縮
                //bitmapStr = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT); //BAOSの文字列データを使用してビットマップ画像を文字列型変数に代入

                intent.putExtra("PutAccount", myID);
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
                    Bitmap bmp;
                    bmp = BitmapFactory.decodeStream(bis);
                    headerView.setImageBitmap(bmp);
                    headerView.setColorFilter(Color.argb(0, 0, 0, 0));
                    myPic = "header" + myID + ".jpg";
                }
            } else if (requestCode == RESULT_PICK_COLOR) {
                Resources r = getResources();
                Bitmap defaultBmp = BitmapFactory.decodeResource(r, R.drawable.header_default);
                defaultBmp = Bitmap.createScaledBitmap(defaultBmp, getWidth, (int) (getWidth * 0.5625), false);
                headerView.setImageBitmap(defaultBmp);
                TemporaryColor = resultData.getIntExtra("Temporary_Color", 0);
                headerView.setColorFilter(TemporaryColor);
            }
        }
    }

    private void passwordDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View passwordDialogView = inflater.inflate(R.layout.password_castom, null);
        SeekBar seekBar = passwordDialogView.findViewById(R.id.password_seekbar);
        CustomPassword_editText = passwordDialogView.findViewById(R.id.password_edittext);
        ImageView imageView = passwordDialogView.findViewById(R.id.password_reflesh);
        final TextView textView = passwordDialogView.findViewById(R.id.password_seekbar_value);
        Spinner spinner = passwordDialogView.findViewById(R.id.password_spinner);
        RandomLength = mPref.getInt("PasswordLength", 15);
        //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.password_array, android.R.layout.simple_spinner_item);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_custom, getResources().getStringArray(R.array.password_array));
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

        new AlertDialog.Builder(AccountInfo.this)
                .setTitle("パスワード生成")
                .setView(passwordDialogView)
                .setPositiveButton("変更", (dialog, which) -> {
                    PasswordInfo.setText("");
                    PasswordInfo.setText(CustomPassword_editText.getText().toString());
                    PasswordInfo.requestFocus();
                    passwordVisibility(true);
                    ShowPassword = true;
                    CharSequence StrLength = PasswordInfo.getText();
                    PasswordInfo.setSelection(StrLength.length());
                }).setNegativeButton("キャンセル", null)
                .show();

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
                        .setPositiveButton("開く", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String uriString = "package:" + getPackageName();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(uriString));
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("キャンセル", null)
                        .show();


            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void changeTextColor() {
        int theme = mPref.getInt("accent", R.style.NoActionBar);
        switch (theme) {
            case R.style.NoActionBar:
                setTheme(R.style.NoActionBar2);
                AccountHeading.setTextColor(0xFFFF60A8);
                MailHeading.setTextColor(0xFFFF60A8);
                PassHeading.setTextColor(0xFFFF60A8);
                UrlHeading.setTextColor(0xFFFF60A8);
                MemoHeading.setTextColor(0xFFFF60A8);
                break;

            case R.style.NoActionBarCyan:
                setTheme(R.style.NoActionBarCyan2);
                AccountHeading.setTextColor(0xFF00B8D4);
                MailHeading.setTextColor(0xFF00B8D4);
                PassHeading.setTextColor(0xFF00B8D4);
                UrlHeading.setTextColor(0xFF00B8D4);
                MemoHeading.setTextColor(0xFF00B8D4);
                break;

            case R.style.NoActionBarOrange:
                setTheme(R.style.NoActionBarOrange2);
                AccountHeading.setTextColor(0xFFFF6D00);
                MailHeading.setTextColor(0xFFFF6D00);
                PassHeading.setTextColor(0xFFFF6D00);
                UrlHeading.setTextColor(0xFFFF6D00);
                MemoHeading.setTextColor(0xFFFF6D00);
                break;

            case R.style.NoActionBarGreen:
                setTheme(R.style.NoActionBarGreen2);
                AccountHeading.setTextColor(0xFF64DD17);
                MailHeading.setTextColor(0xFF64DD17);
                PassHeading.setTextColor(0xFF64DD17);
                UrlHeading.setTextColor(0xFF64DD17);
                MemoHeading.setTextColor(0xFF64DD17);
                break;
        }
    }

    private void changeTheme() {
        int theme = mPref.getInt("accent", R.style.NoActionBar);
        switch (theme) {
            case R.style.NoActionBar:
                setTheme(R.style.NoActionBar2);
                break;

            case R.style.NoActionBarCyan:
                setTheme(R.style.NoActionBarCyan2);
                break;

            case R.style.NoActionBarOrange:
                setTheme(R.style.NoActionBarOrange2);
                break;

            case R.style.NoActionBarGreen:
                setTheme(R.style.NoActionBarGreen2);
                break;
        }
    }

    private void setupView() {
        setContentView(R.layout.activity_account_info);
        Toolbar myToolbar = findViewById(R.id.my_toolbar3);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        findViews();
        setTitle("");
    }

    private void getDisplayWidth() {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);
        getWidth = size.x;
        Log.d("取得した横幅", String.valueOf(getWidth));
    }

    private void openImageFile() {
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
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(AccountInfo.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(AccountInfo.this)
                    .setMessage("権限がないためストレージの画像ファイルにアクセスできません。権限を許可するためにアプリの設定を開きますか？")
                    .setPositiveButton("開く", (dialogInterface14, i14) -> {
                        String uriString = "package:" + getPackageName();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(uriString));
                        startActivity(intent);
                    })
                    .setNegativeButton("キャンセル", null)
                    .show();
        } else {
            new AlertDialog.Builder(AccountInfo.this)
                    .setMessage("画像ファイルにアクセスするために権限を付与してください。この権限は画像ファイルの読み込み以外には使用しません。")
                    .setPositiveButton("確認", (dialogInterface1, i1) -> ActivityCompat.requestPermissions(AccountInfo.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE))
                    .show();
        }
    }

    private void pickColor() {
        Intent intent = new Intent(getApplicationContext(), ColorPalett.class);
        startActivityForResult(intent, RESULT_PICK_COLOR);
        overridePendingTransition(R.anim.under_silde_in, R.anim.dropdown_exit);
    }

    private void setTitleHeader() {
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(getApplicationContext().openFileInput(sendTitle + "_header.jpg"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bmp;
        bmp = BitmapFactory.decodeStream(bis);
        headerView.setImageBitmap(bmp);
        headerView.setColorFilter(Color.argb(0, 0, 0, 0));
        myPic = sendTitle + "_header.jpg";
    }

    private void deleteHeader() {
        Resources r = getResources();
        Bitmap defaultBmp = BitmapFactory.decodeResource(r, R.drawable.header_default);
        defaultBmp = Bitmap.createScaledBitmap(defaultBmp, getWidth, (int) (getWidth * 0.5625), false);
        headerView.setImageBitmap(defaultBmp);
        if (TemporaryColor == 0) {
            headerView.setColorFilter(myColor);
        } else {
            headerView.setColorFilter(TemporaryColor);
        }
        myPic = "";
    }

    private void warningDialog() {
        new AlertDialog.Builder(AccountInfo.this)
                .setMessage("パスワード等のセキュリティ情報を共有する場合は共有先に注意してください。")
                .setPositiveButton("確認", (dialogInterface14, i12) -> shareDialog())
                .setNegativeButton("次回から表示しない", (dialogInterface12, i1) -> {
                    shareDialog();
                    editor.putBoolean("ShareWarning", true);
                    editor.apply();
                })
                .setOnCancelListener(dialogInterface13 -> {
                })
                .show();
    }

}