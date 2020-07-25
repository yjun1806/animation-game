package com.test.animation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class Total_rank_adapter extends RecyclerView.Adapter<Total_rank_adapter.ViewHolder> {


    private final ArrayList<Record_game> rank_list;
    public Context context;

    public Total_rank_adapter(ArrayList<Record_game> rank_list, Context context) {
        this.rank_list = rank_list;
        this.context = context;
    }

    @NonNull
    @Override
    public Total_rank_adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rank_line, parent, false);
        int height = parent.getMeasuredHeight()/10;
        int width = parent.getMeasuredWidth();
        view.setLayoutParams(new RecyclerView.LayoutParams(width, height));


        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Total_rank_adapter.ViewHolder holder, int position) {
        ViewHolder myView = holder;

        if(position == 0){
            myView.rank_number.setBackgroundResource(R.drawable.goldstar);
            myView.rank_name.setTextColor(Color.parseColor("#FFFFD21F"));
            myView.rank_name.setTextSize(36f);
            myView.rank_name.setTypeface(null, Typeface.BOLD);
        }else if(position == 1){
            myView.rank_number.setBackgroundResource(R.drawable.sliverstar);
            myView.rank_name.setTextColor(Color.parseColor("#FF969696"));
            myView.rank_name.setTextSize(28f);
            myView.rank_name.setTypeface(null, Typeface.BOLD);
        }else if(position == 2){
            myView.rank_number.setBackgroundResource(R.drawable.coperstar);
            myView.rank_name.setTextColor(Color.parseColor("#FFBD6739"));
            myView.rank_name.setTextSize(20f);
            myView.rank_name.setTypeface(null, Typeface.BOLD);
        }
        myView.rank_number.setText(String.valueOf(position+1));
        myView.rank_name.setText(rank_list.get(position).name);
        myView.rank_score.setText(rank_list.get(position).time);

        /*LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(context, R.anim.rank_up);

        holder.line.setLayoutAnimation(animationController);*/

        //holder.line.animate().translationY(1000).setDuration(100).scaleX(0).start();
        /*holder.line.animate().setStartDelay(100*position).setDuration(500)
                .translationYBy(300).translationY(0).alphaBy(1).scaleX(1);*/
        //holder.line.startAnimation(animation);


    }

    @Override
    public int getItemCount() {
        return rank_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView rank_number, rank_name, rank_score;
        LinearLayout line;


        public ViewHolder(View itemView) {
            super(itemView);
            rank_number = itemView.findViewById(R.id.rk_number);
            rank_name = itemView.findViewById(R.id.rk_name);
            rank_score = itemView.findViewById(R.id.rk_score);
            line = itemView.findViewById(R.id.rk_line);
        }
    }
}
