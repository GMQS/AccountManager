package Design;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.widget.ImageView;
import com.example.accountmanager.R;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import DataBase.GetFilter;
import File.IO.CreateFile;
import File.IO.CreateWallpaperFile;

/**
 * ImageViewに画像ファイルをセットするクラス
 */
class SetWallpaper implements ISetImage{

    private Context context;
    private ImageView wallpaper;


    /**
     * コンストラクタ
     * @param context コンテキスト
     * @param wallpaper セットするImageView
     */
    public SetWallpaper(Context context, ImageView wallpaper) {
        this.context = context;
        this.wallpaper = wallpaper;
    }


    /**
     * 画像ファイルを読み込んでイメージビューにセット
     */

    @Override
    public void set() {
        CreateFile createFile = new CreateWallpaperFile(context);
        File file = createFile.create();
        if (file.exists()) {
            BufferedInputStream bis = null;
            try {
                bis = new BufferedInputStream(context.openFileInput("wallpaper.jpg"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap bmp = BitmapFactory.decodeStream(bis);
            wallpaper.setImageBitmap(bmp);
            return;
        }
        Resources r = context.getResources();
        Bitmap defaultBmp = BitmapFactory.decodeResource(r, R.drawable.header_default);
        wallpaper.setImageBitmap(defaultBmp);
        wallpaper.setColorFilter(Color.argb(new GetFilter(context).getValue(), 0, 0, 0));
    }

}
