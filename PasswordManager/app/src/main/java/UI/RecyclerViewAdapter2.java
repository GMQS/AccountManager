package UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accountmanager.R;

public class RecyclerViewAdapter2 extends RecyclerView.Adapter<RecyclerViewAdapter2.multiLineViewHolder>{
    //メンバ変数
    ArrayList<String> HeaderDatas; //リストのデータを保持する変数
    ArrayList<String> MailDatas;
    Context context;


    //コンストラクタ
    protected RecyclerViewAdapter2(ArrayList<String> itemDatas, ArrayList<String> MailDatas, Context context) {
        //データをコンストラクタで受け取りメンバ変数に格納
        this.HeaderDatas = itemDatas;
        this.MailDatas = MailDatas;
        this.context = context;
    }

    @NonNull
    @Override
    public multiLineViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //onCreateViewHolder()ではリスト一行分のレイアウトViewを生成する
        //作成したViewはViewHolderに格納してViewHolderをreturnで返す

        //レイアウトXMLからViewを生成
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.listitem_recyclerview2, viewGroup, false);
        //ViewHolderを生成してreturn
        final multiLineViewHolder holder = new multiLineViewHolder(view);

        //クリックイベントを登録
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                onItemClick(view,position,HeaderDatas.get(position),MailDatas.get(position));
            }
        });

        //ホールドイベント
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position = holder.getAdapterPosition();
                onItemLongClick(view,position,HeaderDatas.get(position),MailDatas.get(position));
                return true;
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull multiLineViewHolder MultiLineViewHolder, int position) {
        //onBindViewHolder()ではデータとレイアウトの紐づけを行なう

        SharedPreferences mPref;
        mPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        int theme = mPref.getInt("accent", R.style.NoActionBar);

        switch (theme) {
            case R.style.NoActionBar:
                MultiLineViewHolder.text02.setTextColor(0xFFFF60A8);
                break;

            case R.style.NoActionBarCyan:
                MultiLineViewHolder.text02.setTextColor(0xFF00B8D4);
                break;

            case R.style.NoActionBarOrange:
                MultiLineViewHolder.text02.setTextColor(0xFFFF6D00);
                break;

            case R.style.NoActionBarGreen:
                MultiLineViewHolder.text02.setTextColor(0xFF64DD17);
                break;
        }

        MultiLineViewHolder.text01.setText(HeaderDatas.get(position));
        MultiLineViewHolder.text02.setText(MailDatas.get(position));

    }

    @Override
    public int getItemCount() {
        //データ個数を返す
        return HeaderDatas.size();
    }

    public void onItemClick(View view,int position,String itemData,String mailData){
    }

    public void onItemLongClick(View view,int position,String itemData,String mailData){

    }

    /* ViewHolder（インナークラス） */
    class multiLineViewHolder extends RecyclerView.ViewHolder {
        TextView text01;
        TextView text02;

        multiLineViewHolder(@NonNull View itemView) {
            super(itemView);
            text01 = itemView.findViewById(R.id.text01);
            text02 = itemView.findViewById(R.id.text02);
        }
    }

}
