package com.test.animation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Random;

public class One_to_25 extends AppCompatActivity {

    ArrayList<One_to_25_list_info> list = new ArrayList<>();
    ArrayList<Integer> oneto50_list = new ArrayList<>();

    Random r = new Random();

    private GestureDetector gestureDetector; // 다양한 터치 이벤트를 처리하는 클래스, 길게누르기, 두번누르기 등등..

    LinearLayout linearLayout;

    TextView[] textViews;
    TextView timer, explain;
    Button start;

    boolean game_running = false;
    int game_code = 0; // 1to25 이면 1, 1to50 이면 2;

    ArrayList<Record_game> rank_list;

    int number = 1;
    private long myBaseTime;

    LinearLayout record;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_one_to_25);
        timer = findViewById(R.id.o2_time);
        start = findViewById(R.id.o2_start);
        record = findViewById(R.id.o2_record);
        explain = findViewById(R.id.o2_game_explain);
        Intent intent = getIntent();
        game_code = intent.getIntExtra("gamecode", 0);

        if(game_code == 0){
            timer.setText("1 to 25");
        }else if(game_code == 1){
            timer.setText("1 to 50");
        }

        gestureDetector = new GestureDetector(getApplicationContext(),new GestureDetector.SimpleOnGestureListener() {

            //누르고 뗄 때 한번만 인식하도록 하기위해서
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        // 레이아웃 세팅하는 부분, 게임 종류 상관없이 레이아웃은 25개
        int tmpId;
        textViews = new TextView[25];
        for(int i=1; i<=25; i++){
            list.add(new One_to_25_list_info());
            tmpId = getResources().getIdentifier("square"+i, "id", "com.test.animation");
            textViews[i-1] = findViewById(tmpId);
        }

        linearLayout = findViewById(R.id.o2_layout);
        linearLayout.setVisibility(View.INVISIBLE);


        linearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(gestureDetector.onTouchEvent(event) && game_running) {
                    Log.d("1to25", event.getX() + "," + event.getY());
                    int width = v.getWidth() / 5;
                    int height = v.getHeight() / 5;

                    int codi_x = (int) (event.getX() / width);
                    int codi_y = (int) (event.getY() / height);

                    final int xy = codi_y * 5 + codi_x;
                    Log.i("1to25", "현재 클릭 아이템 인덱스 : " + xy + "/" + codi_x + " , " + codi_y + " // " + textViews[xy].getText().toString() + " // " + number);

                    if (!list.get(xy).choice) {
                        if(game_code == 0) {
                            if (textViews[xy].getText().toString().equals(String.valueOf(number))) {
                                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down);
                                animation.setFillAfter(true);
                                animation.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                        textViews[xy].setBackgroundColor(Color.GRAY);
                                        list.get(xy).choice = true;
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        textViews[xy].setVisibility(View.INVISIBLE);

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                                textViews[xy].startAnimation(animation);
                                number++;

                                //게임 종료처리 부분
                                if (number == 26) {
                                    game_running = false;
                                    start.setText("RESTART");
                                    Timer.removeMessages(0);
                                    Animation animaition2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.game_end);
                                    animaition2.setFillAfter(true);
                                    timer.startAnimation(animaition2);
                                    timer.setTextColor(Color.parseColor("#2f2f2f"));
                                    record.setVisibility(View.VISIBLE);
                                    start.setVisibility(View.INVISIBLE);

                                    load_database(game_code);
                                    Button record_button = findViewById(R.id.o2_record_button);
                                    record_button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            EditText record_name = findViewById(R.id.o2_record_name);
                                            Record_game record_game = new Record_game(record_name.getText().toString(), timer.getText().toString());
                                            //databaseReference.child("Game").child("Oneto25").push().setValue(record_game);

                                            save_database(game_code, record_game);
                                            Toast.makeText(One_to_25.this, "점수가 기록되었습니다.", Toast.LENGTH_SHORT).show();

                                            Animation ani = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down);
                                            ani.setAnimationListener(new Animation.AnimationListener() {
                                                @Override
                                                public void onAnimationStart(Animation animation) {

                                                }

                                                @Override
                                                public void onAnimationEnd(Animation animation) {
                                                    start.setVisibility(View.VISIBLE);
                                                    record.setVisibility(View.GONE);
                                                }

                                                @Override
                                                public void onAnimationRepeat(Animation animation) {

                                                }
                                            });
                                            record_name.setText("");
                                            record.startAnimation(ani);

                                        }
                                    });



                                }
                            }else {
                                Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
                                //shake.setFillAfter(true);
                                shake.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                        textViews[xy].setBackgroundColor(Color.RED);

                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        textViews[xy].setBackgroundColor(Color.BLACK);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                                textViews[xy].startAnimation(shake);
                            }
                        }else if(game_code == 1){
                            //게임 진행 처리부분
                            if (Integer.parseInt(textViews[xy].getText().toString()) == oneto50_list.get(number-1)) {
                                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down);
                                animation.setFillAfter(true);
                                animation.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                        textViews[xy].setBackgroundColor(Color.GRAY);
                                        list.get(xy).choice = true;
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        textViews[xy].setVisibility(View.INVISIBLE);

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                                textViews[xy].startAnimation(animation);
                                number++;

                                //게임 종료처리 부분
                                if (number == 26) {
                                    game_running = false;
                                    start.setText("RESTART");
                                    Timer.removeMessages(0);
                                    Animation animaition2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.game_end);
                                    animaition2.setFillAfter(true);
                                    timer.startAnimation(animaition2);
                                    record.setVisibility(View.VISIBLE);
                                    start.setVisibility(View.INVISIBLE);

                                    load_database(game_code); // 저장된 데이터 불러오기, 시간이 오래걸리므로 미리 불러온다.
                                    Button record_button = findViewById(R.id.o2_record_button);
                                    record_button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            EditText record_name = findViewById(R.id.o2_record_name);
                                            Record_game record_game = new Record_game(record_name.getText().toString(), timer.getText().toString());
                                            //databaseReference.child("Game").child("Oneto25").push().setValue(record_game);

                                            save_database(game_code, record_game); // 데이터 저장
                                            Toast.makeText(One_to_25.this, "점수가 기록되었습니다.", Toast.LENGTH_SHORT).show();

                                            Animation ani = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down);
                                            ani.setAnimationListener(new Animation.AnimationListener() {
                                                @Override
                                                public void onAnimationStart(Animation animation) {

                                                }

                                                @Override
                                                public void onAnimationEnd(Animation animation) {

                                                    record.setVisibility(View.GONE);
                                                    start.setVisibility(View.VISIBLE);
                                                }

                                                @Override
                                                public void onAnimationRepeat(Animation animation) {

                                                }
                                            });
                                            record_name.setText("");
                                            record.startAnimation(ani);

                                        }
                                    });
                                }
                            }else {
                                Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
                                //shake.setFillAfter(true);
                                shake.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                        textViews[xy].setBackgroundColor(Color.RED);

                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        textViews[xy].setBackgroundColor(Color.BLACK);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                                textViews[xy].startAnimation(shake);
                            }
                        }
                    }
                }
                return true;


            }
        });

    }

    

    public void onClick_one25_start(View view) {
        timer.setTextColor(Color.parseColor("#e9e9e9"));
        explain.setVisibility(View.GONE);
        //게임 종류에 상관없이 공동적인 부분
        timer.clearAnimation();
        if(game_running){ // 게임이 실행중일때 버튼을 누르는 경우 => 게임 정지
            start.setText("RESTART");
            game_running = false;

            Timer.removeMessages(0);
        }else { // 게임이 정지 중일때 버튼을 누르는 경우 => 실행이된다.
            start.setText("STOP");
            game_running = true;

            number = 1;


            for (int i=0; i<25; i++){
                list.get(i).choice = false;
                textViews[i].setVisibility(View.VISIBLE);
                /*Animation animation = AnimationUtils.loadAnimation(this, R.anim.none);
                textViews[i].startAnimation(animation);*/
                textViews[i].clearAnimation();
                textViews[i].setBackgroundColor(Color.BLACK);
            }

            if(game_code == 0) { // 1to25 게임의 경우
                // 1~25까지의 숫자중 임의의 중복없는 숫자를 넣는다.
                for (int i = 0; i < 25; i++) {
                    list.get(i).number = r.nextInt(25) + 1;
                    for (int j = 0; j < i; j++) {
                        if (list.get(i).number == list.get(j).number) {
                            i--;
                        }
                    }
                }

                for (int i = 1; i <= 25; i++) {
                    textViews[i - 1].setText(String.valueOf(list.get(i - 1).number));
                }
            }else if(game_code == 1){ // 1to 50게임인 경우
                oneto50_list.clear();
                // 25개의 아이템에 50개의 숫자중 25개만 넣음
                for (int i = 0; i < 25; i++) {
                    list.get(i).number = r.nextInt(50) + 1;
                    for (int j = 0; j < i; j++) { // 중복만 안되게 한다.
                        if (list.get(i).number == list.get(j).number) {
                            i--;
                        }
                    }
                }

                for (int i = 1; i <= 25; i++) {
                    textViews[i - 1].setText(String.valueOf(list.get(i - 1).number));
                    oneto50_list.add(list.get(i-1).number);
                }

                final Comparator<Integer> sort = new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            return Integer.compare(o1, o2);
                        }
                        return 0;
                    }
                };
                Collections.sort(oneto50_list, sort);
                String tmp = "";
                for(int i=0; i<25; i++){
                    tmp += oneto50_list.get(i) + " ";
                }
                Log.i("ontto50", "정렬된 리스트 : " + tmp);
            }

            Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_up);
            animation.setFillAfter(true);
            animation.setDuration(500);
            linearLayout.startAnimation(animation);
            Animation.AnimationListener listener = new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    linearLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            };
            linearLayout.setLayoutAnimationListener(listener);

            myBaseTime = SystemClock.elapsedRealtime();
            Timer.sendEmptyMessage(0);
        }



    }


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
        Timer.removeMessages(0);
        super.onDestroy();
    }


    void load_database(int game_code){

        if(game_code == 0) {
            rank_list = new ArrayList<>();
            databaseReference.child("Game").child("Oneto25").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Record_game tmp = dataSnapshot.getValue(Record_game.class);
                    Log.i("1to25", tmp.name + tmp.time + "/" + dataSnapshot.getKey());

                    //if (tmp != null && rank_list.size() <= 10) {
                        rank_list.add(tmp);
                    //}
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


        }else if(game_code ==1){
            rank_list = new ArrayList<>();
            databaseReference.child("Game").child("Oneto50").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Record_game tmp = dataSnapshot.getValue(Record_game.class);
                    Log.i("1to25", tmp.name + tmp.time + "/" + dataSnapshot.getKey());

                    //if (tmp != null && rank_list.size() <= 10) {
                    rank_list.add(tmp);
                    //}
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

    }

    void save_database(int game_code, Record_game nowgame){
        if(game_code == 0) {
            rank_list.add(nowgame); // 리스트에 저장

            Collections.sort(rank_list, new Ascending()); // 시간이 빠른 순으로 정렬

            //리스트 크기가 10이상이면 지운다.
            if (rank_list.size() > 10) {
                for (int i = 10; i < rank_list.size(); i++) {
                    rank_list.remove(i);
                }
            }
            // 데이터베이스에 저장
            databaseReference.child("Game").child("Oneto25").setValue(rank_list);

        }else if(game_code == 1){
            rank_list.add(nowgame);

            Collections.sort(rank_list, new Ascending());

            if (rank_list.size() > 10) {
                for (int i = 10; i < rank_list.size(); i++) {
                    rank_list.remove(i);
                }
            }
            databaseReference.child("Game").child("Oneto50").setValue(rank_list);
        }
    }

    static class Ascending implements Comparator<Record_game>{

        @Override
        public int compare(Record_game o1, Record_game o2) {
            return o1.time.compareTo(o2.time);
        }
    }

}
