package Text;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class JointText {
    private String separator;

    public JointText(String separator) {
        this.separator = separator;
    }


    public String jointEditText(ArrayList<TextInputEditText> editTextList) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<TextInputEditText> il = editTextList.iterator(); il.hasNext(); ) {
            TextInputEditText et = il.next();
            sb.append(Objects.requireNonNull(et.getText()).toString());
            if (il.hasNext()) {
                sb.append(separator);
            }
        }
        return new String(sb);
    }

    public String jointText(ArrayList<String> textList){
        StringBuilder sb = new StringBuilder();
        for(Iterator<String> il = textList.iterator(); il.hasNext();){
            String str = il.next();
            sb.append(str);
            if(il.hasNext()){
                sb.append(separator);
            }
        }
        return new String(sb);
    }
}
