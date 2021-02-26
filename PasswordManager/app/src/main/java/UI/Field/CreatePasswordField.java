package UI.Field;

import android.content.Context;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;

import com.example.accountmanager.Functions;
import com.example.accountmanager.R;

import UI.Dialog.GeneratePasswordDialog;

public class CreatePasswordField extends CreateField {
    private Context context;
    private ImageView visibleIcon;
    private ImageView generatePasswordIcon;
    private GeneratePasswordDialog passwordDialog;
    private boolean passwordVisible = false;

    public CreatePasswordField(Context context, LinearLayoutCompat parentLayout, Button addButton) {
        super(context, parentLayout, addButton);
        this.context = context;
        this.visibleIcon = new ImageView(context);
        this.generatePasswordIcon = new ImageView(context);
    }

    @Override
    public void remove() {

    }

    @Override
    public void edit() {

    }

    @Override
    public void create() {
        super.setParams(visibleIcon);

        final int dp_value = Functions.convertDP(context, 12);
        generatePasswordIcon.setId(ViewCompat.generateViewId());
        generatePasswordIcon.setPadding(dp_value, dp_value, dp_value, dp_value);
        super.addIcon(generatePasswordIcon);
        generatePasswordIcon.setImageResource(R.drawable.ic_baseline_vpn_key_24);
        ConstraintLayout.LayoutParams generatePasswordIconLayoutParams =
                new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        generatePasswordIconLayoutParams.endToStart = super.getIconId();
        generatePasswordIconLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        generatePasswordIconLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        generatePasswordIcon.setLayoutParams(generatePasswordIconLayoutParams);

        visibleIcon.setId(ViewCompat.generateViewId());
        visibleIcon.setPadding(dp_value, dp_value, dp_value, dp_value);
        super.addIcon(visibleIcon);
        visibleIcon.setImageResource(R.drawable.ic_baseline_visibility_24);
        ConstraintLayout.LayoutParams visibleIconLayoutParams =
                new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        visibleIconLayoutParams.endToStart = generatePasswordIcon.getId();
        visibleIconLayoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        visibleIconLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        visibleIcon.setLayoutParams(visibleIconLayoutParams);

        Functions.setRippleEffect(context,generatePasswordIcon,visibleIcon);


        visibleIcon.setOnClickListener(v -> {

            if (passwordDialog != null) {
                passwordVisible = passwordDialog.isVisible();
                passwordDialog = null;
            }
            passwordVisible = !passwordVisible;
            passwordVisibility(passwordVisible, this);
            super.focusText();
        });

        generatePasswordIcon.setOnClickListener(v -> {
            passwordDialog = new GeneratePasswordDialog(context, passwordVisible, visibleIcon);
            passwordDialog.generateDialog(this);
        });


        super.create();
    }

    private void passwordVisibility(boolean visible, CreateField createField) {
        if (visible) {
            createField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            visibleIcon.setImageResource(R.drawable.ic_baseline_visibility_off_24);
            return;
        }
        createField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        visibleIcon.setImageResource(R.drawable.ic_baseline_visibility_24);

    }
}
