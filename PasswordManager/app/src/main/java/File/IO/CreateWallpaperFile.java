package File.IO;

import android.content.Context;

import java.io.File;

public class CreateWallpaperFile extends CreateFile{
    Context context;

    public CreateWallpaperFile(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public File create() {
        return new File(context.getFilesDir().getPath() + "/wallpaper.jpg");
    }
}
