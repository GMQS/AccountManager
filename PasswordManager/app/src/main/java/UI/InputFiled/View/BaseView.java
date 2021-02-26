package UI.InputFiled.View;

import android.content.Context;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;

import com.example.accountmanager.Functions;
import com.example.accountmanager.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;

import UI.Dialog.EditFieldDialog;

public class BaseView {
    private ConstraintLayout constraintLayout;
    private TextInputLayout textInputLayout;
    private TextInputEditText textInputEditText;
    private ImageView editIcon;


    private Context context;
    private LinearLayoutCompat linearLayoutCompat;
    private Button button;
    private ArrayList<TextInputEditText> editTextList;

    public BaseView(Context context, LinearLayoutCompat linearLayoutCompat, Button button, ArrayList<TextInputEditText> editTextList) {
        this.context = context;
        this.linearLayoutCompat = linearLayoutCompat;
        this.button = button;
        this.editTextList = editTextList;

        this.constraintLayout = new ConstraintLayout(context);
        this.textInputLayout = new TextInputLayout(context);
        this.textInputEditText = new TextInputEditText(context);
        this.editIcon = new ImageView(context);
    }

    public void create() {
        //ビューの追加
        linearLayoutCompat.removeView(button);
        linearLayoutCompat.addView(constraintLayout);
        constraintLayout.addView(textInputLayout);
        textInputLayout.addView(textInputEditText);
        constraintLayout.addView(editIcon);
        linearLayoutCompat.addView(button);


        //ビューID生成
        constraintLayout.setId(ViewCompat.generateViewId());
        textInputLayout.setId(ViewCompat.generateViewId());
        textInputEditText.setId(ViewCompat.generateViewId());
        editIcon.setId(ViewCompat.generateViewId());

        //editTextList.add(textInputEditText);


        //アイコン詳細設定
        editIcon.setImageResource(R.drawable.ic_baseline_more_vert_24);

        //単位をdpにして設定
        int dp_value = Functions.convertDP(context, 12);
        editIcon.setPadding(dp_value, dp_value, dp_value, dp_value);
        Functions.setRippleEffect(context, editIcon);

        //テキスト詳細設定
        textInputLayout.setHintTextAppearance(R.style.AppTheme_Design_TextAppearance_Design_Hint);
        textInputEditText.setTextColor(context.getResources().getColor(R.color.colorText));

    }

    public void focusText(){
        textInputEditText.requestFocus();
        textInputEditText.setSelection(Objects.requireNonNull(textInputEditText.getText()).length());
    }

    public void setInputType(int inputType) {
        textInputEditText.setInputType(inputType);
    }

    public void setHint(String hintText) {
        textInputLayout.setHint(hintText);
    }

    public Context getContext() {
        return this.context;
    }

    public int getInputType(){
        return this.textInputEditText.getInputType();
    }

    public void addIcon(ImageView icon){
        constraintLayout.addView(icon);
    }

    public void removeIcon(ImageView icon){
        constraintLayout.removeView(icon);
    }

    public int getBaseIconId(){
        return this.editIcon.getId();
    }

    public void setText(String text){
        textInputEditText.setText(text);
    }

    public String getText(){
        return Objects.requireNonNull(this.textInputEditText.getText()).toString();
    }


    public void setLayoutParams(ImageView anchorIcon) {
        final ConstraintLayout.LayoutParams textInputLayoutParams =
                new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        textInputLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        textInputLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        textInputLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        textInputLayoutParams.endToStart = anchorIcon.getId(); //abstract
        textInputLayout.setLayoutParams(textInputLayoutParams);


        final LinearLayoutCompat.LayoutParams constraintLayoutParams =
                new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        constraintLayoutParams.topMargin = Functions.convertDP(context, 32);
        constraintLayout.setLayoutParams(constraintLayoutParams);


        final FrameLayout.LayoutParams textInputEditTextParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textInputEditText.setLayoutParams(textInputEditTextParams);

        final ConstraintLayout.LayoutParams editIconLayoutParams =
                new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editIconLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        editIconLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        editIconLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        editIcon.setLayoutParams(editIconLayoutParams);
    }

    public BaseView setIconClickListener(EditFieldDialog editFieldDialog) {
        editIcon.setOnClickListener(v -> {
            final PopupMenu popupMenu = new PopupMenu(context, editIcon);
            popupMenu.getMenuInflater().inflate(R.menu.field_options, popupMenu.getMenu());
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getTitle().toString()) {
                    case "削除":
                        new AlertDialog.Builder(context)
                                .setMessage("フィールドを削除しますか？")
                                .setPositiveButton("削除", (dialog, which) -> {
                                    linearLayoutCompat.removeView(constraintLayout);
                                    editTextList.remove(textInputEditText);
                                    //hintTextList.remove(Objects.requireNonNull(textInputLayout.getHint()).toString());
                                }).setNegativeButton("キャンセル", null)
                                .show();
                        break;
                    case "フィールドを編集":
                        editFieldDialog.show();
                        break;
                }
                return false;
            });

        });
        return this;
    }
}
