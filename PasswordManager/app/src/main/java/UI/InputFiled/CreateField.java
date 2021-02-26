package UI.InputFiled;

import android.content.Context;
import android.text.InputType;
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

import java.util.ArrayList;
import java.util.Objects;

import UI.InputFiled.View.BaseView;

public abstract class CreateField {
    //private ArrayList<String> hintTextList;

    private BaseView baseView;


    public CreateField(Context context, LinearLayoutCompat linearLayoutCompat, Button button, ArrayList<TextInputEditText> editTextList) {
        //this.hintTextList = hintTextList;
        this.baseView = new BaseView(context,linearLayoutCompat,button,editTextList);
    }

    public BaseView getView(){
        return this.baseView;
    }

    public abstract void create();

}
