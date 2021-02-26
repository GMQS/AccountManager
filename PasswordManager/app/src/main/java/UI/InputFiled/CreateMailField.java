package UI.InputFiled;

import android.content.Context;
import android.text.InputType;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;


public class CreateMailField extends CreateField {


    public CreateMailField(Context context, LinearLayoutCompat linearLayoutCompat, Button button, ArrayList<TextInputEditText> editTextList) {
        super(context, linearLayoutCompat, button, editTextList);
    }

    @Override
    public void create() {




    }
}
