package DataBase;

import android.content.Context;
import android.database.Cursor;

import java.util.Objects;

import Value.Filter;

/**
 * 背景フィルタのアルファ値を取得するクラス
 */

public class GetFilter {
    private Context context;

    public GetFilter(Context context) {
        this.context = context;
    }

    /**
     * @return アルファ値 Filter型
     */

    public int getValue() {
        SQL dbAdapter = new SQL(context);
        dbAdapter.openData();
        Cursor c = dbAdapter.getWallpaper();
        Filter filter = null;
        if (c.moveToFirst()) {
            filter = new Filter(c.getInt(0));
        }
        c.close();

        return Objects.requireNonNull(filter).getFilter();
    }


}
