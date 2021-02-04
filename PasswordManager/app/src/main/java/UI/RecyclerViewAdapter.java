package UI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accountmanager.R;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.SampleViewHolder> {
    //メンバ変数
    ArrayList<String> itemDatas; //リストのデータを保持する変数


    //コンストラクタ
    protected RecyclerViewAdapter(ArrayList<String> itemDatas) {
        //データをコンストラクタで受け取りメンバ変数に格納
        this.itemDatas = itemDatas;
    }

    @NonNull
    @Override
    public SampleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //onCreateViewHolder()ではリスト一行分のレイアウトViewを生成する
        //作成したViewはViewHolderに格納してViewHolderをreturnで返す

        //レイアウトXMLからViewを生成
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.listitem_recyclerview1, viewGroup, false);
        //ViewHolderを生成してreturn
        final SampleViewHolder holder = new SampleViewHolder(view);

        //クリックイベントを登録
        holder.itemView.setOnClickListener(view12 -> {
            int position = holder.getAdapterPosition();
            onItemClick(view12, position, itemDatas.get(position));
        });

        //ホールドイベント
        holder.itemView.setOnLongClickListener(view1 -> {
            int position = holder.getAdapterPosition();
            onItemLongClick(view1, position, itemDatas.get(position));
            return true;
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final SampleViewHolder sampleViewHolder, final int position) {
        //onBindViewHolder()ではデータとレイアウトの紐づけを行なう
        sampleViewHolder.text01.setText(itemDatas.get(position));
    }


    @Override
    public int getItemCount() {
        //データ個数を返す
        return itemDatas.size();
    }

    public void onItemClick(View view, int position, String itemData) {
    }

    public void onItemLongClick(View view, int position, String itemData) {
    }

    /* ViewHolder（インナークラス） */
    static class SampleViewHolder extends RecyclerView.ViewHolder {
        TextView text01;

        SampleViewHolder(@NonNull View itemView) {
            super(itemView);
            text01 = itemView.findViewById(R.id.text01);
        }
    }

}