package Design;

import android.content.Context;
import android.media.Image;
import android.widget.ImageView;

public interface ISetImage {
    void set();
    static ISetImage setWallpaper(Context context, ImageView wallpaper){
        return new SetWallpaper(context,wallpaper);
    }



}
