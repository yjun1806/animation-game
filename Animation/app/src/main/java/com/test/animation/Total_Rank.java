package com.test.animation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Total_Rank extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<Record_game> rank_list = new ArrayList<>();
    Total_rank_adapter rank_adapter;


    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private int game_code=0;
    int Max_game_number = 4;


    String[] game_name = {"Oneto25", "Oneto50", "Eat_Food", "Nova_run"};
    TextView game_name_tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_total__rank);
        rank_adapter = new Total_rank_adapter(rank_list, this);
        game_name_tv = findViewById(R.id.rk_game_name);

        set_RecyclerView();
        get_database(game_code);


        ImageButton next_bt, pre_bt;
        next_bt = findViewById(R.id.rk_next);
        pre_bt = findViewById(R.id.rk_pre);

        next_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game_code++;
                if(game_code == Max_game_number){
                    game_code = 0;
                }
                rank_list.clear();
                get_database(game_code);

            }
        });

        pre_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game_code--;
                if(game_code == -1){
                    game_code = Max_game_number-1;
                }
                rank_list.clear();
                get_database(game_code);

            }
        });

        /*Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);

                    handler.sendEmptyMessage(0);
                    Log.i("데이터베이스 핸들러", " 크기 : " + rank_list.size());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();*/

    }

  /*  @SuppressLint("HandlerLeak") Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            runAnimation(recyclerView);
        }
    };*/

    private void runAnimation(RecyclerView recyclerView) {
        Context context = recyclerView.getContext();
        LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(context, R.anim.sample_layout);

        recyclerView.setAdapter(rank_adapter);
        recyclerView.setLayoutAnimation(animationController);

        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }


    private void get_database(int game_code) {
        game_name_tv.setText(game_name[game_code]);
        databaseReference.child("Game").child(game_name[game_code]).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Record_game tmp = dataSnapshot.getValue(Record_game.class);
                rank_list.add(tmp);
               // Log.i("데이터베이스", " 크기 : " + rank_list.size());
                //rank_adapter.notifyDataSetChanged();
                runAnimation(recyclerView);


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void set_RecyclerView() {

        recyclerView = findViewById(R.id.rk_recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(rank_adapter);
    }
}
