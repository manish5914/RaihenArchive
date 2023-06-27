package com.example.raihenv2;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class ScheduleFragment extends Fragment {

    private FloatingActionButton micButton;
    int REQUEST_CODE_SPEECH_INPUT = 1;
    int pointer =0;
    //use on both layout
    RadioGroup radioGroup;
    RadioButton radioButton;

    String theDay = "Monday";

    ConstraintLayout scheduleLayout, editScheduleLayout;

    boolean isView = true;
    private TextToSpeech textToSpeech;

    private FirebaseAuth myAuth;
    private DatabaseReference databaseReference;

    //used on scheduleLayout
    ListView sList;
    FloatingActionButton fab;
    int actCount = 0;

    //used on editScheduleLayout
    EditText activity_name, activity_desc;
    TextView  activity_sTime, activity_eTime;
    Button delBtn, saveBtn;
    String tempName;
    boolean confirm = true;
    boolean doRemove = true;
    int t1h, t1m, t2h, t2m;

    ArrayList<ScheduleClass> fileList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_schedule_fragment, container, false);

        radioGroup = view.findViewById(R.id.radioGroup);

        textToSpeech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                textToSpeech.setLanguage(Locale.US);
                //textToSpeech.setSpeechRate(0.75f);

            }
        });
        //Start of feedback
        ImageView TTS = view.findViewById(R.id.speak);
        TTS.setOnClickListener(this::TexttoSpeechButton);

        try {
            myAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference();

        } catch (Exception e) {
            e.printStackTrace();
        }

        scheduleLayout = view.findViewById(R.id.scheduleLayout);
        editScheduleLayout = view.findViewById(R.id.editScheduleLayout);

        sList = view.findViewById(R.id.scheduleList);
        fab = view.findViewById(R.id.addAct);

        fab.setOnClickListener(this::OnClick);
        sList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //load activity info in next layout
                delBtn.setText("DELETE");
                loadEdit(position);
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioButton = view.findViewById(checkedId);
                switch (checkedId) {
                    case R.id.monday:
                        theDay = "Monday";
                        break;
                    case R.id.tuesday:
                        theDay = "Tuesday";
                        break;
                    case R.id.wednesday:
                        theDay = "Wednesday";
                        break;
                    case R.id.thursday:
                        theDay = "Thursday";
                        break;
                    case R.id.friday:
                        theDay = "Friday";
                        break;
                    case R.id.saturday:
                        theDay = "Saturday";
                        break;
                    case R.id.sunday:
                        theDay = "Sunday";
                        break;
                }

                if (isView) {
                    loadSchedule();
                } else {
                    doRemove = false;
                }
            }


        });

        activity_sTime = view.findViewById(R.id.editSTime);
        activity_eTime = view.findViewById(R.id.editETime);
        activity_name = view.findViewById(R.id.editActName);
        activity_desc = view.findViewById(R.id.editActDesc);

        micButton = view.findViewById(R.id.speech);

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
                            .makeText(getActivity(), " " + e.getMessage(),
                                    Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        delBtn = view.findViewById(R.id.btnDelete);
        delBtn.setOnClickListener(this::OnClick);
        saveBtn = view.findViewById(R.id.btnSave);
        saveBtn.setOnClickListener(this::OnClick);
        activity_eTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        getContext(),
                        android.R.style.Theme_Holo_Dialog_MinWidth,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                t2h = hourOfDay;
                                t2m = minute;

                                String time = t2h + ":" + t2m;

                                SimpleDateFormat f24Hours = new SimpleDateFormat("HH:mm");

                                try {
                                    Date date = f24Hours.parse(time);

                                    activity_eTime.setText(f24Hours.format(date));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 24, 0, true
                );
                timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                timePickerDialog.updateTime(t2h,t2m);
                timePickerDialog.show();
            }
        });

        activity_sTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        getContext(),
                        android.R.style.Theme_Holo_Dialog_MinWidth,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                t1h = hourOfDay;
                                t1m = minute;

                                String time = t1h + ":" + t1m;

                                SimpleDateFormat f24Hours = new SimpleDateFormat("HH:mm");

                                try {
                                    Date date = f24Hours.parse(time);

                                    activity_sTime.setText(f24Hours.format(date));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 24, 0, true
                );
                timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                timePickerDialog.updateTime(t1h,t1m);
                timePickerDialog.show();
            }
        });

        loadSchedule();
        return view;
    }

    void loadSchedule() {
        actCount = 0;

        fileList = new ArrayList<>();

        databaseReference.child("schedule").child(myAuth.getCurrentUser().getUid()).child(theDay).child("file").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if (task.getResult().getValue() != null) {

                    for (DataSnapshot ds:task.getResult().getChildren()) {
                        Log.d("TEST", "LOOPING!" + actCount);
                        String fileName = ds.getKey();

                        String sTime = ds.child("startTime").getValue().toString();
                        String eTime = "";
                        if (ds.child("endTime").getValue() != null) {
                            eTime = ds.child("endTime").getValue().toString();
                        }

                        String aName = ds.child("name").getValue().toString();
                        String aDesc = "";
                        if (ds.child("description").getValue() != null) {
                            aDesc = ds.child("description").getValue().toString();
                        }

                        fileList.add(new ScheduleClass(fileName, sTime, eTime, aName, aDesc));
                        actCount++;
                    }
                    CustomAdapter customAdapter = new CustomAdapter();
                    sList.setAdapter(customAdapter);
                } else {
                    actCount = 0;

                    CustomAdapter customAdapter = new CustomAdapter();
                    sList.setAdapter(customAdapter);
                }
            }
        });


    }


    void loadEdit(int pos) {
        scheduleLayout.setVisibility(View.GONE);
        editScheduleLayout.setVisibility(View.VISIBLE);
        doRemove = true;
        isView = false;
        if (pos != -1) {

            tempName = fileList.get(pos).getFileName();

            activity_sTime.setText(fileList.get(pos).getsTime());
            activity_eTime.setText(fileList.get(pos).geteTime());
            activity_name.setText(fileList.get(pos).getaName());
            activity_desc.setText(fileList.get(pos).getaDesc());
        } else {
            tempName = "";
        }
    }

    void returnSchedule() {
        activity_sTime.setText("Select Start Time");
        activity_eTime.setText("Select End Time");
        activity_name.setText("");
        activity_desc.setText("");
        scheduleLayout.setVisibility(View.VISIBLE);
        editScheduleLayout.setVisibility(View.GONE);
        isView = true;

        loadSchedule();
    }

    void saveData() {
        String startTime = activity_sTime.getText().toString().trim();
        String endTime = activity_eTime.getText().toString().trim();
        String name = activity_name.getText().toString().trim();
        String description = activity_desc.getText().toString().trim();

        String madeName = startTime + name;

        if (doRemove) {
            if (!tempName.isEmpty() && !tempName.equals(madeName)) {
                databaseReference.child("schedule").child(myAuth.getCurrentUser().getUid()).child(theDay).child("file").child(tempName).removeValue();
                databaseReference.child("schedule").child(myAuth.getCurrentUser().getUid()).child(theDay).child("count").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.getResult().getValue() != null) {

                            databaseReference.child("schedule").child(myAuth.getCurrentUser().getUid()).child(theDay).child("count").setValue(Integer.valueOf(task.getResult().getValue().toString()) - 1);
                            actualSave();
                        }
                    }
                });
            }
        } else {
            actualSave();
        }


    }

    void actualSave() {
        String startTime = activity_sTime.getText().toString().trim();
        String endTime = activity_eTime.getText().toString().trim();
        String name = activity_name.getText().toString().trim();
        String description = activity_desc.getText().toString().trim();

        String madeName = startTime + name;

        try {
            DatabaseReference scheduleStore = databaseReference.child("schedule").child(myAuth.getCurrentUser().getUid()).child(theDay).child("file").child(madeName);
            scheduleStore.child("startTime").setValue(startTime);
            scheduleStore.child("endTime").setValue(endTime);
            scheduleStore.child("name").setValue(name);
            scheduleStore.child("description").setValue(description);

            databaseReference.child("schedule").child(myAuth.getCurrentUser().getUid()).child(theDay).child("count").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.getResult().getValue() != null) {
                        databaseReference.child("schedule").child(myAuth.getCurrentUser().getUid()).child(theDay).child("count").setValue(Integer.valueOf(task.getResult().getValue().toString()) + 1);
                    } else {
                        databaseReference.child("schedule").child(myAuth.getCurrentUser().getUid()).child(theDay).child("count").setValue(1);
                    }
                    returnSchedule();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity().getApplicationContext(), "Activity couldn't be added, try again later!", Toast.LENGTH_SHORT);
        }
    }

    public void OnClick(View v) {
        switch (v.getId()) {
            case R.id.addAct:
                delBtn.setText("CANCEL");
                loadEdit(-1);
                break;
            case R.id.btnDelete:
                if (!tempName.isEmpty()) {
                    databaseReference.child("schedule").child(myAuth.getCurrentUser().getUid()).child(theDay).child("file").child(tempName).removeValue();
                    databaseReference.child("schedule").child(myAuth.getCurrentUser().getUid()).child(theDay).child("count").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.getResult().getValue() != null) {

                                int counting = Integer.valueOf(task.getResult().getValue().toString()) - 1;

                                databaseReference.child("schedule").child(myAuth.getCurrentUser().getUid()).child(theDay).child("count").setValue(counting);
                                returnSchedule();
                            }
                        }
                    });

                }
                break;
            case R.id.btnSave:
                confirm = true;
                String startTime = activity_sTime.getText().toString().trim();
                String name = activity_name.getText().toString().trim();

                if (startTime.equals("Select Start Time")) {
                    activity_sTime.setError("Start time can't be empty!");
                    activity_sTime.requestFocus();
                    return;
                }

                if (name.isEmpty()) {
                    activity_name.setError("Name can't be empty!");
                    activity_name.requestFocus();
                    return;
                }

                String madeName = startTime + name;

                databaseReference.child("schedule").child(myAuth.getCurrentUser().getUid()).child(theDay).child("file").child(madeName).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.getResult().getValue() != null) {
                            //databaseReference.child("schedule").child(myAuth.getCurrentUser().getUid()).child(theDay).child("count").removeValue();
                            confirm = false;
                            new AlertDialog.Builder(getActivity())
                                    .setTitle("Update/Replace")
                                    .setMessage("Activity with identical start time and name already exist, continue with update/replace?")
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            //Toast.makeText(MainActivity.this, "Yaay", Toast.LENGTH_SHORT).show();
                                            saveData();
                                        }})
                                    .setNegativeButton(android.R.string.no, null).show();
                        }
                        if (confirm) {
                            saveData();
                        }
                    }
                });
                break;
        }
    }


    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return actCount;
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
            View view = getLayoutInflater().inflate(R.layout.schedulelayout, null);
            TextView actStartTime = view.findViewById(R.id.editStartTime);
            TextView actEndTime = view.findViewById(R.id.editEndTime);
            TextView actName = view.findViewById(R.id.editTextName);
            TextView actDesc = view.findViewById(R.id.editTextDesc);

            actStartTime.setText(fileList.get(position).getsTime());
            if (fileList.get(position).geteTime().equals("Select End Time")) {
                actEndTime.setText("No end time!");
                actEndTime.setTextSize(18);
            } else {
                actEndTime.setText(fileList.get(position).geteTime());
                actEndTime.setTextSize(30);
            }
            actName.setText(fileList.get(position).getaName());
            actDesc.setText(fileList.get(position).getaDesc());
            return view;
        }
    }

    public void TexttoSpeechButton(View view) {
        if(!textToSpeech.isSpeaking()) {
            textToSpeech.speak("This is the Schedule page... You can choose between the seven radio buttons each representing a day of the week... You can create a schedule by pressing the + button on the bottom right... Enter the name time and description then click save to add an activity to the schedule.", TextToSpeech.QUEUE_FLUSH, null, null);
        }
        else {
            Toast.makeText(getActivity(), "System Busy", Toast.LENGTH_SHORT).show();
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
                    .makeText(getActivity(), " " + e.getMessage(),
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                if (pointer == 0) {
                    activity_name.setText(Objects.requireNonNull(result).get(0));
                    SpeechtoInfo();
                    return;
                }
                if (pointer == 1) {
                    activity_desc.setText(Objects.requireNonNull(result).get(0));
                    return;
                }


            }
        }
    }
}