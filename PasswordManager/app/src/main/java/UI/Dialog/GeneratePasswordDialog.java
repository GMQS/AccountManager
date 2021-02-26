package UI.Dialog;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.accountmanager.Functions;
import com.example.accountmanager.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import UI.Field.CreatePasswordField;
import UI.InputFiled.CreateField;
import UI.InputFiled.View.BaseView;

public class GeneratePasswordDialog {
    private Context context;
    private boolean visible;
    private ImageView icon;
    private EditText editText;
    private int randomLength;
    private String charSet;

    public GeneratePasswordDialog(Context context,boolean visible,ImageView icon){
        this.context = context;
        this.visible = visible;
        this.icon = icon;
    }


    public void generateDialog(CreatePasswordField passwordField){
        View passwordDialogView = View.inflate(context,R.layout.password_castom, null);
        SeekBar seekBar = passwordDialogView.findViewById(R.id.password_seekbar);
        ImageView imageView = passwordDialogView.findViewById(R.id.password_reflesh);
        TextView textView = passwordDialogView.findViewById(R.id.password_seekbar_value);
        Spinner spinner = passwordDialogView.findViewById(R.id.password_spinner);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.password_array,R.layout.spinner_custom);


        editText = passwordDialogView.findViewById(R.id.password_edittext);
        randomLength = sharedPreferences.getInt("PasswordLength", 15);



        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position){
                    case 0:
                        charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
                        break;
                    case 1:
                        charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$&*-+!?%^.:\\/{}[]=()<>";
                        break;
                    case 2:
                        charSet = "0123456789";
                        break;
                }
                editText.setText(Functions.getRandomString(randomLength, charSet));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                editText.setText(Functions.getRandomString(randomLength, charSet));
            }
        });

        imageView.setOnClickListener(v -> editText.setText(Functions.getRandomString(randomLength, charSet)));

        seekBar.setProgress(randomLength);
        textView.setText(String.valueOf(randomLength));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                randomLength = progress;
                editText.setText(Functions.getRandomString(progress, charSet));
                textView.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        new AlertDialog.Builder(context)
                .setTitle("パスワード生成")
                .setView(passwordDialogView)
                .setPositiveButton("変更", (dialog, which) -> {
                    if(!visible){
                        visible = true;
                        passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        icon.setImageResource(R.drawable.ic_baseline_visibility_off_24);
                    }
                    passwordField.setText("");
                    passwordField.setText(editText.getText().toString());
                    passwordField.focusText();
                }).setNegativeButton("キャンセル", null)
                .show();
    }

    public boolean isVisible(){
        return this.visible;
    }






}
