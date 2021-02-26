package UI.Dialog;

import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

public interface ICustomFieldDialog {
    void spinnerItemSelected(AdapterView<?> parent, View view, int itemIndex, long id);
    void spinnerItemNothingSelected(AdapterView<?> parent);
    void dialogPositiveButtonAction(View dialogView,EditText editText);
}
