package com.example.accountmanager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import Async.AsyncWork;
import DataBase.GetFilter;
import DataBase.SQL;
import Design.ISetImage;
import UI.RecyclerViewAdapter;
import Value.Filter;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_CODE = 1; //ストレージアクセスパーミッション判定用
    private static final int RESULT_PICK_WALLPAPER = 1000; //画像変更時のIDの監視用 壁紙
    private static final int RESULT_PICK_TITLE_HEADER = 2000; //画像変更時のIDの監視用 タイトルヘッダー
    private static final int RESULT_CHANGE = 3000; //変更通知用
    private static final int RESULT_ADD = 4000; //アカウント追加
    private static final int RESULT_OPTION = 5000;
    private static final String SHOWCASE_ID = "Add";

    private ImageView Wallpaper; //背景用イメージビューの定義
    private SQL dbAdapter = new SQL(this);
    private String Title;
    private CoordinatorLayout layout;
    private FloatingActionButton fab;
    //private int FilterValue;
    //private int Prog;
    //private int invProg;
    //private SeekBar seekBar;
    //private TextView seekBarValue;
    //private boolean ChangeOverlay;
    private int Choice;
    private int theme;
    private SharedPreferences mPref;
    private SharedPreferences.Editor editor;
    private Animation animation;
    private RecyclerView recyclerView;
    private ArrayList<String> RecycleItems;
    private RecyclerViewAdapter recyclerViewAdapter;
    private MenuItem searchItem;
    private androidx.appcompat.widget.SearchView searchView;
    private String searchWord = "";
    private boolean delete;
    private Snackbar snackbar;

    private Toolbar toolbar;


    private Context myContext;

    @Override
    public void onBackPressed() {

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (snackbar != null) {
            snackbar.dismiss();
        }
        deleteData();
        dbAdapter.closeData();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        searchView.onActionViewCollapsed();
        searchItem.collapseActionView();
        searchWord = "";
        if (snackbar != null) {
            snackbar.dismiss();
        }
        deleteData();
    }

    //アクティビティ起動時に一度呼び出される
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbAdapter.openData();
        myContext = getApplicationContext();


        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = mPref.edit();


        theme = mPref.getInt("accent", R.style.NoActionBar);
        setTheme(theme);


        setContentView(R.layout.activity_main); //XMLレイアウトをセット
        findViews();


        setSupportActionBar(toolbar);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL); //縦方向に設定
        recyclerView.setLayoutManager(layoutManager);
        RecycleItems = new ArrayList<>();

        Animation fabPop = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fab_pop);
        fab.startAnimation(fabPop);

        ISetImage.setWallpaper(myContext, Wallpaper).set();

        recyclerViewAdapter = new RecyclerViewAdapter(RecycleItems) {
            // onItemClick()をオーバーライドして
            // クリックイベントの処理を記述する

            @Override
            public void onItemClick(View view, int position, String itemData) {
                Title = itemData;
                recyclerView.setEnabled(false);
                Intent intent = new Intent(getApplication(), MultiAccountListActivity.class);
                intent.putExtra("nextActivity", true);
                intent.putExtra("TITLE_STRING", Title);
                startActivity(intent);
                overridePendingTransition(R.anim.left_slide_in, R.anim.dropdown_exit);
            }

            @Override
            public void onItemLongClick(View view, final int position, final String itemData) {

                Title = itemData;
                LayoutInflater inflater = getLayoutInflater();
                final View editTextView = inflater.inflate(R.layout.edittext, null);
                final EditText edittext = editTextView.findViewById(R.id.DialogEditText);
                edittext.setText(Title);
                ArrayList<String> choiceList = new ArrayList<>();
                choiceList.add("削除");
                choiceList.add("名前を変更");
                choiceList.add("タイトルヘッダー画像登録");

                File file = new File(getFilesDir().getPath() + "/" + Title + "_header.jpg");
                if (file.exists()) {
                    choiceList.add("タイトルヘッダー画像削除");
                }


                String[] CastingList = choiceList.toArray(new String[0]);


                new AlertDialog.Builder(MainActivity.this)
                        .setItems(CastingList, (dialogInterface, which) -> {
                            if (which == 0) {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setMessage("選択したタイトルを1件削除しますか？同じタイトルで保存されている情報もすべて削除されるので注意してください")
                                        .setPositiveButton("削除", (dialogInterface17, i) -> {
                                            delete = true;
                                            RecycleItems.remove(position);
                                            recyclerViewAdapter.notifyItemRemoved(position);
                                            snackbar = Snackbar.make(layout, "[" + Title + "]を1件削除しました", BaseTransientBottomBar.LENGTH_SHORT);
                                            snackbar.addCallback(new Snackbar.Callback() {
                                                @Override
                                                public void onDismissed(Snackbar snackbar, int event) {
                                                    deleteData();
                                                }

                                                @Override
                                                public void onShown(Snackbar snackbar) {
                                                }
                                            });
                                            snackbar.setAction("元に戻す", v -> {
                                                RecycleItems.add(position, Title);
                                                recyclerViewAdapter.notifyItemRangeInserted(position, 1);
                                                delete = false;
                                            }).show();
                                            editor.apply();
                                        })
                                        .setNegativeButton("キャンセル", (dialogInterface16, i) -> {
                                        })
                                        .show();
                            }
                            if (which == 1) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                AlertDialog dialog;
                                builder.setMessage("タイトル名を入力")
                                        .setView(editTextView)
                                        .setPositiveButton("変更", (dialogInterface15, i) -> {
                                            String changeTitle = edittext.getText().toString();
                                            dbAdapter.changeTitle(Title, changeTitle);
                                            dbAdapter.changeHeader(changeTitle, Title + "_header.jpg", changeTitle + "_header.jpg");
                                            File oldFile = new File(getFilesDir().getPath() + "/" + Title + "_header.jpg");
                                            File newFile = new File(getFilesDir().getPath() + "/" + changeTitle + "_header.jpg");
                                            if (oldFile.exists()) {
                                                oldFile.renameTo(newFile);
                                            }

                                            loadTitle(searchWord);

                                            if (mPref.getBoolean("AutoSync", false)) {
                                                Intent intent = new Intent();
                                                intent.putExtra("Job", "RENAME_TITLE");
                                                AsyncWork.enqueueWork(getApplicationContext(), intent);
                                            }
                                            Snackbar.make(layout, "タイトル名を変更しました", Snackbar.LENGTH_SHORT).show();

                                        })
                                        .setNegativeButton("キャンセル", null);

                                dialog = builder.create();
                                edittext.requestFocus();
                                Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                dialog.show();
                            }

                            if (which == 2) {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setMessage("選択したタイトル名のヘッダー画像を一括で変更します。個別で設定されているヘッダー画像は変更されません。")
                                        .setPositiveButton("変更", (dialogInterface14, i) -> {

                                            if (ContextCompat.checkSelfPermission(
                                                    MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                                                    PackageManager.PERMISSION_GRANTED) {

                                                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                                if (intent.resolveActivity(getPackageManager()) != null) {
                                                    startActivityForResult(intent, RESULT_PICK_TITLE_HEADER);
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "画像選択アプリケーションが見つかりませんでした", Toast.LENGTH_SHORT).show();
                                                }
                                            } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                                new AlertDialog.Builder(MainActivity.this)
                                                        .setMessage("権限がないためストレージの画像ファイルにアクセスできません。権限を許可するためにアプリの設定を開きますか？")
                                                        .setPositiveButton("開く", (dialogInterface141, i1) -> {
                                                            String uriString = "package:" + getPackageName();
                                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(uriString));
                                                            startActivity(intent);
                                                        })
                                                        .setNegativeButton("キャンセル", null)
                                                        .show();

                                            } else {
                                                new AlertDialog.Builder(MainActivity.this)
                                                        .setMessage("画像ファイルにアクセスするために権限を付与してください。この権限は画像ファイルの読み込み以外には使用しません。")
                                                        .setPositiveButton("確認", (dialogInterface1412, i12) -> ActivityCompat.requestPermissions(MainActivity.this,
                                                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                                REQUEST_CODE))
                                                        .show();
                                            }
                                        })
                                        .setNegativeButton("キャンセル", (dialogInterface13, i) -> {
                                        })
                                        .show();
                            }
                            if (which == 3) {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setMessage("タイトルに登録されているデフォルトのヘッダー画像を削除しますか？")
                                        .setPositiveButton("削除", (dialogInterface12, i) -> {
                                            dbAdapter.changeHeader(Title, Title + "_header.jpg", "");
                                            File file1 = new File(getFilesDir().getPath() + "/" + Title + "_header.jpg");
                                            file1.delete();
                                            Snackbar.make(layout, "ヘッダー画像を削除しました", Snackbar.LENGTH_SHORT).show();
                                        })
                                        .setNegativeButton("キャンセル", (dialogInterface1, i) -> {
                                        })
                                        .show();
                            }
                        })
                        .show();


            }
        };
        recyclerView.setAdapter(recyclerViewAdapter);
        loadTitle(searchWord);
        setTitle("Account Manager");

        animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.add_fab_animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(getApplication(), CreateDataActivity.class);
                startActivityForResult(intent, RESULT_ADD);
                overridePendingTransition(R.anim.under_silde_in, R.anim.dropdown_exit);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        //フローティングボタンタップ時の動作設定
        fab.setOnClickListener(v -> {
            fab.startAnimation(animation);
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && fab.isShown()) {
                    fab.hide();
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fab.show();
                }

                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }


    //オプショナルボタンの実装
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        ActionBar actionBar = getSupportActionBar();

        inflater.inflate(R.menu.menu, menu);
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(false);
        searchItem = menu.findItem(R.id.menu_search);
        searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();

        EditText txtSearch = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        txtSearch.setTextColor(Color.WHITE);

        searchView.setQueryHint("検索ワードを入力");
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {

            //サーチビューの文字列取得リスナ
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchWord = newText;
                loadTitle(searchWord);
                return true;
            }


        });
        searchView.setOnFocusChangeListener((v, hasFocus) -> {
        });


        new Handler().post(() -> {

            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(500); // half second between each showcase view
            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(MainActivity.this, SHOWCASE_ID);
            sequence.setConfig(config);

            //tutorial
            sequence.addSequenceItem(
                    new MaterialShowcaseView.Builder(MainActivity.this)
                            .setTarget(fab)
                            .setContentText("このボタンからアカウント情報の登録ができます。")
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
            case R.id.BGedit: //タップした項目のIDと一致した場合実行
                ArrayList<String> choiceList = new ArrayList<>();
                choiceList.add("壁紙登録");
                choiceList.add("オーバーレイ調整");

                File wallpaper = new File(getFilesDir().getPath() + "/wallpaper.jpg");
                if (wallpaper.exists()) {
                    choiceList.add("壁紙削除");
                }
                String[] CastingList = choiceList.toArray(new String[0]);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("壁紙設定")
                        .setItems(CastingList, (dialogInterface, which) -> {
                            if (which == 0) {

                                if (ContextCompat.checkSelfPermission(
                                        MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                                        PackageManager.PERMISSION_GRANTED) {

                                    // You can use the API that requires the permission.
                                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                                    if (intent.resolveActivity(getPackageManager()) != null) {
                                        startActivityForResult(intent, RESULT_PICK_WALLPAPER);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "画像選択アプリケーションが見つかりませんでした", Toast.LENGTH_SHORT).show();
                                    }


                                } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                    // In an educational UI, explain to the user why your app requires this
                                    // permission for a specific feature to behave as expected. In this UI,
                                    // include a "cancel" or "no thanks" button that allows the user to
                                    // continue using your app without granting the permission.
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setMessage("権限がないためストレージの画像ファイルにアクセスできません。権限を許可するためにアプリの設定を開きますか？")
                                            .setPositiveButton("開く", (dialogInterface13, i) -> {
                                                String uriString = "package:" + getPackageName();
                                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(uriString));
                                                startActivity(intent);
                                            })
                                            .setNegativeButton("キャンセル", null)
                                            .show();

                                } else {
                                    // You can directly ask for the permission.
                                    // The registered ActivityResultCallback gets the result of this request.
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setMessage("画像ファイルにアクセスするために権限を付与してください。この権限は画像ファイルの読み込み以外には使用しません。")
                                            .setPositiveButton("確認", (dialogInterface14, i) -> ActivityCompat.requestPermissions(MainActivity.this,
                                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                    REQUEST_CODE))
                                            .show();
                                }

                            }
                            if (which == 1) {
                                LayoutInflater inflater = getLayoutInflater();
                                View view = inflater.inflate(R.layout.seekbar, null);
                                SeekBar seekBar = view.findViewById(R.id.seekBar1);
                                TextView seekBarValue = view.findViewById(R.id.seekBar1Value);
                                int FilterValue = new GetFilter(myContext).getValue();
                                //int invFilterValue = 255 - FilterValue;

                                seekBar.setProgress(FilterValue);
                                seekBarValue.setText(String.valueOf(FilterValue));


                                seekBar.setOnSeekBarChangeListener(
                                        new SeekBar.OnSeekBarChangeListener() {
                                            //ツマミがドラッグされると呼ばれる
                                            @Override
                                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                Wallpaper.setColorFilter(Color.argb(progress, 0, 0, 0));
                                                seekBarValue.setText(String.valueOf(progress));
                                            }

                                            //ツマミがタッチされた時に呼ばれる
                                            @Override
                                            public void onStartTrackingTouch(SeekBar seekBar) {

                                            }

                                            //ツマミがリリースされた時に呼ばれる
                                            @Override
                                            public void onStopTrackingTouch(SeekBar seekBar) {

                                            }

                                        });


                                AlertDialog seekBarDialog = new AlertDialog.Builder(this)
                                        .setView(view)
                                        .setPositiveButton("変更", (dialog, i) -> {
                                            dbAdapter.saveWallpaper(null,FilterValue, "FILTER", false);
                                            Snackbar.make(layout, "オーバーレイを変更しました", Snackbar.LENGTH_SHORT).show();
                                        })
                                        .setNegativeButton("キャンセル", (dialog, i) -> {
                                            Wallpaper.setColorFilter(Color.argb(FilterValue, 0, 0, 0));
                                        })
                                        .setOnDismissListener(dialog -> {
                                            Wallpaper.setColorFilter(Color.argb(FilterValue, 0, 0, 0));
                                        })
                                        .create();

                                setThumb(seekBar);

                                Objects.requireNonNull(seekBarDialog.getWindow()).setFlags(0, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                                seekBarDialog.show();
                            }

                            if (which == 2) {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setMessage("壁紙を削除しますか？")
                                        .setPositiveButton("削除", (dialogInterface1, i) -> {

                                            File file = new File(getFilesDir().getPath() + "/wallpaper.jpg");
                                            if (file.exists()) {
                                                file.delete();
                                            }
                                            ISetImage.setWallpaper(myContext, Wallpaper).set();
                                        })
                                        .setNegativeButton("キャンセル", (dialogInterface12, i) -> {
                                        })
                                        .show();
                            }
                        })
                        .show();

                return true;

            case R.id.other:

                Intent option = new Intent(getApplication(), OtherSettingsActivity.class);
                //Intent option = new Intent(getApplication(), AppSettingsActivity.class);
                startActivityForResult(option, RESULT_OPTION);
                overridePendingTransition(R.anim.under_silde_in, R.anim.dropdown_exit);
                return true;

            case R.id.sort:

                int Sort = mPref.getInt("Sort", 0);
                final String[] Title = new String[]{
                        "登録順",
                        "名前順"};
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("並べ替え方法選択")
                        .setSingleChoiceItems(Title, Sort, (dialogInterface, button) -> {
                            if (button == 0) {
                                Choice = 0;
                            }
                            if (button == 1) {
                                Choice = 1;
                            }
                        })
                        .setPositiveButton("決定", (dialogInterface, i) -> {
                            if (Choice == 0) {
                                editor.putBoolean("ABC", false);
                                editor.putInt("Sort", 0);
                                editor.apply();
                            }
                            if (Choice == 1) {
                                editor.putBoolean("ABC", true);
                                editor.putInt("Sort", 1);
                                editor.apply();
                            }
                            loadTitle(searchWord);
                            recyclerView.startLayoutAnimation();
                        })
                        .show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //ファイル選択アクティビティ終了時の結果によって分岐させるメソッド

    //引数1:リクエスト時コード,引数2:結果コード,引数3:結果データ
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        Intent intent = new Intent(getApplicationContext(), CropImageActivity.class);
        ISetImage.setWallpaper(myContext, Wallpaper).set();

        if (resultCode == RESULT_OK && resultData != null) {
            if (requestCode == RESULT_PICK_WALLPAPER) { //結果コードの変動なし　かつ　リザルトOK時に実行

                Uri uri; //URI変数の定義
                //結果データが空でないとき
                uri = resultData.getData(); //URIに結果データを代入

                int Width = Wallpaper.getWidth();
                int Height = Wallpaper.getHeight();
                Log.d("Viewのサイズ幅", String.valueOf(Wallpaper.getWidth()));
                Log.d("Viewのサイズ高さ", String.valueOf(Wallpaper.getHeight()));

                intent.putExtra("SetWallpaper", true);
                intent.putExtra("Width", Width);
                intent.putExtra("Height", Height);
                intent.putExtra("URI", Objects.requireNonNull(uri).toString());//test

                startActivityForResult(intent, RESULT_CHANGE);
            } else if (requestCode == RESULT_PICK_TITLE_HEADER) {
                Uri uri; //URI変数の定義
                //結果データが空でないとき
                uri = resultData.getData(); //URIに結果データを代入

                int Width = Wallpaper.getWidth();
                Log.d("Viewのサイズ幅", String.valueOf(Wallpaper.getWidth()));
                Log.d("Viewのサイズ高さ", String.valueOf(Wallpaper.getHeight()));

                intent.putExtra("TitleHeader", Title);
                intent.putExtra("Width", Width);
                intent.putExtra("URI", Objects.requireNonNull(uri).toString());//test

                startActivityForResult(intent, RESULT_CHANGE);
            } else if (requestCode == RESULT_CHANGE) {
                if (resultData.getBooleanExtra("CHANGE_WALLPAPER", false)) {
                    ISetImage.setWallpaper(myContext, Wallpaper).set();
                    Snackbar.make(layout, "壁紙を登録しました", Snackbar.LENGTH_SHORT).show();
                } else if (resultData.getBooleanExtra("CHANGE_TITLE_HEADER", false)) {
                    Snackbar.make(layout, "ヘッダー画像を登録しました", Snackbar.LENGTH_SHORT).show();
                }
            } else if (requestCode == RESULT_ADD) {
                String titleName = resultData.getStringExtra("TITLE_NAME");
                if (titleName != null) {
                    loadTitle(searchWord);
                    Snackbar.make(layout, "[" + titleName + "]を1件追加しました", Snackbar.LENGTH_SHORT).show();
                }

            }
        } else if (requestCode == RESULT_OPTION) {
            ISetImage.setWallpaper(myContext, Wallpaper).set();

            loadTitle(searchWord);


        }
    }

    private void loadTitle(String query) {
        RecycleItems.clear();

        String columns = SQL.COL_TITLE;
        String order_by = null;
        if (mPref.getBoolean("ABC", false)) {
            order_by = SQL.COL_TITLE;
        }

        Cursor c = dbAdapter.searchData(true, columns, query, order_by);
        if (c.moveToFirst()) {
            do {
                RecycleItems.add(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();
        recyclerViewAdapter.notifyDataSetChanged();

    }

    private void findViews() {
        Wallpaper = findViewById(R.id.wallpaper);
        layout = findViewById(R.id.coordinatorlayout);
        fab = findViewById(R.id.fab);
        recyclerView = findViewById(R.id.recyclerview1);
        toolbar = findViewById(R.id.my_toolbar);
    }

    private void deleteData() {
        if (delete) {
            File file = new File(getFilesDir().getPath() + "/" + Title + "_header.jpg");
            if (file.exists()) {
                file.delete();
            }
            Cursor c = dbAdapter.getIds(Title);
            if (c.moveToFirst()) {
                do {
                    dbAdapter.deleteAccount(c.getString(0));
                    File files = new File(getFilesDir().getPath() + "/" + c.getString(7));
                    if (files.exists()) {
                        files.delete();
                    }


                } while (c.moveToNext());
            }
            c.close();
        }
        delete = false;
    }

    private void setThumb(SeekBar seekBar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            switch (theme) {
                case R.style.NoActionBar:
                    seekBar.setThumb(ResourcesCompat.getDrawable(getResources(), R.drawable.thumb_pink, null));
                    break;

                case R.style.NoActionBarCyan:
                    seekBar.setThumb(ResourcesCompat.getDrawable(getResources(), R.drawable.thumb_cyan, null));
                    break;

                case R.style.NoActionBarOrange:
                    seekBar.setThumb(ResourcesCompat.getDrawable(getResources(), R.drawable.thumb_orange, null));
                    break;

                case R.style.NoActionBarGreen:
                    seekBar.setThumb(ResourcesCompat.getDrawable(getResources(), R.drawable.thumb_green, null));
                    break;
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

                new AlertDialog.Builder(MainActivity.this)
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

}
