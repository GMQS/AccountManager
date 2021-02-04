package File.IO;

import android.content.Context;

import java.io.File;

public class CreateHeaderFile extends CreateFile {
    String ID;

    public CreateHeaderFile(Context context, String ID) {
        super(context);
        this.ID = ID;
    }

    public File create() {
        return new File(context.getFilesDir().getPath() + "/header" + ID + ".jpg");
    }

}
