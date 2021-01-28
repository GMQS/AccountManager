package com.example.accountmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

public class ColorPalett extends AppCompatActivity implements ColorPickerDialogListener,View.OnClickListener {

    private SharedPreferences mPref;
    private static final int DIALOG_ID = 0;
    Intent intent;

    @Override public void onColorSelected(int dialogId, int color) {
        if (dialogId == DIALOG_ID) {
            intent.putExtra("Temporary_Color", color);
            overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
            setResult(RESULT_OK,intent);
            finish();
        }
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
    }

    @Override public void onDialogDismissed(int dialogId) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = new Intent();

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(R.style.NoActionBar2);
        setContentView(R.layout.activity_color_palett);


        findViewById(R.id.FFD50000).setOnClickListener(this);
        findViewById(R.id.FFC51162).setOnClickListener(this);
        findViewById(R.id.FFAA00FF).setOnClickListener(this);
        findViewById(R.id.FF304FFE).setOnClickListener(this);
        findViewById(R.id.FF00B8D4).setOnClickListener(this);
        findViewById(R.id.FF00BFA5).setOnClickListener(this);
        findViewById(R.id.FF00C853).setOnClickListener(this);
        findViewById(R.id.FFAEEA00).setOnClickListener(this);
        findViewById(R.id.FFFFD600).setOnClickListener(this);
        findViewById(R.id.FFFF6D00).setOnClickListener(this);
        findViewById(R.id.FF3E2723).setOnClickListener(this);
        findViewById(R.id.FF263238).setOnClickListener(this);
        findViewById(R.id.close).setOnClickListener(this);
        findViewById(R.id.CustomColor).setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if (v != null) {
            switch (v.getId()) {
                case R.id.CustomColor:
                    ColorPickerDialog.newBuilder()
                            .setDialogTitle(R.string.ColorPickerDialogTitle)
                            .setDialogId(DIALOG_ID)
                            .setColor(Color.BLACK)
                            .show(this);
                    break;
                case R.id.close:
                    setResult(RESULT_CANCELED);
                    finish();
                    break;
                case R.id.FFD50000:
                    intent.putExtra("Temporary_Color", 0xFFD50000);
                    overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
                    setResult(RESULT_OK,intent);
                    finish();
                    break;
                case R.id.FFC51162:
                    intent.putExtra("Temporary_Color", 0xFFC51162);
                    overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
                    setResult(RESULT_OK,intent);
                    finish();
                    break;
                case R.id.FFAA00FF:
                    intent.putExtra("Temporary_Color", 0xFFAA00FF);
                    overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
                    setResult(RESULT_OK,intent);
                    finish();
                    break;
                case R.id.FF304FFE:
                    intent.putExtra("Temporary_Color", 0xFF304FFE);
                    overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
                    setResult(RESULT_OK,intent);
                    finish();
                    break;
                case R.id.FF00B8D4:
                    intent.putExtra("Temporary_Color", 0xFF00B8D4);
                    overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
                    setResult(RESULT_OK,intent);
                    finish();
                    break;
                case R.id.FF00BFA5:
                    intent.putExtra("Temporary_Color", 0xFF00BFA5);
                    overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
                    setResult(RESULT_OK,intent);
                    finish();
                    break;
                case R.id.FF00C853:
                    intent.putExtra("Temporary_Color", 0xFF00C853);
                    overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
                    setResult(RESULT_OK,intent);
                    finish();
                    break;
                case R.id.FFAEEA00:
                    intent.putExtra("Temporary_Color", 0xFFAEEA00);
                    overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
                    setResult(RESULT_OK,intent);
                    finish();
                    break;
                case R.id.FFFFD600:
                    intent.putExtra("Temporary_Color", 0xFFFFD600);
                    overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
                    setResult(RESULT_OK,intent);
                    finish();
                    break;
                case R.id.FFFF6D00:
                    intent.putExtra("Temporary_Color", 0xFFFF6D00);
                    overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
                    setResult(RESULT_OK,intent);
                    finish();
                    break;
                case R.id.FF3E2723:
                    intent.putExtra("Temporary_Color", 0xFF3E2723);
                    overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
                    setResult(RESULT_OK,intent);
                    finish();
                    break;
                case R.id.FF263238:
                    intent.putExtra("Temporary_Color", 0xFF263238);
                    overridePendingTransition(R.anim.flowup_in, R.anim.under_slide_out);
                    setResult(RESULT_OK,intent);
                    finish();
                    break;
            }
        }
    }
}