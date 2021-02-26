package UI.Field;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;

import com.example.accountmanager.Functions;
import com.example.accountmanager.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public abstract class CreateField implements ICreateField {

    private Context context;
    private LinearLayoutCompat parentLayout;
    private Button addButton;

    private ConstraintLayout constraintLayout;
    private TextInputLayout textInputLayout;
    private TextInputEditText textInputEditText;
    private ImageView editIcon;
    private ConstraintLayout.LayoutParams textLayoutParams;

    public CreateField(Context context, LinearLayoutCompat parentLayout, Button addButton) {
        this.context = context;
        this.parentLayout = parentLayout;
        this.addButton = addButton;

        this.constraintLayout = new ConstraintLayout(context);
        this.textInputLayout = new TextInputLayout(context);
        this.textInputEditText = new TextInputEditText(context);
        this.editIcon = new ImageView(context);
        this.textLayoutParams = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void addIcon(ImageView icon) {
        this.constraintLayout.addView(icon);
    }

    public int getIconId() {
        return this.editIcon.getId();
    }


    public void setParams(ImageView anchorIcon) {
        //レイアウトを設定
        textLayoutParams.endToStart = anchorIcon.getId();
    }

    public void setText(String text){
        textInputEditText.setText(text);
    }

    public String getText(){
        return Objects.requireNonNull(this.textInputEditText.getText()).toString();
    }

    public void focusText() {
        textInputEditText.requestFocus();
        textInputEditText.setSelection(Objects.requireNonNull(textInputEditText.getText()).length());
    }

    public void setInputType(int inputType) {
        textInputEditText.setInputType(inputType);
    }

    @Override
    public void create() {


        //ビューの追加
        parentLayout.removeView(addButton);
        parentLayout.addView(constraintLayout);
        constraintLayout.addView(textInputLayout);
        textInputLayout.addView(textInputEditText);
        constraintLayout.addView(editIcon);
        parentLayout.addView(addButton);

        //ビューID生成
        constraintLayout.setId(ViewCompat.generateViewId());
        textInputLayout.setId(ViewCompat.generateViewId());
        textInputEditText.setId(ViewCompat.generateViewId());
        editIcon.setId(ViewCompat.generateViewId());


        //アイコン詳細設定
        editIcon.setImageResource(R.drawable.ic_baseline_more_vert_24);

        //単位をdpにして設定
        int dp_value = Functions.convertDP(context, 12);
        editIcon.setPadding(dp_value, dp_value, dp_value, dp_value);
        Functions.setRippleEffect(context, editIcon);

        //テキスト詳細設定
        textInputLayout.setHintTextAppearance(R.style.AppTheme_Design_TextAppearance_Design_Hint);
        textInputEditText.setTextColor(context.getResources().getColor(R.color.colorText));


        //レイアウトを設定
        textLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        textLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        textLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        textInputLayout.setLayoutParams(textLayoutParams);


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

        //アイコンクリックリスナーを設定
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
                                    remove();
                                    parentLayout.removeView(constraintLayout);
                                }).setNegativeButton("キャンセル", null)
                                .show();
                        break;
                    case "フィールドを編集":
                        edit(); //抽象メソッド
                        break;
                }
                return false;
            });

        });
    }
}
