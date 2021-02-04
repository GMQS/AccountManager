package File.IO;

import android.content.Context;

import java.io.File;

public class CreateTitleHeaderFile extends CreateFile {
    String titleName;

    public CreateTitleHeaderFile(Context context, String titleName) {
        super(context);
        this.titleName = titleName;
    }

    @Override
    public File create() {
        return new File(context.getFilesDir().getPath() + "/" + titleName + "_header.jpg");
    }
}
