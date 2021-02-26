package UI.InputFiled.View;

import android.widget.Spinner;

public interface IFieldView {
    void setSpinnerSelection(Spinner spinner);
    void setHint(String hint);
    void changeField(int inputType);

}
