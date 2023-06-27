package com.example.raihenv2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import petrov.kristiyan.colorpicker.ColorPicker;

public class EditEvent extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public String currentUserUID = mAuth.getCurrentUser().getUid();
    public String currentDate ="";
    public String currentName = "";
    public int currentEventNumber;
    public String currentEventType =  "Event";
    public String[] items = {"Event", "Assignment", "Homework"};
    public Event currentEvent;
    public String currentColor;

    public EditText name;
    public EditText info;
    public TextView type;
    public Button colPick;
    private TextToSpeech textToSpeech;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        Intent intent = getIntent();
        currentDate = intent.getStringExtra("date");
        currentEventNumber = intent.getIntExtra("eventNumber", 0);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                textToSpeech.setLanguage(Locale.US);

            }
        });
        //Start of feedback
        ImageView TTS = findViewById(R.id.speak);
        TTS.setOnClickListener(this::TexttoSpeechButton);

    }

    @Override
    protected void onStart() {
        super.onStart();
        TextView test = findViewById(R.id.edit_event_title);
        type = (TextView) findViewById(R.id.currentType);
        name = (EditText)findViewById(R.id.newName);
        info = (EditText)findViewById(R.id.newInfo);
        colPick = (Button)findViewById(R.id.edit_pick_color);

        Spinner spinner = (Spinner) findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.event_types, R.layout.spinner_list);
        adapter.setDropDownViewResource(R.layout.spinner_list);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        try{
            FirebaseDatabase.getInstance().getReference("Events/" + currentUserUID +"/" + currentDate + "/Event-" +currentEventNumber).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    try{
                        Map<String, Object> results = (Map<String, Object>) task.getResult().getValue();
                        Log.d("color", (String)results.get("EventColor"));
                        currentEvent = new Event(currentEventNumber, (String)results.get("EventName"), (String)results.get("EventInfo"), (String)results.get("EventType"), (String)results.get("EventColor"));
                        Log.d("editEvent", "got update event");

                        name.setHint(currentEvent.eventName);
                        info.setHint(currentEvent.eventInfo);
                        type.setText("Current Event Type is: " + currentEvent.eventType);
                        currentColor = currentEvent.eventColor;
                        colPick.setBackgroundColor(Color.parseColor(currentEvent.eventColor));
                        test.setText("Editing\n" + currentEvent.eventName);
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
    public void updateEventData(View v){
        //map values
        try{
            Event updatedEvent = changeDatainEvent(currentEvent);

            Map<String, Object> updatedb = new HashMap<>();
            Map<String, Object> value = updatedEvent.toMap();

            updatedb.put("Events/"+currentUserUID+"/"+ currentDate + "/Event-"+ currentEventNumber, value);

            //update realtime db

            FirebaseDatabase.getInstance().getReference().updateChildren(updatedb).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isComplete()){
                        Toast.makeText(EditEvent.this,"Successfully Updated", Toast.LENGTH_LONG).show();
                        finish();

                    }
                }
            });
        }
        catch (Exception e)
        {
            Log.d("Error Found", e.toString());
            Toast.makeText(this,"No internet Connection Found", Toast.LENGTH_LONG).show();
        }


    }
    public Event changeDatainEvent(Event eve){
        if(!name.getText().toString().matches("")){
            eve.eventName = (String)name.getText().toString();
            Log.d("editEvent", "name updated");
        }
        if(!info.getText().toString().matches("")){
            eve.eventInfo = (String)info.getText().toString();
            Log.d("editEvent", "info updated" + info.getText().toString());
        }
        eve.eventType = currentEventType;
        eve.eventColor = currentColor;

        Log.d("editEvent", "event updated");
        return eve;
    }
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        currentEventType = items[i];
        type.setText("Selected Event Type: " + currentEventType);
        Log.d("editEvent", "eveytppe changed to " + currentEventType);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        type.setText("Selected Event Type: " + currentEventType);
    }
    public void chooseColor(View v){

        ColorPicker picker = new ColorPicker(this);
        picker.show();
        picker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
            @Override
            public void onChooseColor(int position,int color) {
                // put code
                colPick.setBackgroundColor(color);
                currentColor =  String.format("#%06X", (0xFFFFFF & color));
            }

            @Override
            public void onCancel(){
                // put code
            }
        });
    }

    public void TexttoSpeechButton(View view)
    {
        if(!textToSpeech.isSpeaking()) {
            textToSpeech.speak("This is the Edit Event Page             Here you can change Event name ,details type and color        Click on update to complete", TextToSpeech.QUEUE_FLUSH, null, null);
        }
        else {
            Toast.makeText(this, "System Busy", Toast.LENGTH_SHORT).show();
        }
    }
}