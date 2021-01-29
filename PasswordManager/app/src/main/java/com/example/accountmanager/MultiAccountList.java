package com.example.accountmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.example.accountmanager.SQL.COL_ACCOUNT;

public class MultiAccountList extends AppCompatActivity {

    private static final int RESULT_DELETE = 1;
    private static final int RESULT_ADD = 4000;

    private static final String SHOWCASE_ID = "Add2";
    private SQL dbAdapter = new SQL(this);
    private ImageView Wallpaper;
    private String title;
    private ArrayList<String> getsID;
    private ArrayList<String> getsName;
    private String ID;
    private CoordinatorLayout layout;
    private Boolean add = false;
    private String getAccount;
    private FloatingActionButton fab;
    private int Choice;
    private int lastCheck = 0;
    private SharedPreferences mPref;
    private SharedPreferences.Editor editor;
    private Animation animation;
    private Animation FabDelete;
    private Intent InfoIntent;
    private RecyclerView recyclerView;
    private ArrayList<String> RecycleItems;
    private ArrayList<String> RecycleMailItems;
    private RecyclerViewAdapter2 recyclerViewAdapter;

    private MenuItem searchItem;
    private androidx.appcompat.widget.SearchView searchView;
    private String searchWord = "";

    private boolean delete;
    private Snackbar snackbar;
    private String AccountName;
    private String MailName;
    private int Pos;
    private String infoID;
    private String infoTitle;
    private int infoLastCheck;
    private boolean infoDelete;

    private int sendPos;
    private String sendAccount;
    private String sendMail;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (snackbar != null) {
            snackbar.dismiss();
        }
        if (delete) {
            deleteData();
            delete = false;
        } else if (infoDelete) {
            deleteDataFromInfo();
            infoDelete = false;
        }
        editor.remove("Start");
        editor.apply();
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
        if (delete) {
            deleteData();
            delete = false;
        } else if (infoDelete) {
            deleteDataFromInfo();
            infoDelete = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        dbAdapter.openData();
        mPref = PreferenceManager.getDefaultSharedPreferences(this);//タイムスタンプ用
        editor = mPref.edit();
        setTheme(mPref.getInt("accent", R.style.NoActionBar));
        setContentView(R.layout.activity_multi_account_list);
        findViews(); //変数とIDの紐付け
        InfoIntent = new Intent(getApplication(), AccountInfo.class);

        Toolbar myToolbar = findViewById(R.id.my_toolbar2);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);

