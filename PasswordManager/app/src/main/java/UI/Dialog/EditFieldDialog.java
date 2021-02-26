package UI.Dialog;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import androidx.constraintlayout.widget.ConstraintLayout;

import UI.Field.ICreateField;
import UI.InputFiled.View.IFieldView;
import UI.InputFiled.View.PasswordFieldView;

public class EditFieldDialog extends CustomFieldDialog {
    private PasswordFieldView passwordFieldView;
    private int inputType;

    public EditFieldDialog(Context context,PasswordFieldView passwordFieldView) {
        super(context);
        this.passwordFieldView = passwordFieldView;
    }

    @Override
    public CustomFieldDialog setDefaultSpinnerSelection(Spinner spinner) {
        passwordFieldView.setSpinnerSelection(spinner);
        return this;
    }

    @Override
    public void dialogPositiveButtonAction(View dialogView,EditText editText) {
        final String fieldName = editText.getText().toString();
        passwordFieldView.setHint(fieldName);
        passwordFieldView.changeField(inputType);
    }


    @Override
    public void spinnerItemSelected(AdapterView<?> parent,View view, int itemIndex, long id) {
        inputType = itemIndex;
    }

    @Override
    public void spinnerItemNothingSelected(AdapterView<?> parent) {
    }
}
