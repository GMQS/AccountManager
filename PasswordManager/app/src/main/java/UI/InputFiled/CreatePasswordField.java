package UI.InputFiled;

import android.content.Context;
import android.widget.Button;

import androidx.appcompat.widget.LinearLayoutCompat;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;

import UI.InputFiled.View.BaseView;
import UI.InputFiled.View.PasswordFieldView;

public class CreatePasswordField extends CreateField {

    public CreatePasswordField(Context context, LinearLayoutCompat linearLayoutCompat, Button button, ArrayList<TextInputEditText> editTextList) {
        super(context, linearLayoutCompat, button, editTextList);
    }

    @Override
    public void create() {
        PasswordFieldView passwordFieldView = new PasswordFieldView(super.getView());
        passwordFieldView.generateView();
        BaseView baseView = super.getView();
        baseView.create();
    }
}
