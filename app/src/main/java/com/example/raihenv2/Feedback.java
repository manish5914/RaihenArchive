package com.example.raihenv2;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;


public class Feedback extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth myAuth;
    private float val;

    private ImageView[] FStar = new ImageView[5];
    private ImageView[] HStar = new ImageView[5];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback);

        try {
            myAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference();
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView feedbackinfo = findViewById(R.id.feedbackinfo);

        FStar[0] = findViewById(R.id.full_star1);
        FStar[1] = findViewById(R.id.full_star2);
        FStar[2] = findViewById(R.id.full_star3);
        FStar[3] = findViewById(R.id.full_star4);
        FStar[4] = findViewById(R.id.full_star5);

        HStar[0] = findViewById(R.id.half_star1);
        HStar[1] = findViewById(R.id.half_star2);
        HStar[2] = findViewById(R.id.half_star3);
        HStar[3] = findViewById(R.id.half_star4);
        HStar[4] = findViewById(R.id.half_star5);


        Slider slider = findViewById(R.id.slider);
        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                value = slider.getValue();
                int passingInt = (int) value;
                val = (float) passingInt / 2;
                switch ((int)value)
                {
                    case 1:
                        clearStar();
                        HStar[0].setVisibility(View.VISIBLE);
                        break;

                    case 2:
                        clearStar();
                        setFStar(1);
                        break;

                    case 3:
                        clearStar();
                        setFStar(1);
                        HStar[1].setVisibility(View.VISIBLE);
                        break;

                    case 4:
                        clearStar();
                        setFStar(2);
                        break;

                    case 5:
                        clearStar();
                        setFStar(2);
                        HStar[2].setVisibility(View.VISIBLE);
                        break;

                    case 6:
                        clearStar();
                        setFStar(3);
                        break;
                    case 7:
                        clearStar();
                        setFStar(3);
                        HStar[3].setVisibility(View.VISIBLE);
                        break;

                    case 8:
                        clearStar();
                        setFStar(4);
                        break;
                    case 9:
                        clearStar();
                        setFStar(4);
                        HStar[4].setVisibility(View.VISIBLE);
                        break;

                    case 10:
                        clearStar();
                        setFStar(5);
                        break;


                    default:
                        clearStar();
                        break;




                }
            }
        });

        databaseReference.child("feedback").child("users").child(myAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.getResult().getValue() != null) {
                    feedbackinfo.setText(task.getResult().child("description").getValue().toString());
                    slider.setValue((Float.valueOf(task.getResult().child("rating").getValue().toString())) * 2);
                }
            }
        });

        Button button = findViewById(R.id.cancel_fb);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Jason over here come look at this
        Button submit = findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean change = false;
                databaseReference.child("feedback").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.getResult().getValue() != null) {
                            if (task.getResult().child("rating").getValue() != null && task.getResult().child("count").getValue() != null) {
                                if (task.getResult().child("users").child(myAuth.getCurrentUser().getUid()).exists()) {
                                    float rating = Float.valueOf(task.getResult().child("rating").getValue().toString());
                                    int count = Integer.valueOf(task.getResult().child("count").getValue().toString());
                                    float myrating = Float.valueOf(task.getResult().child("users").child(myAuth.getCurrentUser().getUid()).child("rating").getValue().toString());

                                    float newRating = (((rating * count) - myrating) + val) / count;
                                    databaseReference.child("feedback").child("rating").setValue(newRating);
                                } else {

                                    float rating = Float.valueOf(task.getResult().child("rating").getValue().toString());
                                    int count = Integer.valueOf(task.getResult().child("count").getValue().toString()) + 1;

                                    databaseReference.child("feedback").child("count").setValue(count);
                                    float newRating = ((rating * (count - 1)) + val) / count;
                                    databaseReference.child("feedback").child("rating").setValue(newRating);
                                }
                            } else {
                                databaseReference.child("feedback").child("count").setValue(1);
                                databaseReference.child("feedback").child("rating").setValue(val);
                            }

                            finish();
                        }
                    }
                });
                databaseReference.child("feedback").child("users").child(myAuth.getCurrentUser().getUid()).child("rating").setValue(val);
                String desc = feedbackinfo.getText().toString();
                if (desc != null) {
                    databaseReference.child("feedback").child("users").child(myAuth.getCurrentUser().getUid()).child("description").setValue(desc);
                }

            }
        });
    }

    public void setFStar(int i)
    {
        for(int j=0;j<i;j++)
        {
            FStar[j].setImageDrawable(getResources().getDrawable(R.drawable.ic_star_filled,getApplicationContext().getTheme()));
        }
    }

    public void clearStar()
    {
        for(int j=0;j<=4;j++)
        {
            FStar[j].setImageDrawable(getResources().getDrawable(R.drawable.ic_star_full,getApplicationContext().getTheme()));
            HStar[j].setVisibility(View.GONE);
        }
    }
}