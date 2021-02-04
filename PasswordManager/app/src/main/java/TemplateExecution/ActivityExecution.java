package TemplateExecution;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import DataBase.SQL;
import UI.RecyclerViewAdapter;

public class ActivityExecution extends AppCompatActivity {
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
    private int FilterValue;
    private int Prog;
    private int invProg;
    private SeekBar seekBar;
    private TextView seekBarValue;
    private AlertDialog seekBarDialog;
    private boolean ChangeOverlay;
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


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbAdapter.openData();


    }
}
