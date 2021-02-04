package DropBox;

import android.content.Context;
import android.os.FileUtils;
import android.os.Looper;
import android.util.Log;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxUploader;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.dropbox.core.v2.files.DownloadBuilder;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.RelocationErrorException;
import com.dropbox.core.v2.files.SearchMatch;
import com.dropbox.core.v2.files.SearchResult;
import com.dropbox.core.v2.files.UploadBuilder;
import com.dropbox.core.v2.files.UploadUploader;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class DropboxManager {
    private Context mContext;
    private DbxClientV2 mClient;

    private final static String DROP_BOX_KEY = "mb56hamy5tjecjd";

    /**
     * コンストラクタ。
     *
     * @param context コンテキスト
     */
    public DropboxManager(Context context) {
        mContext = context;
    }

    /**
     * コンストラクタ。認証済みの場合はこちらを使用する。
     *
     * @param context コンテキスト
     * @param token   認証済みトークン
     */
    public DropboxManager(Context context, String token) {
        mContext = context;
        DbxRequestConfig config = new DbxRequestConfig("Name/Version");
        mClient = new DbxClientV2(config, token);
    }

    /**
     * 認証処理を開始する。
     * 認証ページが開く。
     */
    public void startAuthenticate() {
        Auth.startOAuth2Authentication(mContext, DROP_BOX_KEY);
    }

    /**
     * 認証トークンを取得する。
     *
     * @return 認証トークン。
     */
    public String getAccessToken() {
        return Auth.getOAuth2Token();
    }

    /**
     * ファイルをバックアップする。
     *
     * @param srcFilePath 保存元のファイルパス(Android)
     * @param dstFilePath 保存先のファイルパス(Dropbox)
     * @return 成功した場合はtrue, 失敗した場合はfalseを返す
     */
    public boolean backup(String srcFilePath, String dstFilePath) {
        try {
            File file = new File(srcFilePath);
            if (file.exists()) {
                InputStream input = new FileInputStream(file);
                // Dropboxのファイルパスは先頭に"/"と指定する必要があるため、ファイルパスの先頭に"/"をつける
                mClient.files().uploadBuilder("/" + dstFilePath)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(input);
            }

        } catch (Exception e) {
            Log.e("tag", "Upload Error: " + e);
            return false;
        }
        return true;
    }


    /**
     * ファイルを削除する。
     *
     * @param deleteFilePath 削除対象のファイルパス(Dropbox側)
     * @return 成功した場合はtrue, 失敗した場合はfalseを返す
     */
    public void delete(String deleteFilePath) {
        try {
            //byte[] bytes = convertFileToBytes(file);
            // Dropboxのファイルパスは先頭に"/"と指定する必要があるため、ファイルパスの先頭に"/"をつける
            mClient.files().deleteV2("/" + deleteFilePath);
        } catch (Exception e) {
            Log.e("tag", "Delete Error: " + e);
        }
    }

    /**
     * ファイルを削除する。
     *
     * @return 成功した場合はtrue, 失敗した場合はfalseを返す
     */
    public void DirDelete() {
        try {
            // Dropboxのファイルパスは先頭に"/"と指定する必要があるため、ファイルパスの先頭に"/"をつける
            mClient.files().deleteV2("/tmp");
        } catch (Exception e) {
            Log.e("tag", "Delete Error: " + e);
        }
    }

    public boolean moveFiles() {

        try {
            ListFolderResult tmpResult = mClient.files().listFolder("/tmp");
            List<Metadata> list = tmpResult.getEntries();
            ListFolderResult rootResult = mClient.files().listFolder("");
            List<Metadata> rootList = rootResult.getEntries();
            for (int i = 0; i < rootList.size(); i++) {
                if (!rootList.get(i).getPathLower().contains("tmp") && list.toString().contains(rootList.get(i).getName())){
                    mClient.files().deleteV2(rootList.get(i).getPathLower());
                }
            }
            for (int i = 0; i < list.size(); i++) {
                mClient.files().moveV2Builder(list.get(i).getPathLower(), "/" + list.get(i).getName()).start();
            }
            mClient.files().deleteV2("/tmp");
        }catch (DbxException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * クラウド上のファイルとデバイスのファイルを比較して相違がある場合にファイルを削除するメソッド
     *
     * @return 成功 = true, 失敗 = false
     */

    public boolean SearchDelete(String internalFilePath) {
        try {
            // Java.io ディレクトリの中身を検索して配列に格納 File型
            File dir = new File(internalFilePath);
            if (dir.exists()) {
                String[] list_Internal = dir.list();
                //DropBox V2 API ディレクトリの中身を検索して配列に格納 ListFolderResult型
                ListFolderResult result = mClient.files().listFolder("");
                List<Metadata> list_DBX = result.getEntries();

                for (int i = 0; i < list_DBX.size(); i++) {
                    if (list_Internal != null)
                        if (!Arrays.asList(list_Internal).contains(list_DBX.get(i).getName()) && !list_DBX.get(i).getName().contains("AccountDB.db") && !list_DBX.get(i).getName().contains("tmp")) {
                            Log.d("サーチするデータ", list_DBX.get(i).getName());
                            mClient.files().deleteV2("/" + list_DBX.get(i).getName());
                        }
                }
            }
        } catch (Exception e) {
            Log.e("tag", "Delete Error: " + e);
            return false;
        }
        return true;
    }

    /*
    public boolean restore_() {
        ListFolderResult result;
        try {
            result = mClient.files().listFolder("");
        } catch (DbxException e) {
            e.printStackTrace();
            return false;
        }
        List<Metadata> list_DBX = result.getEntries();
        for (int i = 0; i < list_DBX.size(); i++) {
            try {
                File file = new File("/data/data/com.example.passwordmanager/files/" + list_DBX.get(i).getName());
                FileOutputStream fos = new FileOutputStream(file);
                mClient.files().download(list_DBX.get(i).getPathLower()).download(fos);
            } catch (DbxException | FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
     */

    public boolean restore(String internalFilePath, String internalDataBasePath) {
        ListFolderResult result;
        try {
            result = mClient.files().listFolder("");
        } catch (DbxException e) {
            e.printStackTrace();
            return false;
        }
        List<Metadata> list_DBX = result.getEntries();
        File tmpDir = new File(internalFilePath);
        tmpDir.mkdir();
        for (int i = 0; i < list_DBX.size(); i++) {
            try {
                //File file = new File(internalFilePath + list_DBX.get(i).getName() + ".tmp");
                File file = new File(internalFilePath + list_DBX.get(i).getName());

                FileOutputStream fos = new FileOutputStream(file);
                String fileName = list_DBX.get(i).getName();
                if (!fileName.equals("AccountDB.db")) {
                    mClient.files().download(list_DBX.get(i).getPathLower()).download(fos);
                    //DbxDownloader<FileMetadata> downloader = mClient.files().downloadBuilder(list_DBX.get(i).getPathLower()).start();
                    //downloader.download(fos);
                    Log.d("画像ファイル処理ブロック", fileName);

                } else {
                    File dbFile = new File(internalDataBasePath);
                    FileOutputStream fosdb = new FileOutputStream(dbFile);
                    String dbFileName = list_DBX.get(i).getName();
                    mClient.files().download(list_DBX.get(i).getPathLower()).download(fosdb);
                    Log.d("データベース処理ブロック", dbFileName);
                }
            } catch (DbxException | FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * ファイルを復元する。
     *
     * @param srcFilePath 復元元のファイルパス(Dropbox)
     * @param dstFilePath 復元先のファイルパス(Android)
     * @return 成功した場合はtrue, 失敗した場合はfalseを返す
     */

    /*
    public boolean restoreDB(String srcFilePath, String dstFilePath) {
        try {
            // ファイルを検索する
            SearchResult result = mClient.files().search("",srcFilePath);
            List<SearchMatch> matches = result.getMatches();

            Metadata metadata = null;
            for (SearchMatch match : matches) {
                metadata = match.getMetadata();
                break;
            }


            if (metadata == null) {
                // ファイルが見つからない場合
                Log.d("tag", "metadata not found");
                return false;
            } else {
                Log.d("tag", "metadata.getPathLower(): " + metadata.getPathLower());
            }


            // ダウンロードし、ファイルを置き換える
            File file = new File(dstFilePath);
            OutputStream os = new FileOutputStream(file);
            mClient.files().download(metadata.getPathLower()).download(os);

        } catch (Exception e) {
            Log.e("tag", "Download Error: " + e);
            return false;
        }
        return true;
    }

     */

    /**
     * Fileをbytes[]に変換する。
     *
     * @param file ファイル
     * @return bytes
     * @throws IOException
     */
    private byte[] convertFileToBytes(File file) throws IOException {
        final long fileSize = file.length();
        final int byteSize = (int) fileSize;
        byte[] bytes = new byte[byteSize];
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            try {
                raf.readFully(bytes);
            } finally {
                raf.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

}
