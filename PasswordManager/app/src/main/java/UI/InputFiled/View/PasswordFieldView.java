package UI.InputFiled.View;

import android.content.Context;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;

import com.example.accountmanager.Functions;
import com.example.accountmanager.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import UI.Dialog.GeneratePasswordDialog;

public class PasswordFieldView{
    private BaseView baseView;

    private Context context;
    private ImageView visibleIcon;
    private ImageView generatePasswordIcon;
    private boolean passwordVisible = false;
    private GeneratePasswordDialog passwordDialog;
    private boolean deleteIcon = false;

    public PasswordFieldView(BaseView baseView) {
        this.baseView = baseView;
        this.context = baseView.getContext();
        this.visibleIcon = new ImageView(context);
        this.generatePasswordIcon = new ImageView(context);
    }

    public void setHint(String hint){
        baseView.setHint(hint);
    }

    public void setSpinnerSelection(Spinner spinner) {
        switch (baseView.getInputType()) {
            case InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                spinner.setSelection(0);
                break;
            case InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD:
                spinner.setSelection(1);
                break;
            case InputType.TYPE_CLASS_TEXT:
                spinner.setSelection(2);
                break;
            case InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI:
                spinner.setSelection(3);
                break;
        }
    }


    public void changeField(int inputType) {
        switch (inputType) {
            case 0:
                deleteIcon = true;
                baseView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                baseView.removeIcon(visibleIcon);
                baseView.removeIcon(generatePasswordIcon);
                break;
            case 1:
                baseView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                if (deleteIcon) {
                    baseView.addIcon(visibleIcon);
                    baseView.addIcon(generatePasswordIcon);
                }
                deleteIcon = false;
                break;
            case 2:
                deleteIcon = true;
                baseView.setInputType(InputType.TYPE_CLASS_TEXT);
                baseView.removeIcon(visibleIcon);
                baseView.removeIcon(generatePasswordIcon);
                break;
            case 3:
                deleteIcon = true;
                baseView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
                baseView.removeIcon(visibleIcon);
                baseView.removeIcon(generatePasswordIcon);
                break;
        }
    }

    public void generateView() {
        final int dp_value = Functions.convertDP(context, 12);

        generatePasswordIcon.setId(ViewCompat.generateViewId());
        generatePasswordIcon.setPadding(dp_value, dp_value, dp_value, dp_value);
        baseView.addIcon(generatePasswordIcon);
        generatePasswordIcon.setImageResource(R.drawable.ic_baseline_vpn_key_24);
        ConstraintLayout.LayoutParams generatePasswordIconLayoutParams =
                new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        generatePasswordIconLayoutParams.endToStart = baseView.getBaseIconId();
        generatePasswordIconLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        generatePasswordIconLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        generatePasswordIcon.setLayoutParams(generatePasswordIconLayoutParams);

        visibleIcon.setId(ViewCompat.generateViewId());
        visibleIcon.setPadding(dp_value, dp_value, dp_value, dp_value);
        baseView.addIcon(visibleIcon);
        visibleIcon.setImageResource(R.drawable.ic_baseline_visibility_24);
        ConstraintLayout.LayoutParams visibleIconLayoutParams =
                new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        visibleIconLayoutParams.endToStart = generatePasswordIcon.getId();
        visibleIconLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        visibleIconLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        visibleIcon.setLayoutParams(visibleIconLayoutParams);

        baseView.setLayoutParams(visibleIcon);

        Functions.setRippleEffect(baseView.getContext(), generatePasswordIcon, visibleIcon);


        visibleIcon.setOnClickListener(v -> {
            if (passwordDialog != null) {
                passwordVisible = passwordDialog.isVisible();
                passwordDialog = null;
            }
            passwordVisible = !passwordVisible;
            passwordVisibility(passwordVisible, baseView);
            baseView.focusText();
        });

        generatePasswordIcon.setOnClickListener(v -> {
            passwordDialog = new GeneratePasswordDialog(context, passwordVisible, visibleIcon);
            passwordDialog.generateDialog(baseView);
        });
    }


    private void passwordVisibility(boolean visible,BaseView baseView) {
        if (visible) {
            baseView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            visibleIcon.setImageResource(R.drawable.ic_baseline_visibility_off_24);
            return;
        }
        baseView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        visibleIcon.setImageResource(R.drawable.ic_baseline_visibility_24);

    }
}
