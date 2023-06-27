package com.example.raihenv2;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import petrov.kristiyan.colorpicker.ColorPicker;

public class AddEvent extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    private FloatingActionButton micButton;
    int REQUEST_CODE_SPEECH_INPUT = 1;
    private FirebaseAuth mAuth;
    private String currentDate;
    private  TextView Name_Speak,Info_Speak;
    private String eventName, eventInfo;
    private String eventColor = "#00CCCC";
    private String eventType = "Event";
    private EditText name, info;
    public int NumEvent= 0;
    public String[] items = {"Event", "Assignment", "Homework"};

    //J trial variables
    ConstraintLayout defLayout, shareLayout;
    ListView listlist;
    int friendCount, groupCount, click, length;
    int pointer =0;
    ContextWrapper cw;
    File gDirectory, fDirectory;
    String userId;

    String[] fImages, fNames;
    String[] gImages, gNames;
    boolean[] fChosen, gChosen;
    private TextToSpeech textToSpeech;

    private DatabaseReference databaseReference, profileRef;
    //End trial

    Button scan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        Intent intent = getIntent();
        currentDate = intent.getStringExtra("DATE");

        //J Enclose
        defLayout = findViewById(R.id.normal_default);
        shareLayout = findViewById(R.id.shareLayout);
        listlist = findViewById(R.id.listlist);
        friendCount = groupCount = 0;


        cw = new ContextWrapper(getApplicationContext());
        gDirectory = cw.getDir("Groups", Context.MODE_PRIVATE);
        fDirectory = cw.getDir("Friends", Context.MODE_PRIVATE);

        //End Enclose
        scan = (Button)findViewById(R.id.scanqr);
        scan.setOnClickListener(this);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                textToSpeech.setLanguage(Locale.US);

            }
        });
        //Start of feedback
        ImageView TTS = findViewById(R.id.speak);
        TTS.setOnClickListener(this::TexttoSpeechButton);
        Name_Speak=(EditText)findViewById(R.id.eventName);
        Info_Speak=(EditText)findViewById(R.id.eventInfo);

        micButton = findViewById(R.id.speech);

        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pointer =0;
                Intent intent
                        = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
                } catch (Exception e) {
                    Toast
                            .makeText(AddEvent.this, " " + e.getMessage(),
                                    Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    void setList() {
        CustomAdapter customAdapter = new CustomAdapter();
        listlist.setAdapter(customAdapter);
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (click == 0) {
                return friendCount;//length of array
            } else {
                return groupCount;
            }
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.customlayout, null);
            ImageView myImageView = view.findViewById(R.id.imageView);
            TextView myTextView = view.findViewById(R.id.textView);

            CheckBox myCheckBox = view.findViewById(R.id.checkBox);
            myCheckBox.setVisibility(View.VISIBLE);
            myCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (myCheckBox.isChecked()) {
                        if (click == 0) {
                            fChosen[position] = true;
                        } else {
                            gChosen[position] = true;
                        }
                    } else {
                        if (click == 0) {
                            fChosen[position] = false;
                        } else {
                            gChosen[position] = false;
                        }
                    }
                }
            });

            if (click == 0) {
                if (fChosen[position]) {
                    myCheckBox.setChecked(true);
                }
            } else {
                if (gChosen[position]) {
                    myCheckBox.setChecked(true);
                }
            }

            if (click == 0) {
                //set image for [position]
                File checkFile = new File(fImages[position]);
                if (checkFile.exists()) {
                    myImageView.setImageBitmap(BitmapFactory.decodeFile(fImages[position]));
                } else {
                    myImageView.setImageDrawable(getResources().getDrawable(R.drawable.jotaro));
                }
                Log.d("TAG", "getView: " + fImages[position]);
                //set name for [position]
                myTextView.setText(fNames[position]);
            } else {
                //set image for [position]
                File checkFile = new File(gImages[position]);
                if (checkFile.exists()) {
                    myImageView.setImageBitmap(BitmapFactory.decodeFile(gImages[position]));
                } else {
                    myImageView.setImageDrawable(getResources().getDrawable(R.drawable.jotaro));
                }
                Log.d("TAG", "getView: " + gImages[position]);
                //set name for [position]
                myTextView.setText(gNames[position]);
            }
            return view;
        }
    }

    void shareEvent() {
        Log.d("TAG", "shareEvent: it got called");
        try {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < friendCount; i++) {
            if (fChosen[i]) {
                profileRef = databaseReference.child(fNames[i]);

                profileRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.getResult().getValue() != null) {
                            userId = task.getResult().child("uid").getValue().toString();

                            //add event
                            shareEventPart2();
                        }
                    }
                });
            }
        }

        for (int i = 0; i < groupCount; i++) {
            if (gChosen[i]) {
                try {
                    File groupFile = new File(gDirectory, gNames[i]);
                    File destination = new File(groupFile, "namelist.txt");
                    FileReader fileReader = new FileReader(destination);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    //StringBuilder stringBuilder = new StringBuilder();
                    String line = bufferedReader.readLine();
                    while (line != null) {
                        try {
                            profileRef = databaseReference.child(fNames[i]);

                            profileRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (task.getResult().getValue() != null) {
                                        userId = task.getResult().child("uid").getValue().toString();

                                        //add event
                                        shareEventPart2();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        line = bufferedReader.readLine();
                    }
                    bufferedReader.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //finish();
    }

    void shareEventPart2() {
        try {
            FirebaseDatabase.getInstance().getReference("Events").child(userId).child("stats").child(currentDate).child("numberEvents").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    try{
                        if(task.getResult().getValue() != null){
                            Log.d("addEvent", String.valueOf(task.getResult().getValue()));
                            Long temp = (long) task.getResult().getValue();
                            setData(temp.intValue(), userId);
                        }
                        else{
                            setData(1, userId);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.d("Error Found", e.toString());
                        Toast.makeText(getApplicationContext(),"No internet Connection Found", Toast.LENGTH_LONG).show();
                        finish();
                    }

                }
            });
        }
        catch (Exception e)
        {
            Log.d("Error Found", e.toString());
            Toast.makeText(this,"No internet Connection Found", Toast.LENGTH_LONG).show();
            finish();
        }


    }



















    @Override
    protected void onStart() {
        super.onStart();
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.event_types, R.layout.spinner_list);
        adapter.setDropDownViewResource(R.layout.spinner_list);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    public void addEvent(View v){
        name = (EditText)findViewById(R.id.eventName);
        eventName = (String) name.getText().toString();
        info = (EditText) findViewById(R.id.eventInfo);
        eventInfo = (String) info.getText().toString();


        if (eventName.isEmpty()){
            name.setError("Enter Name of Event");
            name.requestFocus();
            return;
        }
        if (eventInfo.isEmpty()){
            info.setError("Enter Info about Event");
            info.requestFocus();
            return;
        }
        if(!eventInfo.isEmpty() && !eventName.isEmpty()){
            checkNumberEvent();
        }
        shareEvent();
    }
    public void checkNumberEvent(){
        String tempUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        try {
            FirebaseDatabase.getInstance().getReference("Events").child(tempUID).child("stats").child(currentDate).child("numberEvents").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    try{
                        if(task.getResult().getValue() != null){
                            Log.d("addEvent", String.valueOf(task.getResult().getValue()));
                            Long temp = (long) task.getResult().getValue();
                            setData(temp.intValue(),tempUID);
                        }
                        else{
                            setData(1, tempUID);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.d("Error Found", e.toString());
                        Toast.makeText(getApplicationContext(),"Failed", Toast.LENGTH_LONG).show();
                        finish();
                    }

                }
            });
        }
        catch (Exception e)
        {
            Log.d("Error Found", e.toString());
            Toast.makeText(this,"No internet Connection Found", Toast.LENGTH_LONG).show();
            finish();
        }

    }
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        eventType = items[i];
        Log.d("Spinner", "item selected " + items[i] + " currentType is " + eventType);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
    //private int currentNumber;
    private void setData(int newNum, String uid){
        mAuth = FirebaseAuth.getInstance();
        //int currentNumber=0;
        //get current user uid
        String currentUserUID = uid;
        Log.d("TAG", "setData: " + currentUserUID);
        String type = eventType;
        FirebaseDatabase.getInstance().getReference("Events").child(currentUserUID).child("stats").child(currentDate).child("currentNumber").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.getResult().getValue() == null)
                {
                    addData(0,currentUserUID, newNum, type);
                    FirebaseDatabase.getInstance().getReference("Events").child(currentUserUID).child("stats").child(currentDate).child("currentNumber").setValue(1);
                    FirebaseDatabase.getInstance().getReference("Events").child(currentUserUID).child("stats").child(currentDate).child("numberEvents").setValue(1);
                }
                else{
                    Long cNum = (long) task.getResult().getValue();
                    addData(cNum.intValue(), currentUserUID, newNum, type);
                }
            }
        });

    }
    public void addData(int currentNumber, String currentUserUID, int numEvent, String type){
        currentNumber++;
        numEvent++;
        //create new Event
        Event currentEvent = new Event(currentNumber, eventName, eventInfo, type, eventColor);

        //FirebaseDatabase.getInstance().getReference("Events").child(mAuth.getCurrentUser().getUid()).child(currentDate).child("Event").setValue(currentEvent);
        //FirebaseDatabase.getInstance().getReference("Events").child(currentUserUID).child(currentDate+"/currentNumber").setValue(temp[0]+1);

        Map<String, Object> childUpdate = new HashMap<String, Object>();
        Map<String, Object> values = currentEvent.toMap();
        Log.d("hello", "current type is " +  type);

        //adding data to hash map
        childUpdate.put("Events/"+currentUserUID+"/"+ currentDate + "/Event-"+ currentNumber, values);
        childUpdate.put("Events/"+currentUserUID+"/stats/"+currentDate+"/numberEvents", numEvent);
        childUpdate.put("Events/"+currentUserUID+"/stats/"+ currentDate+"/currentNumber", currentNumber);

        //updating database
        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isComplete()){
                    Toast.makeText(AddEvent.this,"Successfully Added On: " + currentDate, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
        //TextView t = (TextView) getView().findViewById(R.id.currentDate);
        //t.setText("currentNum" + currentNumber);
    }
    public void chooseColor(View v){
        Button colPick = (Button)v.findViewById(R.id.color_pick);

        ColorPicker picker = new ColorPicker(this);
        picker.show();
        picker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
            @Override
            public void onChooseColor(int position,int color) {
                // put code
                colPick.setBackgroundColor(color);
                eventColor =  String.format("#%06X", (0xFFFFFF & color));
            }

            @Override
            public void onCancel(){
                // put code
            }
        });
    }
    @Override
    public void onClick(View v) {
        IntentIntegrator ii = new IntentIntegrator(this);
        ii.setPrompt("Scan a QR Code");
        ii.setOrientationLocked(false);
        ii.setBeepEnabled(false);
        ii.initiateScan();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                if(pointer == 0) {
                    Name_Speak.setText(Objects.requireNonNull(result).get(0));
                    SpeechtoInfo();
                    return;
                }
                if(pointer == 1)
                {
                    Info_Speak.setText(Objects.requireNonNull(result).get(0));
                    return;
                }


            }
        }


        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                // if the intentResult is not null we'll set
                // the content and format of scan message
                //input.setText(intentResult.getContents().toString());

                //Log.d("qr", intentResult.getContents());
                String date = intentResult.getContents().substring(0, 12);
                //Log.d("output task", date);
                String strSource = intentResult.getContents().substring(13, intentResult.getContents().length()-1);
                //Log.d("output task", strSource);
                currentDate = date.substring(1, date.length()-1);
                //new HashMap object
                Map<String, String> hMapData = new HashMap<String, String>();

                //split the String by a comma
                String parts[] = strSource.split(",");

                //iterate the parts and add them to a map
                for(String part : parts) {

                    //split the employee data by : to get id and name
                    String empdata[] = part.split("=");

                    String strId = empdata[0].trim();
                    String strName = empdata[1].trim();

                    //add to map
                    hMapData.put(strId, strName);

                }
                eventName = hMapData.get("EventName");
                eventType = hMapData.get("EventType");
                eventColor = hMapData.get("EventColor");
                eventInfo = hMapData.get("EventInfo");
                //Log.d("qrSource", strSource);
                checkNumberEvent();

                //eventName = intentResult.getContents().
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void TexttoSpeechButton(View view)
    {
        if(!textToSpeech.isSpeaking()) {
            textToSpeech.speak("This is the Add Event page,   You can add your events here and choose the kind of events namely        Event,Assignment and Homework,       The color bar under the list allows you to choose the color of your events.      The color allows you to differentiate the events type by class,   or persons.          Scan allows you to scan a qr code which contains all the informaton of an event", TextToSpeech.QUEUE_FLUSH, null, null);
        }
        else {
            Toast.makeText(this, "System Busy", Toast.LENGTH_SHORT).show();
        }
    }

    public void SpeechtoInfo()
    {
        pointer=1;
        Intent intent
                = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast
                    .makeText(AddEvent.this, " " + e.getMessage(),
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }

}
