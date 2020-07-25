package com.test.animation;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Game3 extends AppCompatActivity {

    ImageView chr, cloud, bomb;
    AnimationDrawable ani, bomb_ani;
    ConstraintLayout background;
    boolean is_jumping = false;
    boolean is_game_start = false;

    int heart = 5; // 체력, 0이되면 게임오버
    ImageView[] heart_bar;

    int ba_wi;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    LinearLayout seal, heart_gage, record_window;

    Button start_bt, record_bt;

    ArrayList<Record_game> rank_list;

    TextView timer, record_time;
    private long myBaseTime;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game3);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //Layout initial
        chr = findViewById(R.id.g3_chr);
        background = findViewById(R.id.g3_background);
        cloud = findViewById(R.id.g3_cloud);
        bomb = findViewById(R.id.g3_bomb);
        heart_bar = new ImageView[5];
        heart_bar[0] = findViewById(R.id.g3_heart_01);
        heart_bar[1] = findViewById(R.id.g3_heart_02);
        heart_bar[2] = findViewById(R.id.g3_heart_03);
        heart_bar[3] = findViewById(R.id.g3_heart_04);
        heart_bar[4] = findViewById(R.id.g3_heart_05);
        seal = findViewById(R.id.g3_seal); // 레이아웃 봉인
        start_bt = findViewById(R.id.g3_start_button);
        heart_gage = findViewById(R.id.g3_heart_gage);
        timer = findViewById(R.id.g3_time);
        record_window = findViewById(R.id.g3_record);
        record_bt = findViewById(R.id.g3_record_button);
        record_time = findViewById(R.id.g3_endpoint);

        //게임 시작전에 다 숨겨버린다.

        heart_gage.setVisibility(View.INVISIBLE);
        chr.setVisibility(View.INVISIBLE);
        bomb.setVisibility(View.INVISIBLE);
        cloud.setVisibility(View.INVISIBLE);





        Log.e("시작전", "위치정보 - 캐릭터 : " + chr.getX() + " , " + chr.getY() + " / bomb : " + bomb.getX() + " , " + bomb.getY());


        Log.e("시작전", "위치세팅 - " +ba_wi + " 캐릭터 : " + chr.getX() + " , " + chr.getY() + " / bomb : " + bomb.getX() + " , " + bomb.getY());



        background.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(is_game_start) {
                    if (!is_jumping) {

                        game_handler.sendEmptyMessage(0);

                        Thread delay = new Thread(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    Thread.sleep(300);
                                    is_jumping = true;
                                    Thread.sleep(500);
                                    is_jumping = false;
                                    game_handler.sendEmptyMessage(1);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        delay.start();
                    }
                }
                return false;

            }
        });


    }

    private boolean crashing = false;
    private boolean motion_detect_onoff= true;
    private int game_time=0;
    Thread motion_detect = new Thread(new Runnable() {
        @Override
        public void run() {

            while (motion_detect_onoff) {
                try {
                    Thread.sleep(10);

                    //Log.i("움직임 탐지", "x : " + bomb.getX() + " y: " + bomb.getY() + " height : " + bomb.getHeight() + " width : " + bomb.getWidth());

                    if(!is_jumping && chr.getX() - 100 < bomb.getX() && bomb.getX() < chr.getX() + 100){
                        //Toast.makeText(Game3.this, "충돌!!", Toast.LENGTH_SHORT).show();
                        game_handler.sendEmptyMessage(5);
                        game_handler.sendEmptyMessage(7);
                        Log.d("충돌탐지", "충돌!!! - chr : " + chr.getX() + " bomb :" + bomb.getX());
                        //crashing = true;
                        Thread.sleep(500);
                        game_handler.sendEmptyMessage(4);


                    }

                    game_time++;
                    if(game_time == 500){
                        bombtime -= 2000;
                    }
                    if(game_time == 1000){
                        bombtime -= 2000;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        background = findViewById(R.id.g3_background);
        ba_wi = background.getWidth();
    }

    private long bombtime = 6000;
    @SuppressLint("HandlerLeak")
    Handler game_handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0: // 달리는 중에 점프입력이 들어왔을때
                    ani.stop(); // 달리는 모션을 정지시켜주고
                    chr.setImageResource(R.drawable.jump); // 점프모션을 세팅해준뒤
                    ani = (AnimationDrawable) chr.getDrawable();
                    ani.start(); // 점프를 시작시켜준다.
                    break;
                case 1: // 점프모션이 끝났을때
                    ani.stop();
                    chr.setImageResource(R.drawable.run); // 점프모션을 세팅해준뒤
                    ani = (AnimationDrawable) chr.getDrawable();
                    ani.start(); // 점프를 시작시켜준다.
                    break;
                case 2: // 구름의 이동을 다루는 부분
                    cloud.animate().x(-500)
                            .setDuration(8000).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {

                            cloud.setX(background.getWidth());
                            game_handler.sendEmptyMessage(2);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    }).start();
                    break;
                case 3: // 폭탄의 이동을 다루는 부분
                    bomb.animate().x(-300)
                            .setDuration(bombtime).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            Log.d("폭탄", "이동시킨다");
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            Log.d("폭탄", "이동 끝 다시 원위치");

                            bomb.setX(background.getWidth());
                            game_handler.sendEmptyMessage(3);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            Log.d("폭탄", "애니메이션 취소됨");
                            //helper.sendEmptyMessage(0);
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    }).start();
                    break;
                case 4: // 하트감소 핸들러
                    if(heart != 0) {
                        heart_bar[heart - 1].setImageResource(R.drawable.heart_02);
                        heart--;
                        if(heart == 0){
                            Toast.makeText(getApplicationContext(), "죽었습니다.", Toast.LENGTH_SHORT).show();
                            Timer.removeMessages(0);
                            game_over();
                        }

                    }
                    break;
                case 5: // 폭탄 폭발
                    bomb.clearAnimation();
                    bomb_ani.stop();
                    bomb.setImageResource(R.drawable.explosing);
                    bomb_ani = (AnimationDrawable) bomb.getDrawable();
                    bomb_ani.start();
                    Thread exploed = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                Thread.sleep((bomb_ani.getNumberOfFrames()-1)*100);
                                Log.d("충돌후", "폭발효과가 끝났다");
                                helper.sendEmptyMessage(0);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    exploed.start();
                    //game_handler.sendEmptyMessage(6);

                    break;
                case 6: // 폭탄 폭발 후 재생성


                    break;
                case 7: // 캐릭터 깜박임
                    Animation blink = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
                    chr.startAnimation(blink);
                    break;
            }
        }
    };

    private void game_over() {
        motion_detect_onoff = false;
        motion_detect.interrupt();

        ani.stop();
        bomb_ani.stop();

        bomb.clearAnimation();
        bomb.animate().setListener(null);
        bomb.animate().cancel();

        chr.setVisibility(View.INVISIBLE);

        cloud.clearAnimation();
        cloud.animate().cancel();
        bomb.setVisibility(View.GONE);
        cloud.setVisibility(View.GONE);
        seal.setVisibility(View.VISIBLE);
        start_bt.setVisibility(View.GONE);

        record_window.setVisibility(View.VISIBLE);
        record_time.setText(timer.getText().toString());

        load_database();

        record_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText record_name = findViewById(R.id.g3_record_name); // 데이터베이스에 기록할 이름
                Record_game record_game = new Record_game(record_name.getText().toString(), timer.getText().toString());

                save_database(record_game); // 데이터 저장
                Toast.makeText(getApplicationContext(), "점수가 기록되었습니다.", Toast.LENGTH_SHORT).show();

                Animation ani = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down);
                ani.setFillAfter(true);
                ani.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        record_window.setVisibility(View.GONE);
                        //game_start_bt.setVisibility(View.VISIBLE);
                        finish();
                        startActivity(new Intent(getApplicationContext(), Game3.class));
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                record_window.startAnimation(ani);
                record_name.setText("");
            }
        });
    }

    @SuppressLint("HandlerLeak")
    Handler helper = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    bomb.setVisibility(View.INVISIBLE);
                    Thread delay = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(bombtime/5);
                                helper.sendEmptyMessage(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    delay.start();

                    //game_handler.sendEmptyMessage(3);
                    break;
                case 1:
                    bomb.setVisibility(View.VISIBLE);
                    bomb_ani.stop();
                    Log.d("충돌후", "폭탄 다시 세팅한다");
                    bomb.setX(background.getWidth());
                    Log.d("충돌후", "여길로 보냈다 : " + bomb.getX());
                    bomb.setImageResource(R.drawable.bombani);
                    bomb_ani = (AnimationDrawable) bomb.getDrawable();
                    bomb_ani.start();
                    break;
            }

        }
    };

    @SuppressLint("HandlerLeak")
    Handler Timer = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            timer.setText(getTimeout());
            Timer.sendEmptyMessage(0);
        }
    };

    private String getTimeout() {
        long now = SystemClock.elapsedRealtime();
        long outTime = now - myBaseTime;
        String out_time = String.format("%02d:%02d:%02d", outTime/1000/60, (outTime/1000)%60, (outTime%1000)/10);
        return out_time;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        motion_detect_onoff = false;
        motion_detect.interrupt();
        Timer.removeMessages(0);
    }

    public void onClick_g3_start(View view) {
        myBaseTime = SystemClock.elapsedRealtime();
        Timer.sendEmptyMessage(0);

        //게임 시작하면 사람과 폭탄 애니메이션 동작
        bomb_ani = (AnimationDrawable) bomb.getDrawable();
        ani = (AnimationDrawable) chr.getDrawable();
        ani.start();
        bomb_ani.start();

        // 게임 시작후 구름과 폭탄 이동
        game_handler.sendEmptyMessage(2); // 구름이동 핸들러
        game_handler.sendEmptyMessage(3); // 폭탄이동 핸들러

        motion_detect.start(); // 동작감지 스레드 작동

        //화면에 다시 표시해준다.
        heart_gage.setVisibility(View.VISIBLE);
        chr.setVisibility(View.VISIBLE);
        bomb.setVisibility(View.VISIBLE);
        cloud.setVisibility(View.VISIBLE);

        seal.setVisibility(View.GONE);

        is_game_start = true;


    }

    void load_database() {

        rank_list = new ArrayList<>();
        databaseReference.child("Game").child("Nova_run").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Record_game tmp = dataSnapshot.getValue(Record_game.class);
                rank_list.add(tmp);

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

    void save_database(Record_game nowgame) {

        rank_list.add(nowgame); // 리스트에 저장

        Collections.sort(rank_list, new Ascending()); // 시간이 빠른 순으로 정렬

        //리스트 크기가 10이상이면 지운다.
        if (rank_list.size() > 10) {
            for (int i = 10; i < rank_list.size(); i++) {
                rank_list.remove(i);
            }
        }
        // 데이터베이스에 저장
        databaseReference.child("Game").child("Nova_run").setValue(rank_list);
    }

    class Ascending implements Comparator<Record_game> {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public int compare(Record_game o1, Record_game o2) {

            return  o2.time.compareTo(o1.time);
        }
    }
}
