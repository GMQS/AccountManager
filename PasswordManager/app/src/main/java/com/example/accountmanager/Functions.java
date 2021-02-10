package com.example.accountmanager;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;

import com.oginotihiro.cropview.CropView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import DataBase.GetFilter;
import DataBase.SQL;

import static android.content.Context.KEYGUARD_SERVICE;

public class Functions {

    /**
     * @param context   コンテキスト
     * @param wallpaper 壁紙をセットするImageView ImageView型
     * @throws FileNotFoundException
     */

    public static int setWallpaper(final Context context, final ImageView wallpaper) {

        File file = new File(context.getFilesDir().getPath() + "/wallpaper.jpg");
        if (file.exists()) {
            BufferedInputStream bis = null;
            try {
                bis = new BufferedInputStream(context.openFileInput("wallpaper.jpg"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //ビットマップ画像(背景用)の定義
            Bitmap bmp = BitmapFactory.decodeStream(bis);
            wallpaper.setImageBitmap(bmp);
        } else {
            Resources r = context.getResources();
            Bitmap defaultBmp = BitmapFactory.decodeResource(r, R.drawable.header_default);
            wallpaper.setImageBitmap(defaultBmp);
        }

        int filterValue = new GetFilter(context).getValue();
        wallpaper.setColorFilter(Color.argb(filterValue, 0, 0, 0));
        return filterValue;
    }

    public static void setTmpWallpaper(Context context, ImageView wallpaper, int filterValue) throws FileNotFoundException {

        File file = new File(context.getFilesDir().getPath() + "/tmp_image.jpg");
        if (file.exists()) {
            BufferedInputStream bis = new BufferedInputStream(context.openFileInput("tmp_image.jpg"));
            //ビットマップ画像(背景用)の定義
            Bitmap bmp = BitmapFactory.decodeStream(bis);
            wallpaper.setImageBitmap(bmp);
        }
        wallpaper.setColorFilter(Color.argb(filterValue, 0, 0, 0));
    }


    /**
     * @param target 検査する文字列
     * @return バリデーション結果 true = 正常,false = 不正
     */
    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    /**
     * @param context コンテキスト
     * @param text    クリップボードにコピーする文字列
     */
    public static void clipBoard(Context context, String text) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (null == clipboardManager) {
            return;
        }
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", text));
    }

    /**
     * @param cnt   生成する文字の長さ
     * @param chars ランダム化する元の文字群
     * @return 生成したランダム文字列
     */
    public static String getRandomString(int cnt, String chars) {

        Random rnd = new Random();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < cnt; i++) {
            int val = rnd.nextInt(chars.length());
            buf.append(chars.charAt(val));
        }
        return buf.toString();
    }

    /**
     * @param uri             画像ファイルのURI情報
     * @param reqWidth        変換したい横幅
     * @param reqHeight       変換したい縦幅
     * @param contentResolver コンテンツリザーバー
     * @return 変換後の画像ファイル
     * @throws IOException
     */

    public static Bitmap decodeSampledBitmapFromFileDescriptor(Uri uri, int reqWidth, int reqHeight, ContentResolver contentResolver) throws IOException {

        ParcelFileDescriptor parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);


        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }

    /**
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * @param cropView クロップライブラリのビュー
     * @param context  　openFileOutputメソッドを使用するためにContextを使用
     */
    public static void tmpImageSave(CropView cropView, Context context) {
        Bitmap bmp;
        bmp = cropView.getOutput();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            FileOutputStream fos = context.openFileOutput("tmp_image.jpg", Context.MODE_PRIVATE);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            fos.write(baos.toByteArray());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Soft input area be shown to the user.
     *
     * @param context The context to use
     * @param view    The currently focused view, which would like to receive soft keyboard input
     * @return Result
     */
    public static boolean showSoftInput(Context context, View view) {
        return showSoftInput(context, view, 0);
    }

    /**
     * Soft input area be shown to the user.
     *
     * @param context The context to use
     * @param view    The currently focused view, which would like to receive soft keyboard input
     * @param flags   Provides additional operating flags
     * @return Result
     */
    public static boolean showSoftInput(Context context, View view, int flags) {
        InputMethodManager imm =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null == imm) {
            return false;
        }
        return imm.showSoftInput(view, flags);
    }


    public static void brokenFilesDelete(String getFilesPath, SQL dbAdapter) {
        File file = new File(getFilesPath + "/");
        File[] files = file.listFiles();
        ArrayList<String> DBFiles = new ArrayList<>();
        Cursor c = dbAdapter.getData();
        if (c.moveToFirst()) {
            do {
                DBFiles.add(c.getString(7));
            } while (c.moveToNext());
        }
        c.close();

        //cast
        String[] array = DBFiles.toArray(new String[0]);

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (!Arrays.asList(array).contains(files[i].getName())
                        && !files[i].getName().equals("wallpaper.jpg")
                        && !files[i].getName().contains("_header.jpg")) {
                    files[i].delete();
                }
            }
        }
    }

    public static String checkBiometricSupport(Context context) {

        KeyguardManager keyguardManager =
                (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);

        PackageManager packageManager = context.getPackageManager();
        String errorInfo = "使用可能";

        //指紋認証センサーが搭載されていない
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            errorInfo = "端末のアンドロイドバージョンが生体認証をサポートしていません。";
            return errorInfo;

        }
        //アンドロイドのバージョンが指紋認証をサポートしていない
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            errorInfo = "端末に生体認証機能が搭載されていないため利用できません。";
            return errorInfo;
        }
        //セキュリティ設定をしていない
        if (!keyguardManager.isKeyguardSecure()) {
            errorInfo = "生体認証が利用できません。端末のセキュリティ設定を確認してください。";
            return errorInfo;
        }
        //指紋認証ログインを許可していない
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.USE_BIOMETRIC) !=
                PackageManager.PERMISSION_GRANTED) {
            errorInfo = "生体認証ログインが許可されていません。アプリケーションの設定を確認してください。";
            return errorInfo;
        }
        return errorInfo;

    }


}
