package File.IO;

import android.content.Context;

import java.io.File;

/**
 * Fileオブジェクトを生成する抽象クラス
 */

public abstract class CreateFile {
    Context context;

    public CreateFile(Context context) {
        this.context = context;
    }

    public abstract File create();
}
