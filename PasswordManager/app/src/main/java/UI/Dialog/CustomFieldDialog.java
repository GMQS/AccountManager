package UI.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;

import com.example.accountmanager.R;

import java.util.Objects;

public abstract class CustomFieldDialog implements ICustomFieldDialog {

    private Context context;
    private AlertDialog.Builder builder;
    private String buttonText = "Positive";

    public CustomFieldDialog(Context context) {
        this.context = context;
        builder = new AlertDialog.Builder(context);
    }


    public CustomFieldDialog setDialogTitle(String title) {
        builder.setTitle(title);
        return this;
    }

    public CustomFieldDialog setButtonText(String text) {
        buttonText = text;
        return this;

    }

    public CustomFieldDialog setDefaultSpinnerSelection(Spinner spinner){
        return this;
    }

    public void show() {
        final View dialogView = View.inflate(context, R.layout.custom_field_dialog, null);
        final EditText editText = dialogView.findViewById(R.id.fieldTitle);
        final Spinner spinner = dialogView.findViewById(R.id.selectInputSpinner);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.fieldType, android.R.layout.simple_spinner_item);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final AlertDialog alertDialog;

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        setDefaultSpinnerSelection(spinner);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerItemSelected(parent,view, position, id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinnerItemNothingSelected(parent);
            }
        });

        builder.setView(dialogView)
                .setPositiveButton(buttonText, (dialog, which) -> {
                    dialogPositiveButtonAction(dialogView,editText);
                })
                .setNegativeButton("キャンセル", null);

        alertDialog = builder.create();
        editText.requestFocus();
        Objects.requireNonNull(alertDialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alertDialog.show();
    }


}
