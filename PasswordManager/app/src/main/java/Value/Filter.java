package Value;


/**
 * 背景フィルタのアルファ値を格納するためのラッパークラス 範囲 :0~255
 */
public class Filter {
    private int value;

    public Filter(int value) {
        this.value = value;
        if (0 > value || value > 255) {
            throw new IllegalArgumentException("0-255の範囲外です");
        }
    }

    public int getFilter(){
        return this.value;
    }
}
