package com.example.junmung.hangangparksmap.Culture;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.example.junmung.hangangparksmap.R;

import java.util.List;

public class CultureItemAdapter extends RecyclerView.Adapter<CultureItemAdapter.ViewHolder> {
    private List<CultureItem> cultureItems;
    private Context context;
    private int lastPosition = -1;


    public CultureItemAdapter(List<CultureItem> filterItems) {
        this.cultureItems = filterItems;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final CultureItem cultureItem = cultureItems.get(position);

//        String date = new SimpleDateFormat("MM월 dd일").format(memoItem.getDate());

        holder.title.setText(cultureItem.getTemp());

        Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);

        holder.itemView.setAnimation(animation);
        holder.itemView.startAnimation(animation);
        lastPosition = position;
    }




    @Override
    public CultureItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        // 새로운 뷰 만들기
        View view = LayoutInflater.from(context).inflate(R.layout.item_culture, parent, false);

        // 뷰사이즈 세팅, 마진, 패딩 등등 세팅하는 곳
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }



    @Override
    public int getItemCount() {
        return cultureItems.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // 뷰홀더
    class ViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener {
        TextView title;

        public ViewHolder(View itemView){
            super(itemView);
            itemView.setOnClickListener(this);
            title = itemView.findViewById(R.id.item_culture_textView);
        }


        @Override
        public void onClick(View v) {
            Toast.makeText(context, "눌림", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(context, MemoOpenActivity.class);
//            intent.putExtra("MemoTitle", title.getText());
//            context.startActivity(intent);
        }
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }
}
