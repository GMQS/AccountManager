package com.example.accountmanager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.view.ViewCompat;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;

import Text.JointText;
import UI.Field.CreatePasswordField;
import UI.Field.ICreateField;

public class DataRegistration extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(sharedPreferences.getInt("accent", R.style.NoActionBar));
        setContentView(R.layout.activity_data_registration);
        final LinearLayoutCompat linearLayoutCompat = findViewById(R.id.linearLayout);
        final TextInputEditText titleEditText = findViewById(R.id.titleEditText);
        final TextInputEditText accountEditText = findViewById(R.id.accountEditText);
        final TextInputLayout accountInputLayout = findViewById(R.id.accountInputLayout);


        final ImageView accountEditIcon = findViewById(R.id.accountEditIcon);
        final Button registerButton = findViewById(R.id.Decision);
        final Button addFieldButton = new Button(this);
        final ArrayList<TextInputEditText> editTextList = new ArrayList<>();
        final ArrayList<String> hintTextList = new ArrayList<>();

        editTextList.add(titleEditText);
        editTextList.add(accountEditText);

        hintTextList.add("タイトル");
        hintTextList.add("アカウント");


        setupView(linearLayoutCompat, addFieldButton);

        addFieldButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("追加するフィールドを選択")
                    .setItems(R.array.fieldSummary, (dialog, which) -> {
                        switch (which) {
                            case 0://メールアドレス
                                break;
                            case 1://パスワード
                                new CreatePasswordField(this,linearLayoutCompat,addFieldButton).create();
                                break;
                            case 2://ID
                                break;
                            case 3://メモ
                                break;
                            case 4://URL
                                break;
                            case 5://カスタム
                                break;
                        }
                    })
                    .show();

        });

        accountEditIcon.setOnClickListener(v -> {
            final PopupMenu popupMenu = new PopupMenu(this,accountEditIcon);
            final View editTextView = View.inflate(this,R.layout.edittext,null);
            final EditText editText = editTextView.findViewById(R.id.DialogEditText);
            editText.setText(accountInputLayout.getHint());

            popupMenu.getMenuInflater().inflate(R.menu.filed_options_2,popupMenu.getMenu());
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(item -> {
                if ("フィールド名を変更".equals(item.getTitle().toString())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    AlertDialog alertDialog;
                    builder.setMessage("フィールド名を入力")
                            .setView(editTextView)
                            .setPositiveButton("変更", (dialog, which) -> {
                                final String fieldName = editText.getText().toString();
                                accountInputLayout.setHint(fieldName);
                                hintTextList.set(1, fieldName);
                            })
                            .setNegativeButton("キャンセル", null);

                    alertDialog = builder.create();
                    editText.requestFocus();
                    Objects.requireNonNull(alertDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    alertDialog.show();
                }
                return false;
            });

        });



        registerButton.setOnClickListener(v -> {

            JointText jointText = new JointText(",");
            String editText = jointText.jointEditText(editTextList);
            String hintText = jointText.jointText(hintTextList);

            Toast.makeText(this, editText, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, hintText, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupView(final LinearLayoutCompat linearLayoutCompat, final Button button) {
        linearLayoutCompat.addView(button);
        button.setId(ViewCompat.generateViewId());
        button.setText("フィールドを追加");
        final LinearLayoutCompat.LayoutParams buttonLayoutParams =
                new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonLayoutParams.topMargin = Functions.convertDP(getApplicationContext(),32);
        buttonLayoutParams.bottomMargin = Functions.convertDP(getApplicationContext(),32);
        buttonLayoutParams.leftMargin = Functions.convertDP(getApplicationContext(),32);
        buttonLayoutParams.rightMargin = Functions.convertDP(getApplicationContext(),32);
        buttonLayoutParams.gravity = Gravity.CENTER;
        button.setLayoutParams(buttonLayoutParams);
    }
}