package DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import net.sqlcipher.database.SQLiteDatabase; //SQLChipher暗号化用ライブラリ (256bit AES)
import net.sqlcipher.database.SQLiteException;
import net.sqlcipher.database.SQLiteOpenHelper; //SQLChipher暗号化用ライブラリ (256bit AES)


public class SQL {


    private SharedPreferences mPref;
    private SharedPreferences.Editor editor;
    private static final String DB_NAME = "AccountDB.db";
    private static final String DB_TABLE = "tabledb";
    private static final String DB_LABEL_TABLE = "labeldb";
    private static final String DB_PIN_TABLE = "pintabledb";
    private static final String DB_WALLPAPER_TABLE = "wallpapertabledb";
    private static final int DB_VERSION = 5;
    public static final String _ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_ACCOUNT = "account";
    public static final String COL_MAIL = "mail";
    public static final String COL_PASSWORD = "password";
    public static final String COL_URL = "url";
    public static final String COL_MEMO = "memo";
    public static final String COL_IMAGE = "image";
    public static final String COL_COLOR = "color";

    //見出し
    public static final String _ID_HEADING = "_id";
    public static final String COL_ACCOUNT_HEADING = "account_heading";
    public static final String COL_MAIL_HEADING = "mail_heading";
    public static final String COL_PASSWORD_HEADING = "password_heading";
    public static final String COL_URL_HEADING = "url_heading";
    public static final String COL_MEMO_HEADING = "memo_heading";
    public static final String COL_ACCOUNT_VISIBLE = "account_visible";
    public static final String COL_MAIL_VISIBLE = "mail_visible";
    public static final String COL_PASSWORD_VISIBLE = "password_visible";
    public static final String COL_URL_VISIBLE = "url_visible";
    public static final String COL_MEMO_VISIBLE = "memo_visible";


    public static final String COL_PIN = "pin";

    public static final String COL_FILTER = "filter";

    private SQLiteDatabase db = null;
    private DBHelper dbHelper;
    protected Context context;

    public SQL(Context context) {
        this.context = context;
        dbHelper = new DBHelper(context);
    }

     public SQL openData() {
        SQLiteDatabase.loadLibs(context);
        try {
            db = dbHelper.getWritableDatabase("Encryption");
        } catch (SQLiteException e) {
            context.deleteDatabase(DB_NAME);
            mPref = PreferenceManager.getDefaultSharedPreferences(context);
            editor = mPref.edit();
            editor.remove("FirstBoot");
            editor.apply();
            db = dbHelper.getWritableDatabase("Encryption");
            Toast.makeText(context, "データベースが壊れているか、読み込めないため初期化しました", Toast.LENGTH_LONG).show();
        }
        return this;
    }


    public SQL readData() {
        SQLiteDatabase.loadLibs(context);
        db = dbHelper.getReadableDatabase("Encryption");
        return this;
    }


    public void closeData() {
        db.close();
        db = null;
    }