        title = getIntent().getStringExtra("TITLE_STRING");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL); //縦方向に設定
        recyclerView.setLayoutManager(layoutManager);

        RecycleItems = new ArrayList<>();
        RecycleMailItems = new ArrayList<>();

        try {
            Functions.setWallpaper(getFilesDir(), this, Wallpaper, dbAdapter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        setTitle("アカウント選択");
        getsID = new ArrayList<>();
        getsName = new ArrayList<>();


        Animation fabPop = AnimationUtils.loadAnimation(MultiAccountList.this, R.anim.fab_pop);
        fab.startAnimation(fabPop);

        recyclerViewAdapter = new RecyclerViewAdapter2(RecycleItems, RecycleMailItems, getApplicationContext()) {
            // onItemClick()をオーバーライドして
            // クリックイベントの処理を記述する
            @Override
            public void onItemClick(View view, int position, String itemData, String mailData) {
                String ID = getsID.get(position);
                sendPos = position;
                sendAccount = itemData;
                sendMail = mailData;
                InfoIntent.putExtra("id", ID);
                InfoIntent.putExtra("POSITION", sendPos);
                InfoIntent.putExtra("ACCOUNT", sendAccount);
                InfoIntent.putExtra("MAIL", sendMail);
                startActivityForResult(InfoIntent, RESULT_DELETE);
                overridePendingTransition(R.anim.left_slide_in, R.anim.dropdown_exit);
            }

            @Override
            public void onItemLongClick(View view, final int position, final String itemData, final String mailData) {
                ID = getsID.get(position);
                new AlertDialog.Builder(MultiAccountList.this)
                        //.setTitle("項目削除")
                        .setMessage("選択した項目を1件削除しますか？")
                        .setPositiveButton("削除", (dialogInterface, i) -> {
                            delete = true;
                            AccountName = itemData;
                            MailName = mailData;

                            RecycleItems.remove(position);
                            RecycleMailItems.remove(position);
                            recyclerViewAdapter.notifyItemRemoved(position);

                            snackbar = Snackbar.make(layout, "[" + AccountName + "]を1件削除しました", Snackbar.LENGTH_SHORT);
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
                                RecycleItems.add(position, AccountName);
                                RecycleMailItems.add(position, MailName);
                                recyclerViewAdapter.notifyItemRangeInserted(position, 1);
                                delete = false;
                            }).show();

                        })
                        .setNegativeButton("キャンセル", (dialogInterface, i) -> {
                        })
                        .show();
            }
        };
        recyclerView.setAdapter(recyclerViewAdapter);
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(MultiAccountList.this, SHOWCASE_ID);
        sequence.setConfig(config);

        //tutorial
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(MultiAccountList.this)
                        .setTarget(fab)
                        .setContentText("このボタンから同一タイトルでアカウントの追加ができます。")
                        .setDismissText("確認")
                        .build()
        );
        sequence.start();

        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.add_fab_animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(getApplication(), AddMultiAccount.class);
                intent.putExtra("TITLE_STRING",title);
                startActivityForResult(intent, RESULT_ADD);
                overridePendingTransition(R.anim.under_silde_in, R.anim.dropdown_exit);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        FabDelete = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.delete_fab);
        FabDelete.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fab.setOnClickListener(v -> {
            fab.startAnimation(animation);

        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                    fab.hide();
                } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                    fab.show();
                }
            }
        });

        loadAccount(true, searchWord);

    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.flowup_in, R.anim.left_slide_out);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(R.anim.flowup_in, R.anim.left_slide_out);
        return true;
    }

    //オプショナルボタンの実装
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.account_list, menu);

        searchItem = menu.findItem(R.id.menu_search_account);
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
                loadAccount(true, searchWord);
                return true;
            }


        });

        searchView.setOnFocusChangeListener((v, hasFocus) -> {
        });


        return true;
    }

    //オプショナルボタンの動作
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //項目の分岐処理
        if (item.getItemId() == R.id.sort2) {
            int Sort2 = mPref.getInt("Sort2", 0);
            final String[] Title = new String[]{
                    "登録順",
                    "名前順"};
            new AlertDialog.Builder(MultiAccountList.this)
                    .setTitle("並べ替え方法選択")
                    .setSingleChoiceItems(Title, Sort2, (dialogInterface, button) -> {
                        if (button == 0) {
                            Choice = 0;
                        }
                        if (button == 1) {
                            Choice = 1;
                        }
                    })
                    .setPositiveButton("決定", (dialogInterface, i) -> {
                        if (Choice == 0) {
                            editor.putBoolean("ABC_2", false);
                            editor.putInt("Sort2", 0);
                            editor.apply();
                        }
                        if (Choice == 1) {
                            editor.putBoolean("ABC_2", true);
                            editor.putInt("Sort2", 1);
                            editor.apply();
                        }
                        loadAccount(true, searchWord);
                        recyclerView.startLayoutAnimation();
                        //dbAdapter.closeData();
                    })
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    protected void onResume() {
        super.onResume();

        if (infoDelete) {
            RecycleItems.remove(Pos);
            RecycleMailItems.remove(Pos);
            recyclerViewAdapter.notifyItemRemoved(Pos);
        }

        if (mPref.getBoolean("Nothing", false)) {
            finish();
            editor.remove("Nothing");
            editor.apply();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == RESULT_OK && resultData != null) {
            if (requestCode == RESULT_DELETE) {

                if (resultData.getBooleanExtra("DELETE", false)) {
                    infoDelete = true;
                    AccountName = resultData.getStringExtra("ACCOUNT");
                    MailName = resultData.getStringExtra("MAIL");
                    Pos = resultData.getIntExtra("POSITION", 0);
                    infoID = resultData.getStringExtra("ID");
                    infoTitle = resultData.getStringExtra("TITLE");
                    infoLastCheck = resultData.getIntExtra("LAST_CHECK", 0);


                    snackbar = Snackbar.make(layout, "[" + AccountName + "]を1件削除しました", Snackbar.LENGTH_SHORT);
                    snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            deleteDataFromInfo();
                        }

                        @Override
                        public void onShown(Snackbar snackbar) {

                        }
                    });
                    snackbar.setAction("元に戻す", v -> {
                        RecycleItems.add(Pos, AccountName);
                        RecycleMailItems.add(Pos, MailName);
                        recyclerViewAdapter.notifyItemRangeInserted(Pos, 1);
                        infoDelete = false;
                    }).show();
                }
            } else if (requestCode == RESULT_ADD) {
                String accountName = resultData.getStringExtra("ACCOUNT_NAME");
                if (accountName != null) {
                    loadAccount(true, searchWord);
                    Snackbar.make(layout, "[" + accountName + "]を1件追加しました", Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void findViews() {
        recyclerView = findViewById(R.id.recyclerview1);
        Wallpaper = findViewById(R.id.wallpaper);
        layout = findViewById(R.id.coordinatorlayout2);
        fab = findViewById(R.id.fab_);
    }

    private void loadAccount(Boolean finish, String query) {
        RecycleItems.clear();
        RecycleMailItems.clear();
        getsID.clear();

        String order_by = null;

        if (mPref.getBoolean("ABC_2", false)) {
            order_by = COL_ACCOUNT;
        }
        Cursor c = dbAdapter.getAccount(title, order_by, query);

        if (c.moveToFirst()) {
            do {
                RecycleItems.add(c.getString(2));
                RecycleMailItems.add(c.getString(3));
                getsID.add(c.getString(0));

            } while (c.moveToNext());
        }
        c.close();
        recyclerViewAdapter.notifyDataSetChanged();

        if (!mPref.getBoolean("SkipChoiceAccount", false)) {
            int VisibleAccount = 1;
            if (getsID.size() == 1) {
                Cursor cursor = dbAdapter.ShowLabelInfo(getsID.get(0));
                if (cursor.moveToFirst()) {
                    VisibleAccount = cursor.getInt(6);
                }
                cursor.close();
                if (RecycleItems.get(0).equals("") || VisibleAccount == 0) {
                    if (!mPref.getBoolean("Start", false)) {
                        InfoIntent.putExtra("id", getsID.get(0));
                        startActivity(InfoIntent);
                    }
                    if (finish) {
                        finish();
                    }
                }
            }
        }
    }

    private void deleteData() {
        if (delete) {

            Cursor c = dbAdapter.getIds(title);
            if (c.moveToFirst()) {
                while ((c.moveToNext())) {
                    lastCheck++;
                }
            }
            c.close();

            if (lastCheck == 0) {
                File file2 = new File(getFilesDir().getPath() + "/" + title + "_header.jpg");
                if (file2.exists()) {
                    file2.delete();
                }
            }
            lastCheck = 0;

            dbAdapter.deleteAccount(ID);
            editor.putBoolean("Start", true);
            getsID.remove(ID);
            editor.remove(ID);
            editor.remove("RandomColor" + ID);
            editor.apply();

            File file = new File(getFilesDir().getPath() + "/header" + ID + ".jpg");
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private void deleteDataFromInfo() {
        if (infoDelete) {
            Cursor c = dbAdapter.getIds(infoTitle);
            if (c.moveToFirst()) {
                while ((c.moveToNext())) {
                    infoLastCheck++;
                }
            }
            c.close();
            dbAdapter.deleteAccount(infoID);
            File file = new File(getFilesDir().getPath() + "/header" + infoID + ".jpg");
            if (file.exists()) {
                file.delete();
            }

            if (infoLastCheck == 0) {
                File file2 = new File(getFilesDir().getPath() + "/" + infoTitle + "_header.jpg");
                if (file2.exists()) {
                    file2.delete();
                }
            }
            infoLastCheck = 0;
        }
    }

}