package com.example.raihenv2;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int SPEECH_REQUEST_CODE = 0;
    private DrawerLayout drawer;
    public static int seppukuCounter = 0;

    private FirebaseAuth myAuth;
    private DatabaseReference databaseReference;

    private TextToSpeech textToSpeech;

    ContextWrapper cw;
    File directory;
    String hintSpoken = "0 0 0 0 0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            myAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference();

            cw = new ContextWrapper(MainActivity.this);
            directory = cw.getDir("SomethingRepo", Context.MODE_PRIVATE);
            File file = new File(directory, "readHint.txt");
            if (file.exists()) {
                FileReader fileReader = new FileReader(file);
                BufferedReader bReader = new BufferedReader(fileReader);
                String tLine = bReader.readLine();
                hintSpoken = tLine;
                bReader.close();
                fileReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                textToSpeech.setLanguage(Locale.UK);
            }
        });

        findViewById(R.id.fragment_cotainer).setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    String word = "";
                    boolean read = false;
                    String[] didRead = hintSpoken.split(" ");
                    for (int i = 0; i < didRead.length; i++) {
                        if (i == Constants.CURRENT_PAGE && Integer.valueOf(didRead[i]) == 0) {
                            Log.d("TEST", String.valueOf(hintSpoken.charAt(Constants.CURRENT_PAGE)));
                            word += "1 ";
                            read = true;
                        } else {
                            word += didRead[i] + " ";
                        }
                    }

                    hintSpoken = word;
                    try {//maybe need to delete folder
                        File file = new File(directory, "readHint.txt");
                        FileWriter fw = new FileWriter(file);
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(hintSpoken);
                        bw.close();
                        fw.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }

                    if (read) {
                        switch (Constants.CURRENT_PAGE) {
                            case 0:
                                word = "You are on home page. Speak up!";
                                break;
                            case 1:
                                word = "You are on Profile page. Speak up!";
                                break;
                            case 2:
                                word = "You are on Calender page. Speak up!";
                                break;
                            case 3:
                                word = "You are on Schedule page. Speak up!";
                                break;
                            case 4:
                                word = "You are on Game page. Speak up!";
                                break;
                            case 5:
                                word = "You are on Setting page. Speak up!";
                                break;
                        }
                    } else {
                        word = "Where do you wanna go?";
                    }
                    hintFirst(word);
                    return super.onDoubleTap(e);
                }
                @Override
                public boolean onSingleTapConfirmed(MotionEvent event) {
                    return false;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if(savedInstanceState == null){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_cotainer, new HomeFragment()).commit();
        Constants.CURRENT_PAGE = 0;
        navigationView.setCheckedItem((R.id.nav_home));
        }

        try {
            databaseReference.child("userInfo").child("seppuku").child(myAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    String word = "0";
                    try{
                        if (task.getResult().getValue() != null) {
                            word = task.getResult().getValue().toString();
                            seppukuCounter = Integer.valueOf(word);
                        } else {
                            try {
                                File file = new File(directory, "seppuku.txt");

                                if (file.exists()) {
                                    FileReader testReader = new FileReader(file);
                                    BufferedReader bReader = new BufferedReader(testReader);
                                    String tLine = bReader.readLine();
                                    seppukuCounter = (int) Integer.valueOf(tLine);
                                    Log.d("SEPPUKU", "onCreate: " + seppukuCounter);
                                    bReader.close();
                                    testReader.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    catch(Exception e){
                        Log.d("dunno", e.getLocalizedMessage());
                    }

                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.nav_home:
                Constants.CURRENT_PAGE = 0;
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_cotainer, new HomeFragment()).commit();
                break;
            case R.id.nav_profile:
                Constants.CURRENT_PAGE = 1;
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_cotainer, new ProfileFragment()).commit();
                break;
            case R.id.nav_calendar:
                Constants.CURRENT_PAGE = 2;
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_cotainer, new KalenderFragment()).commit();
                break;
            case R.id.nav_schedule:
                Constants.CURRENT_PAGE = 3;
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_cotainer, new ScheduleFragment()).commit();
                break;
            case R.id.nav_seppuku:
                youDied();
                Toast.makeText(this,"Seppuku Committed",Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_flappy:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_cotainer, new gameActivity()).commit();
                break;
            case R.id.nav_settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_cotainer, new SettingFragment()).commit();
                break;
            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                break;
//            case R.id.nav_friends:
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_cotainer, new FriendsActivity()).commit();
//                Constants.CURRENT_PAGE = 4;
//                break;
            case R.id.nav_exit:
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Warning")
                        .setMessage("Do you wish to close the app?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                close();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
                break;


        }

        //Log.d("TAG", "onNavigationItemSelected: " + vef);

        if(item.getItemId()!=R.id.nav_seppuku)
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    void close() {
        finish();
        System.exit(0);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Warning")
                    .setMessage("Do you wish to close the app?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            //Toast.makeText(MainActivity.this, "Yaay", Toast.LENGTH_SHORT).show();
                            close();
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        }
    }

    public void youDied() {
        seppukuCounter++;
        try {
            databaseReference.child("userInfo").child("seppuku").child(myAuth.getCurrentUser().getUid()).setValue(seppukuCounter);

            File file = new File(directory, "seppuku.txt");
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            String tem = String.valueOf(seppukuCounter);
            Log.d("SEPPUKU", "onCreate: " + tem);
            bw.write(tem);
            bw.close();
            fw.close();
            if (Constants.CURRENT_PAGE == 1) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_cotainer, new ProfileFragment()).commit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void hintFirst(String word) {
        if(!textToSpeech.isSpeaking()) {
            textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, "null");
            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {

                }

                @Override
                public void onDone(String utteranceId) {
                    //take input command after text to speech is over
                    displaySpeechRecognizer();
                }

                @Override
                public void onError(String utteranceId) {

                }
            });
        }
        else {
            Toast.makeText(this, "System Busy", Toast.LENGTH_SHORT).show();
        }
    }

    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            for (int i = 0; i < results.size(); i++) {
                String possibleText = results.get(i).toLowerCase();
                String[] spokenText = possibleText.split(" ");
                for (int j = 0; j < spokenText.length; j++) {
                    Log.d("TEST", spokenText[j]);
                    if (spokenText[j].equals("home")) {
                        Constants.CURRENT_PAGE = 0;
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_cotainer, new HomeFragment()).commit();
                        return;
                    } else if (spokenText[j].equals("profile")) {
                        Constants.CURRENT_PAGE = 1;
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_cotainer, new ProfileFragment()).commit();
                        return;
                    } else if (spokenText[j].equals("calendar") || spokenText[j].equals("Kalender")) {
                        Constants.CURRENT_PAGE = 2;
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_cotainer, new KalenderFragment()).commit();
                        return;
                    } else if (spokenText[j].equals("schedule")) {
                        Constants.CURRENT_PAGE = 3;
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_cotainer, new ScheduleFragment()).commit();
                        return;
                    } else if (spokenText[j].equals("friends")) {
                        textToSpeech.speak("There used to be friends.....    but......    no more...", TextToSpeech.QUEUE_FLUSH, null, null);
                        return;
                    } else if (spokenText[j].equals("seppuku") || spokenText[j].equals("suicide") || spokenText[j].equals("die") || spokenText[j].equals("ashley")) {
                        youDied();
                        return;
                    } else if (spokenText[j].equals("game") || spokenText[j].equals("flappy")) {
                        Constants.CURRENT_PAGE = 4;
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_cotainer, new gameActivity()).commit();
                        return;
                    }else if (spokenText[j].equals("settings")) {
                        Constants.CURRENT_PAGE = 5;
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_cotainer, new SettingFragment()).commit();
                        return;
                    }else if (spokenText[j].equals("sign")){
                        FirebaseAuth.getInstance().signOut();
                        finish();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                    else if (spokenText[j].equals("exit") || spokenText[j].equals("close")){
                        close();
                    }

                }
            }
            textToSpeech.speak("Why don't you try speaking properly.", TextToSpeech.QUEUE_FLUSH, null, null);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}