    public void saveData(String title,
                         String account,
                         String mail,
                         String password,
                         String url,
                         String memo,
                         String image,
                         int color) {

        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put(COL_TITLE, title);
            values.put(COL_ACCOUNT, account);
            values.put(COL_MAIL, mail);
            values.put(COL_PASSWORD, password);
            values.put(COL_URL, url);
            values.put(COL_MEMO, memo);
            values.put(COL_IMAGE, image);
            values.put(COL_COLOR, color);

            db.insert(DB_TABLE, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void saveLabel(String AccountLabel,
                          int AccountVisible,
                          String MailLabel,
                          int MailVisible,
                          String PassLabel,
                          int PassVisible,
                          String UrlLabel,
                          int UrlVisible,
                          String MemoLabel,
                          int MemoVisible) {
        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            //values.put(COL_TITLE_HEADING, TitleLabel);
            values.put(COL_ACCOUNT_HEADING, AccountLabel);
            values.put(COL_ACCOUNT_VISIBLE, AccountVisible);
            values.put(COL_MAIL_HEADING, MailLabel);
            values.put(COL_MAIL_VISIBLE, MailVisible);
            values.put(COL_PASSWORD_HEADING, PassLabel);
            values.put(COL_PASSWORD_VISIBLE, PassVisible);
            values.put(COL_URL_HEADING, UrlLabel);
            values.put(COL_URL_VISIBLE, UrlVisible);
            values.put(COL_MEMO_HEADING, MemoLabel);
            values.put(COL_MEMO_VISIBLE, MemoVisible);
            db.insert(DB_LABEL_TABLE, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void changeLabel(String AccountLabel,
                            int AccountVisible,
                            String MailLabel,
                            int MailVisible,
                            String PassLabel,
                            int PassVisible,
                            String UrlLabel,
                            int UrlVisible,
                            String MemoLabel,
                            int MemoVisible,
                            String id) {
        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            //values.put(COL_TITLE_HEADING, TitleLabel);
            values.put(COL_ACCOUNT_HEADING, AccountLabel);
            values.put(COL_ACCOUNT_VISIBLE, AccountVisible);
            values.put(COL_MAIL_HEADING, MailLabel);
            values.put(COL_MAIL_VISIBLE, MailVisible);
            values.put(COL_PASSWORD_HEADING, PassLabel);
            values.put(COL_PASSWORD_VISIBLE, PassVisible);
            values.put(COL_URL_HEADING, UrlLabel);
            values.put(COL_URL_VISIBLE, UrlVisible);
            values.put(COL_MEMO_HEADING, MemoLabel);
            values.put(COL_MEMO_VISIBLE, MemoVisible);
            db.update(DB_LABEL_TABLE, values, _ID + "=?", new String[]{id});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void changeData(String title,
                           String account,
                           String mail,
                           String pass,
                           String url,
                           String memo,
                           int color,
                           String image,
                           String id) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_TITLE, title);
            values.put(COL_ACCOUNT, account);
            values.put(COL_MAIL, mail);
            values.put(COL_PASSWORD, pass);
            values.put(COL_URL, url);
            values.put(COL_MEMO, memo);
            values.put(COL_COLOR, color);
            values.put(COL_IMAGE,image);

            db.update(DB_TABLE, values, _ID + "=?", new String[]{id});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

    }

    public void changeTitle(String title, String changeTitle) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_TITLE, changeTitle);
            db.update(DB_TABLE, values, COL_TITLE + "=?", new String[]{title});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void changeHeader(String title, String image, String changeHeader) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_IMAGE, changeHeader);
            db.update(DB_TABLE, values, "title = ? AND image = ?", new String[]{title, image});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void HeaderEdit(String image, String id) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_IMAGE, image);
            db.update(DB_TABLE, values, _ID + "=?", new String[]{id});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

    }

    public void setColor(String id, int color) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_COLOR, color);
            db.update(DB_TABLE, values, _ID + "=?", new String[]{id});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteData(String title) {

        db.beginTransaction();
        try {
            db.delete(DB_TABLE, "title =?", new String[]{title});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void allDelete() {

        db.beginTransaction();                      // トランザクション開始
        try {
            // deleteメソッド DBのレコードを削除
            // 第1引数：テーブル名
            // 第2引数：削除する条件式 nullの場合は全レコードを削除
            // 第3引数：第2引数で?を使用した場合に使用
            db.delete(DB_TABLE, null, null);// DBのレコードを全削除
            db.delete(DB_LABEL_TABLE, null, null);
            db.setTransactionSuccessful();          // トランザクションへコミット
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();                    // トランザクションの終了
        }
    }

    //データベース検索　引数1:取得するカラム名 引数2:選択条件に使うカラム名 引数3:検索ワード
    public Cursor searchTitle(String title) {
        return db.query(DB_TABLE,
                null,
                COL_TITLE + "=?",
                new String[]{title},
                null,
                null,
                null);
    }


    //データベース検索　引数1:取得するカラム名 引数2:選択条件に使うカラム名 引数3:検索ワード

    /*
    public Cursor searchData(String[] columns, String column, String[] name) {
        return db.query(DB_TABLE, columns, column + "=?", name, null, null, null);
    }

     */

    public Cursor searchInfo(String[] columns, String ti, String ac) {
        return db.query(DB_TABLE,
                columns,
                "title =? AND account = ?",
                new String[]{ti, ac},
                null,
                null,
                null);
    }

    public Cursor getData() {
        return db.query(DB_TABLE,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    public Cursor getData_distinct(Boolean distinct, String columns) {
        return db.query(distinct,
                DB_TABLE,
                new String[]{columns},
                null,
                null,
                null,
                null,
                null,
                null);
    }

    public Cursor getData_sort(Boolean distinct, String columns, String order_by) {
        return db.query(distinct,
                DB_TABLE,
                new String[]{columns},
                null,
                null,
                null,
                null,
                order_by,
                null);
    }

    public Cursor searchData(Boolean distinct, String columns, String param, String order_by) {
        return db.query(distinct,
                DB_TABLE,
                new String[]{columns},
                columns + " like ?",
                new String[]{"%" + param + "%"},
                null,
                null,
                order_by,
                null);
    }

    public Cursor getIds(String ti) {
        return db.query(DB_TABLE,
                null,
                "title =?",
                new String[]{ti},
                null,
                null,
                null,
                null);
    }

    public Cursor getIds_sort(String ti, String order_by) {
        return db.query(DB_TABLE,
                null,
                "title =?",
                new String[]{ti},
                null,
                null,
                order_by,
                null);
    }

    public Cursor getAccount(String ti, String order_by, String param) {
        return db.query(DB_TABLE,
                null,
                "title =? AND account like ?",
                new String[]{ti, "%" + param + "%"},
                null,
                null,
                order_by,
                null);
    }


    public Cursor ShowInfo(String id) {
        return db.query(DB_TABLE,
                null,
                "_id =?",
                new String[]{id},
                null,
                null,
                null,
                null);
    }

    public Cursor ShowLabelInfo(String id) {
        return db.query(DB_LABEL_TABLE,
                null,
                "_id =?",
                new String[]{id},
                null,
                null,
                null,
                null);
    }

    public Cursor getLastID() {
        return db.query(DB_TABLE,
                null,
                null,
                null,
                null,
                null,
                _ID + " DESC",
                "1");
    }


    public void deleteAccount(String id) {
        db.beginTransaction();
        try {
            db.delete(DB_TABLE, "_id =?", new String[]{id});
            db.delete(DB_LABEL_TABLE, "_id =?", new String[]{id});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void saveWallpaper(String wallpaper, int filter, String select, boolean insert) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            if (select.equals("WALLPAPER") || select.equals("ALL")) {
                //values.put(COL_WALLPAPER, wallpaper);
            }
            if (select.equals("FILTER") || select.equals("ALL")) {
                values.put(COL_FILTER, filter);
            }
            if (insert) {
                db.insert(DB_WALLPAPER_TABLE, null, values);
            } else {
                db.update(DB_WALLPAPER_TABLE, values, null, null);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }


    public Cursor getWallpaper() {
        return db.query(DB_WALLPAPER_TABLE,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    public void savePin(String pin, boolean insert) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_PIN, pin);
            if (insert) {
                db.insert(DB_PIN_TABLE, null, values);
            } else {
                db.update(DB_PIN_TABLE, values, null, null);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public Cursor getPin() {
        return db.query(DB_PIN_TABLE,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }


    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL("CREATE TABLE " + DB_TABLE + " ("
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COL_TITLE + " TEXT,"
                    + COL_ACCOUNT + " TEXT,"
                    + COL_MAIL + " TEXT,"
                    + COL_PASSWORD + " TEXT,"
                    + COL_URL + " TEXT,"
                    + COL_MEMO + " TEXT,"
                    + COL_IMAGE + " TEXT,"
                    + COL_COLOR + " TEXT"
                    + ");");

            db.execSQL("CREATE TABLE " + DB_LABEL_TABLE + " ("
                    + _ID_HEADING + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COL_ACCOUNT_HEADING + " TEXT,"
                    + COL_MAIL_HEADING + " TEXT,"
                    + COL_PASSWORD_HEADING + " TEXT,"
                    + COL_URL_HEADING + " TEXT,"
                    + COL_MEMO_HEADING + " TEXT,"
                    + COL_ACCOUNT_VISIBLE + " INTEGER,"
                    + COL_MAIL_VISIBLE + " INTEGER,"
                    + COL_PASSWORD_VISIBLE + " INTEGER,"
                    + COL_URL_VISIBLE + " INTEGER,"
                    + COL_MEMO_VISIBLE + " INTEGER"
                    + ");");

            db.execSQL("CREATE TABLE " + DB_PIN_TABLE + " ("
                    + COL_PIN + " TEXT"
                    + ");");

            db.execSQL("CREATE TABLE " + DB_WALLPAPER_TABLE + " ("
                    + COL_FILTER + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion <= 4 && newVersion >= 5) {
                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
                db.execSQL("DROP TABLE IF EXISTS " + DB_LABEL_TABLE);
                db.execSQL("DROP TABLE IF EXISTS " + DB_PIN_TABLE);
                db.execSQL("DROP TABLE IF EXISTS " + DB_WALLPAPER_TABLE);
                onCreate(db);
            }
        }

    }

}