package com.example.raihenv2;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class GameView extends View{

    private MovingIcon mIcon;
    private Handler handler;
    private Runnable r;
    private ArrayList<Obstacles> arrObstacles;
    private int sumObstables, distance;
    private int score, bestscore = 0;
    private boolean start;
    private Context context;
    private int soundJump;
    private float volume;
    private boolean loadedSound;
    private SoundPool soundPool;
    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        SharedPreferences sp = context.getSharedPreferences("gamesetting", Context.MODE_PRIVATE);
        if (sp!=null){
            bestscore = sp.getInt("bestscore", 0);
        }
        score = 0;
        start = false;
        initMI();
        initO();
        handler = new Handler();
        r = new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        };
        if (Build.VERSION.SDK_INT>=21) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setAudioAttributes(audioAttributes).setMaxStreams(5);
            this.soundPool = builder.build();
        } else {
            soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loadedSound = true;
            }
        });
        soundJump = this.soundPool.load(context, R.raw.jump_02, 1);
    }

    private void initO() {
        sumObstables = 4;
        distance = 369*Constants.SCREEN_HEIGHT/1920;
        arrObstacles = new ArrayList<>();
        for (int i =0; i< sumObstables; i++) {
            if (i < sumObstables/2) {
                this.arrObstacles.add(new Obstacles(Constants.SCREEN_WIDTH+i*((Constants.SCREEN_WIDTH+200*Constants.SCREEN_WIDTH/1000)/(sumObstables/2)),
                        0, 200*Constants.SCREEN_WIDTH/1000, Constants.SCREEN_HEIGHT/2));
                this.arrObstacles.get(this.arrObstacles.size()-1).setBm(BitmapFactory.decodeResource(this.getResources(), R.drawable.game_pipe2));
                this.arrObstacles.get(this.arrObstacles.size()-1).randomY();
            } else {
                this.arrObstacles.add(new Obstacles(this.arrObstacles.get(i-sumObstables/2).getX(), this.arrObstacles.get(i-sumObstables/2).getY()
                +this.arrObstacles.get(i-sumObstables/2).getHeight() + distance, 200*Constants.SCREEN_WIDTH/1000, Constants.SCREEN_HEIGHT/2));
                this.arrObstacles.get(this.arrObstacles.size()-1).setBm(BitmapFactory.decodeResource(this.getResources(), R.drawable.game_pipe1));
            }
        }
    }

    private void initMI() {
        mIcon = new MovingIcon();
        mIcon.setWidth(100*Constants.SCREEN_WIDTH/1000);
        mIcon.setHeight(100*Constants.SCREEN_HEIGHT/1920);
        mIcon.setX(100*Constants.SCREEN_WIDTH/1000);
        mIcon.setY(Constants.SCREEN_HEIGHT/2 - mIcon.getHeight()/2);
        ArrayList<Bitmap> arrBms = new ArrayList<>();
        arrBms.add(BitmapFactory.decodeResource(this.getResources(), R.drawable.game_bird1));
        arrBms.add(BitmapFactory.decodeResource(this.getResources(), R.drawable.game_bird2));
        mIcon.setArrBms(arrBms);
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (start) {
            mIcon.draw(canvas);
            for (int i = 0; i < sumObstables; i++) {
                if (mIcon.getRect().intersect(arrObstacles.get(i).getRect())||mIcon.getY()-mIcon.getHeight()<0||mIcon.getY()>Constants.SCREEN_HEIGHT){
                    Obstacles.speed=0;
                    gameActivity.txt_score_over.setText(gameActivity.txt_score.getText());
                    gameActivity.txt_best_score.setText("best: " + bestscore);
                    gameActivity.txt_score.setVisibility(INVISIBLE);
                    gameActivity.rl_game_over.setVisibility(VISIBLE);
                }
                if (this.mIcon.getX() + this.mIcon.getWidth() > arrObstacles.get(i).getX() + arrObstacles.get(i).getWidth() / 2
                        && this.mIcon.getX() + this.mIcon.getWidth() <= arrObstacles.get(i).getX() + arrObstacles.get(i).getWidth() / 2 + Obstacles.speed
                        && i < sumObstables / 2) {
                    score++;
                    if (score > bestscore) {
                        bestscore = score;
                        SharedPreferences sp = context.getSharedPreferences("gamesetting", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt("bestscore", bestscore);
                        editor.apply();
                    }
                    gameActivity.txt_score.setText("" + score);
                }
                if (this.arrObstacles.get(i).getX() < -arrObstacles.get(i).getWidth()) {
                    this.arrObstacles.get(i).setX(Constants.SCREEN_WIDTH);
                    if (i < sumObstables / 2) {
                        arrObstacles.get(i).randomY();
                    } else {
                        arrObstacles.get(i).setY(this.arrObstacles.get(i - sumObstables / 2).getY() + this.arrObstacles.get(i - sumObstables / 2).getHeight() + distance);
                    }
                }
                this.arrObstacles.get(i).draw(canvas);
            }
        } else {
            if (mIcon.getY()>Constants.SCREEN_HEIGHT/2) {
                mIcon.setDrop(-15*Constants.SCREEN_HEIGHT/1920);
            }
            mIcon.draw(canvas);
        }
        handler.postDelayed(r, 10);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (Constants.GAME_MODE == 0) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mIcon.setDrop(-15);
                if (loadedSound) {
                    int streamId = this.soundPool.play(this.soundJump, (float) 0.5, (float) 0.5, 1, 0, 1f);
                }
            }
        }
        return true;
    }

    public boolean setDropping() {
        if (start) {
            mIcon.setDrop(-15);
        }
        return true;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public void reset() {
        gameActivity.txt_score.setText("0");
        score=0;
        initO();
        initMI();
    }
}
