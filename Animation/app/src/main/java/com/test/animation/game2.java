package com.test.animation;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
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
import java.util.Random;

public class game2 extends AppCompatActivity {

    ConstraintLayout background;
    ImageView target_iv;
    int target_direction = 0; // 0: 아래, 1: 오른쪽, 2: 위, 3: 왼쪽
    ArrayList<Integer> food_resource = new ArrayList<>();
    ImageView food_iv, item_iv;
    Random r = new Random();
    LinearLayout seal;
    TextView point, timer, message;
    int point_number = 0;
    private boolean food_die_animation = false;
    private boolean item_die_animation = false;
    private long myBaseTime;

    int play_time = 30; // 게임 진행 시간 , 초

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    ArrayList<Record_game> rank_list;

    Button game_start_bt;
    LinearLayout record_panel;

    int[] item_source;

    boolean item_running = false; // 아이템을 먹으면 트루 아이템 효과중일땐 아이템 등장 안함
    int now_regen_item=-1;
    long item_effect_start_time;

    double speed; //팩맨 이동속도

    Toast item_toast;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game2);
        background = findViewById(R.id.g2_back);
        target_iv = findViewById(R.id.g2_target);
        food_iv = findViewById(R.id.food);
        seal = findViewById(R.id.g2_seal);
        point = findViewById(R.id.g2_point);
        timer = findViewById(R.id.g2_timer);

        game_start_bt = findViewById(R.id.g2_start_button); // 게임시작버튼
        record_panel = findViewById(R.id.g2_record); // 기록패널
        item_iv = findViewById(R.id.g2_item); // 아이템뷰
        message = findViewById(R.id.g2_message);
        message.setVisibility(View.GONE);

        item_toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        init();
        speed = 1.5;


        for (int i = 1; i <= 14; i++) {
            food_resource.add(getResources().getIdentifier("food_" + i, "drawable", "com.test.animation"));
        }

        item_source = new int[3];
        item_source[0] = getResources().getIdentifier("bomb", "drawable", "com.test.animation"); // 폭탄
        item_source[1] = getResources().getIdentifier("posion", "drawable", "com.test.animation"); // 독
        item_source[2] = getResources().getIdentifier("favourites", "drawable", "com.test.animation"); // 별

        // 화면을 터치할 때마다 팩맨의 이동방향을 바꿔주는 부분
        background.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (target_direction > 3) {
                    target_direction = 0;
                }
                target_iv.animate().cancel();


                int animationSpeed;
                Log.i("터치", "현재스피드 : " + speed);
                switch (target_direction) {
                    case 0: // 아래방향 이동

                        target_iv.setImageResource(R.drawable.down_packman);
                        animationSpeed = (int) ((background.getHeight() - target_iv.getY()) / speed);
                        target_iv.animate().y(background.getHeight() - target_iv.getHeight())
                                .setDuration(animationSpeed)
                                .start();
                        AnimationDrawable drawable = (AnimationDrawable) target_iv.getDrawable();
                        drawable.start();
                        target_direction++;
                        break;
                    case 1: // 오른쪽방향
                        target_iv.setImageResource(R.drawable.right_packman);
                        animationSpeed = (int) ((background.getWidth() - target_iv.getX()) / speed);
                        target_iv.animate().x(background.getWidth() - target_iv.getWidth())
                                .setDuration(animationSpeed)
                                .start();
                        drawable = (AnimationDrawable) target_iv.getDrawable();
                        drawable.start();
                        target_direction++;
                        break;
                    case 2: // 위방향
                        target_iv.setImageResource(R.drawable.up_packman);
                        animationSpeed = (int) (target_iv.getY() / speed);
                        target_iv.animate().y(0)
                                .setDuration(animationSpeed)
                                .start();
                        drawable = (AnimationDrawable) target_iv.getDrawable();
                        drawable.start();
                        target_direction++;
                        break;
                    case 3: // 왼쪽방향
                        target_iv.setImageResource(R.drawable.left_packman);
                        animationSpeed = (int) (target_iv.getX() / speed);
                        target_iv.animate().x(0)
                                .setDuration(animationSpeed)
                                .start();
                        drawable = (AnimationDrawable) target_iv.getDrawable();
                        drawable.start();
                        target_direction++;
                        break;
                }


                return false;
            }
        });

        /*target_iv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d("game2", "팩맨 위치 리스너 : " + target_iv.getX() + ", " + target_iv.getY());
            }
        });*/

    }

    private void init() {
        target_iv.setVisibility(View.INVISIBLE);
        food_iv.setVisibility(View.INVISIBLE);
        point.setVisibility(View.INVISIBLE);
        timer.setVisibility(View.INVISIBLE);
        seal.setVisibility(View.VISIBLE);
        item_iv.setVisibility(View.INVISIBLE);
    }


    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        food_thread_onoff = false;
        conflict_onoff = false;
        item_thread_onoff = false;
        thread.interrupt();
        thread_item.interrupt();
        coflict_target.interrupt();
        set_food_location.removeMessages(0);
        Timer.removeMessages(0);
    }

    public void onClick_g2_start(View view) {
        target_iv.setVisibility(View.VISIBLE);
        food_iv.setVisibility(View.VISIBLE);
        point.setVisibility(View.VISIBLE);
        point.setText("0");
        seal.setVisibility(View.GONE);
        timer.setVisibility(View.VISIBLE);

        set_food(0);
        set_item(0);
        thread.start();
        thread_item.start();
        coflict_target.start();

        myBaseTime = SystemClock.elapsedRealtime();
        Timer.sendEmptyMessage(0);
    }

    //음식 나오게하는 스레드
    private int disappear_time = 2000;
    int food_message = 0;
    private boolean food_thread_onoff = true;
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (food_thread_onoff) {
                try {
                    Thread.sleep(disappear_time);
                    set_food_location.sendEmptyMessage(food_message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    //아이템 뿌려주는 스레드
    private long item_time = 4000;
    int item_message = 0;
    private boolean item_thread_onoff = true;
    Thread thread_item = new Thread(new Runnable() {
        @Override
        public void run() {
            while (item_thread_onoff) {
                try {
                    Thread.sleep(item_time);
                    set_item_location.sendEmptyMessage(item_message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    private boolean conflict_onoff = true;
    // 충돌판정 스레드
    Thread coflict_target = new Thread(new Runnable() {
        @Override
        public void run() {
            while (conflict_onoff) {
                try {
                    Thread.sleep(10);
                    int range = target_iv.getHeight() / 2;
                    //음식충돌판정
                    if (Math.pow(food_iv.getX() - target_iv.getX(), 2) + Math.pow(food_iv.getY() - target_iv.getY(), 2) <= Math.pow(range * 2, 2)) {
                        Log.d("충돌판정", "푸드충돌!!");
                        food_message = 1;
                        set_food_location.sendEmptyMessage(food_message);


                    }
                    //아이템 충돌 판정
                    if (Math.pow(item_iv.getX() - target_iv.getX(), 2) + Math.pow(item_iv.getY() - target_iv.getY(), 2) <= Math.pow(range * 2, 2)) {
                        Log.d("충돌판정", "아이템충돌!!");
                        item_message = 1;
                        item_running = true;
                        item_effect_start_time = SystemClock.elapsedRealtime(); // 충돌할때의 시간 기록
                        set_item_location.sendEmptyMessage(item_message);


                    }
                    /*Log.i("충돌판정", "팩맨 : " + target_iv.getX() + " , " + target_iv.getY());
                    Log.i("충돌판정", "푸드 : " + food_iv.getX() + " , " + food_iv.getY());*/
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    //음식 유아이 변경 핸들러
    @SuppressLint("HandlerLeak")
    Handler set_food_location = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            set_food(msg.what);
        }
    };

    //아이템 위치 변경 핸들러
    @SuppressLint("HandlerLeak")
    Handler set_item_location = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            set_item(msg.what);
        }
    };

    @SuppressLint("HandlerLeak")
    Handler Timer = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String getTime = getTimeout();
            if (getTime.equals("0")) {
                timer.setText("00:00");
                Timer.removeMessages(0);
                game_end();

            } else {
                timer.setText(getTime);
                Timer.sendEmptyMessage(0);

            }

        }
    };

    private String getTimeout() {
        long now = SystemClock.elapsedRealtime();
        long outTime = play_time * 1000 - (now - myBaseTime);
        if (outTime < 5000) {
            timer.setTextColor(Color.RED);
        }
        if (outTime <= 0) {
            return "0";
        }
        if(item_running){
            if(now - item_effect_start_time > 3000){ // 아이템 효과 종료
                set_item_location.sendEmptyMessage(2);
            }
        }
        String out_time = String.format("%02d:%02d", (outTime / 1000) % 60, (outTime % 1000) / 10);
        return out_time;
    }


    private void game_end() {
        food_thread_onoff = false;
        conflict_onoff = false;
        item_thread_onoff = false;
        thread.interrupt();
        thread_item.interrupt();
        coflict_target.interrupt();
        set_food_location.removeMessages(0);
        //Timer.removeMessages(0);
        //Timer.removeCallbacksAndMessages(null);
        /*Message msg = Timer.obtainMessage();
        Timer.sendMessageDelayed(msg, 1000);*/

        Log.i("Eat_food", "game_end call!!");

        load_database();
        LinearLayout explain, item_ex;
        explain = findViewById(R.id.g2_explain);
        item_ex = findViewById(R.id.g2_ex_item);
        explain.setVisibility(View.GONE);
        item_ex.setVisibility(View.GONE);

        record_panel.setVisibility(View.VISIBLE);
        seal.setVisibility(View.VISIBLE);
        game_start_bt.setVisibility(View.GONE);
        target_iv.setVisibility(View.INVISIBLE); // 팩맨
        TextView endpoint = findViewById(R.id.g2_endpoint);
        endpoint.setText("SCORE : " + point_number + " point");

        Button record_bt = findViewById(R.id.g2_record_button); // 기록버튼
        record_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText record_name = findViewById(R.id.g2_record_name); // 데이터베이스에 기록할 이름
                Record_game record_game = new Record_game(record_name.getText().toString(), String.valueOf(point_number));

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

                        record_panel.setVisibility(View.GONE);
                        //game_start_bt.setVisibility(View.VISIBLE);
                        finish();
                        startActivity(new Intent(getApplicationContext(), game2.class));
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                record_panel.startAnimation(ani);
                record_name.setText("");
            }
        });


    }

    // 음식 위치 세팅 메소드
    private void set_food(int type) {
        if (type == 0) {
            food_iv.clearAnimation();
            food_iv.setVisibility(View.VISIBLE);
            int window_height = background.getHeight() - food_iv.getHeight();
            int window_width = background.getWidth() - food_iv.getWidth();
            food_iv.setImageResource(food_resource.get(r.nextInt(14)));
            food_iv.setY(r.nextInt(window_height) + 1);
            food_iv.setX(r.nextInt(window_width) + 1);
        } else if (type == 1) {

            if (!food_die_animation) {
                Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_down);
                //animation.setFillAfter(true);
                food_iv.startAnimation(animation);
                Animation.AnimationListener listener = new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                        point_number++;
                        food_die_animation = true;
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        food_message = 0;
                        food_die_animation = false;
                        food_iv.setX(10000);
                        food_iv.setY(10000);
                        set_food(food_message);
                        point.setText(String.valueOf(point_number));
                        //food_iv.setVisibility(View.GONE);
                        Log.d("포인트증가", "에니끝 " + point_number);
                        Animation point_plus_ani = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_up_and_down);
                        point.startAnimation(point_plus_ani);

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                };
                animation.setAnimationListener(listener);
            }
        }
    }

    // 아이템 위치 세팅 메소드
    private void set_item(int type) {
        if (type == 0) { // 충돌하기전
            if(!item_running) {
                Log.i("아이템 위치세팅", "충돌이 없으니 움직여야지");
                item_iv.clearAnimation();
                item_iv.setVisibility(View.VISIBLE);
                int window_height = background.getHeight() - item_iv.getHeight();
                int window_width = background.getWidth() - item_iv.getWidth();
                int ran = r.nextInt(3);
                item_iv.setImageResource(item_source[ran]);
                now_regen_item = ran;
                item_iv.setY(r.nextInt(window_height) + 1);
                item_iv.setX(r.nextInt(window_width) + 1);
            }else {

            }

        } else if (type == 1) { // 충돌후
            if(item_running){ // 아이템 효과 시작
                item_iv.setVisibility(View.GONE);
                message.setVisibility(View.VISIBLE);
                Log.d("Eat_Item", "먹은 아이템 코드 : " + now_regen_item + item_running);
                switch (now_regen_item){ // 아이템 동작시간은 3초동안
                    case 0: // 폭탄 아이템 - 이속 빠르게
                        message.setText("번개 아이템의 효과가 적용중입니다.");
                        speed = 4;
                        break;
                    case 1: // 독 아이템 - 자신의 이동속도를 느리게 2배 느리게
                        message.setText("독 아이템의 효과가 적용중입니다.");
                        speed = 0.1; // 원래 1.5
                        break;
                    case 2: // 별아이템 - 음식이 사라지는 속도를 2배 느리게
                        message.setText("별 아이템의 효과가 적용중입니다.");
                        disappear_time = 1000;
                        break;
                }
            }

            if (!item_die_animation) {
                Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_down);
                animation.setFillAfter(true);
                item_iv.startAnimation(animation);
                Animation.AnimationListener listener = new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        item_die_animation = true;
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        item_message = 0;
                        item_die_animation = false;
                        set_item(item_message);
                        item_iv.setVisibility(View.GONE);
                        Log.i("아이템 위치세팅", "충돌했으니 움직이지 않는다!!!, 저 멀리 보내버리자!!");
                        item_iv.setY(10000);
                        item_iv.setX(10000);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                };
                animation.setAnimationListener(listener);
            }
        }else if(type == 2){ // 아이템 효과 종료
            if(item_running){
                message.setVisibility(View.GONE);
                item_iv.setVisibility(View.VISIBLE);
                item_running = false;
                switch (now_regen_item){ // 아이템 동작시간은 3초동안
                    case 0: // 폭탄 아이템 - 자신의 이동속도 빠르게
                        speed = 1.5;
                        break;
                    case 1: // 독 아이템 - 자신의 이동속도를 느리게 2배 느리게
                        speed = 1.5;
                        break;
                    case 2: // 별아이템 - 음식이 사라지는 속도를 2배 느리게
                        disappear_time = 2000;
                        break;
                }
                item_message = 0;
                set_item(item_message);
            }
        }
    }

    //저장관련 메소드
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
            databaseReference.child("Game").child("Eat_Food").setValue(rank_list);
    }
    class Ascending implements Comparator<Record_game>{

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public int compare(Record_game o1, Record_game o2) {
            int one = Integer.parseInt(o1.time);
            int two = Integer.parseInt(o2.time);
            return Integer.compare(two, one);
        }
    }


    //저장된 데이터 로드 메소드
    void load_database() {

            rank_list = new ArrayList<>();
            databaseReference.child("Game").child("Eat_Food").addChildEventListener(new ChildEventListener() {
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
}
