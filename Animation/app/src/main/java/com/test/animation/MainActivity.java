package com.test.animation;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    Button button;
    ConstraintLayout con;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.oneto25);
        Button button2 = findViewById(R.id.oneto50);
        Button button3 = findViewById(R.id.game2_bt);
        Button button4 = findViewById(R.id.game3_bt);
        con = findViewById(R.id.con);




        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), One_to_25.class);
                intent.putExtra("gamecode", 0);
                startActivity(intent);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), One_to_25.class);
                intent.putExtra("gamecode", 1);
                startActivity(intent);
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), game2.class);
                intent.putExtra("gamecode", 2);
                startActivity(intent);
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Game3.class);
                intent.putExtra("gamecode", 3);
                startActivity(intent);
            }
        });
    }

    public void onClick_record_viewing(View view) {

        Intent intent = new Intent(getApplicationContext(), Total_Rank.class);
        startActivity(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        LinearLayout line = findViewById(R.id.m_line);

        LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(getApplicationContext(), R.anim.sample_layout);
        line.setLayoutAnimation(animationController);
    }
}
