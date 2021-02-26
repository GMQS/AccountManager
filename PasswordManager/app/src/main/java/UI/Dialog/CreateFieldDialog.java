package UI.Dialog;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;

public class CreateFieldDialog extends CustomFieldDialog {

    public CreateFieldDialog(Context context) {
        super(context);
    }

    @Override
    public void spinnerItemNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void spinnerItemSelected(AdapterView<?> parent, View view, int itemIndex, long id) {

    }

    @Override
    public void dialogPositiveButtonAction(View dialogView, EditText editText) {

    }
}
