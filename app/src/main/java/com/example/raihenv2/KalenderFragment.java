package com.example.raihenv2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class KalenderFragment extends Fragment {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public String currentDate;
    private String currentUserUid = mAuth.getCurrentUser().getUid();
    private Event[] emptyArray = new Event[0];
    String Ktype = "kalender";
    View tempView = getView();
    private Random randomGenerator = new Random();
    private ListView listview;
    private Boolean eventKal;
    private TextToSpeech textToSpeech;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_kalender, container, false);
        TextView title = (TextView) view.findViewById(R.id.currentDate);
        CalendarView kalender = (CalendarView) view.findViewById(R.id.kalenderOnScreen);
        //when user selects new date
        if(currentDate == null) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            currentDate = sdf.format(cal.getTime());
        }
        kalender.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
                //formatting date
                String date = String.format("%02d", i2)  + "-" + String.format("%02d", i1+1) + "-" + String.valueOf(i);
                currentDate = date;
                title.setText("Date Selected: " + date);
                //LinearLayout layout = (LinearLayout) getView().findViewById(R.id.list);
                //clear linear layout for new item
                //layout.removeAllViews();
                ListView listView = (ListView) getView().findViewById(R.id.listv2);
                listView.setAdapter(new MyAdapter(getView().getContext(),new Event[0], Ktype));
                fillEvents(getView(), currentDate, "kalender");
            }
        });

        title.setText("Date Selected: " + currentDate);
        textToSpeech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                textToSpeech.setLanguage(Locale.US);

            }
        });
        //Start of feedback
        ImageView TTS = view.findViewById(R.id.speak);
        TTS.setOnClickListener(this::TexttoSpeechButton);
        return view;
    }
    @Override
    public void onStart() {
        super.onStart();

        FloatingActionButton button = (FloatingActionButton) getView().findViewById(R.id.floatingActionButton);
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //function to add Data
                //setData();
                Intent intent = new Intent(getActivity(), AddEvent.class);
                intent.putExtra("DATE", currentDate);
                startActivity(intent);
            }
        });
        fillEvents(this.getView(), currentDate, "kalender");

    }
    //get number of textviews to be created and calling the function to create them
    public void fillEvents(View v, String date, String type){
        /*
        LinearLayout layout = (LinearLayout) getView().findViewById(R.id.list);
        if(layout.getChildCount()!= 0){
            layout.removeAllViews();
        }*/

        this.Ktype = type;
        this.tempView = v;
        currentDate = date;
        ListView listView;


        if(type == "kalender"){
            listView = (ListView) getView().findViewById(R.id.listv2);
        }else if(type =="today"){
            HomeFragment.today = true;
            listView = (ListView) v.findViewById(R.id.listT);
        }
        else{
            HomeFragment.tom = true;
            listView = (ListView) v.findViewById(R.id.listTom);
        }
        this.listview = listView;
       // ListView listView = (ListView) getView().findViewById(R.id.listv2);
        if(listView.getChildCount() != 0){
            listView.setAdapter(new MyAdapter(v.getContext(),new Event[0], Ktype));

        }

        try{
            FirebaseDatabase.getInstance().getReference("Events").child(currentUserUid).keepSynced(true);
            FirebaseDatabase.getInstance().getReference("Events").child(currentUserUid).child("stats").child(currentDate).child("numberEvents").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    try {
                        if(task.getResult().getValue() != null)
                        {

                            long num = (long)task.getResult().getValue();
                            int c = 0;
                            c = (int) num;
                            //Log.d("eh", "in else c: "+ c + " task " +task.getResult().getValue().toString());
                            if (c != 0){
                                if(type =="today"){
                                    HomeFragment.today = false;
                                }
                                else if (type == "nextday"){
                                    HomeFragment.tom = false;
                                }
                                else if (type == "kalender")
                                {
                                    eventKal = false;
                                }
                                noEvents();
                                createTextViews(v, c, type);
                            }
                            else{
                                check(type);
                                noEvents();
                            }
                        }
                        else{
                            check(type);
                            noEvents();
                        }
                    }
                    catch(Exception e)
                    {
                        Log.d("Error Found A", e.toString());
                        if(Ktype == "kalender"){
                            Toast.makeText(getContext(), "No internet Connection Found", Toast.LENGTH_LONG).show();
                        }
                    }
                    HomeFragment.manish.setChecked(true);
                }
            });

        }
        catch (Exception e)
        {
            Log.d("Error Found B", e.toString());
            Toast.makeText(this.getContext(),"No internet Connection Found", Toast.LENGTH_LONG).show();
        }

    }
    public void check(String type){
        if(type =="today") {
            HomeFragment.today = true;
        }
        else if (type == "nextday"){
            HomeFragment.tom = true;
        }
        else if (type == "kalender"){
            eventKal = true;
        }
    }
    public void noEvents(){
        //ArrayList<String> noEvent = new ArrayList<String>();
        String[] noEvent;
        Resources res = tempView.getResources();
        noEvent = res.getStringArray(R.array.ehh);

        int index = randomGenerator.nextInt(noEvent.length);
        String[] temp = new String[1];

        temp[0] = noEvent[index];
        //Log.d("try something" , "just " + HomeFragment.today + " /  " + HomeFragment.tom);

        ListView lv = tempView.findViewById(R.id.list_empty);
        ListView lt = tempView.findViewById(R.id.listT);
        ListView ltom = tempView.findViewById(R.id.listTom);
        ArrayAdapter<String> dpemp = new ArrayAdapter<String>(tempView.getContext(), R.layout.simple_text_view, temp);

        if(Ktype != "kalender"){
            if (HomeFragment.tom && HomeFragment.today ){
                lv.setAdapter(dpemp);

            }
            else{
                ArrayAdapter<String> dp = new ArrayAdapter<String>(tempView.getContext(), R.layout.simple_text_view, new String[0]);
                lv.setAdapter(dp);
            }
        }
        else{
            if(eventKal){
                listview.setAdapter(dpemp);
            }

        }
    }
    public int currentEventNumber = 0;
    //creating the textviews to add to linear layout
    public void createTextViews(View v ,int numEvents, String type){


        ArrayList<String> eveName = new ArrayList<>();
        ArrayList<String> eveInfo = new ArrayList<>();





        ArrayList<Event> events = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Events/"+currentUserUid+"/"+currentDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get all data at current date
                if(snapshot.getValue() != null){
                    fillEvents((Map<String, Object>)snapshot.getValue());
                    Event[] eve = new Event[events.size()];
                    Collections.sort(events);
                    //FirebaseDatabase.getInstance().getReference("testSomething").setValue(events);
                    for(int i = 0 ; i< eveName.size(); i++){
                        eve[i] = events.get(i);
                    }
                    listview.setAdapter(new MyAdapter(v.getContext(), eve, type));
                }


            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("database", "no data found");
            }
            public void fillEvents(Map<String, Object> details){
                if(details != null){
                    for (Map.Entry<String, Object> entry: details.entrySet()){
                        //take one item from tree/map
                        Map event = (Map) entry.getValue();
                        eveName.add((String)event.get("EventName"));
                        eveInfo.add((String)event.get("EventInfo"));
                        Long eventNumber = (long) event.get("EventNumber");

                        events.add(new Event(eventNumber.intValue(), (String)event.get("EventName"), (String)event.get("EventInfo"), (String)event.get("EventType"), (String)event.get("EventColor")));

                    }
                }
                else{Log.d("Kalender", "in else no data found");}
            }
        });

    }
    //fucntion to delete event in firebase using event id and current date
    public void removeEvent(int currentNum){
        Map<String, Object> updateCurrentNumber = new HashMap();
        try{
            FirebaseDatabase.getInstance().getReference("Events").child(currentUserUid).child("stats").child(currentDate).child("numberEvents").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> snapshot)
                {
                    try{
                        Long convertli = (long)snapshot.getResult().getValue();
                        int temp = convertli.intValue() - 1 ;

                        updateCurrentNumber.put("Events/"+currentUserUid+"/stats/"+currentDate+"/numberEvents", temp);
                        updateCurrentNumber.put("Events/"+currentUserUid+"/"+currentDate+"/Event-"+(currentNum), null);
                        FirebaseDatabase.getInstance().getReference().updateChildren(updateCurrentNumber).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                try{
                                    //LinearLayout layout = (LinearLayout) getView().findViewById(R.id.list);
                                    //layout.removeAllViews();
                                    ListView listView;
                                    listView = (ListView) getView().findViewById(R.id.listv2);
                                    listView.setAdapter(new MyAdapter(tempView.getContext(),new Event[0], Ktype));
                                    Toast.makeText(getActivity(), "Removed Event Number: "+ (currentNum) , Toast.LENGTH_SHORT).show();
                                    fillEvents( getView() ,currentDate, "kalender");
                                    //getActivity().recreate();
                                    Log.d("remove",  " " + convertli.intValue() + " " + currentNum );
                                }
                                catch (Exception e)
                                {
                                    Log.d("Error Found", e.toString());
                                    Toast.makeText(getContext(),"No internet Connection Found", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        Log.d("Error Found", e.toString());
                        Toast.makeText(getContext(),"No internet Connection Found", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        catch (Exception e)
        {
            Log.d("Error Found", e.toString());
            Toast.makeText(this.getContext(),"No internet Connection Found", Toast.LENGTH_LONG).show();
        }
    }
    //function to go to edit event page and sending current event number and current date as intention
    public void gotoEditEvent(int currentNum){
        Intent intent = new Intent(tempView.getContext(), EditEvent.class);
        intent.putExtra("date", currentDate);
        intent.putExtra("eventNumber", currentNum);
        Log.d("check", "Starting Event");
        startActivity(intent);
    }
    public void shareEvent(int currentNum){
        Intent intent = new Intent(tempView.getContext(), ShareEvent.class);
        intent.putExtra("date", currentDate);
        intent.putExtra("eventNumber", currentNum);
        Log.d("check", "Starting Event");
        startActivity(intent);
    }
    class MyAdapter extends BaseAdapter {
        LayoutInflater inflater;
        Context context;
        Event[] event;
        String type;

        public MyAdapter(Context context, Event[] event, String type){
            this.context = context;
            this.event = event;
            this.type = type;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        };

        @Override
        public int getCount() {

            return event.length;
        }

        @Override
        public Object getItem(int position) {
            return event[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder vh = new ViewHolder();

            if (view == null) {
                if (type == "kalender")
                    view = inflater.inflate(R.layout.event_detail, null);
                else view = inflater.inflate(R.layout.event_home, null);
            } else {
                view.setTag(view);
            }

            vh.nm = (TextView) view.findViewById(R.id.eveName);
            vh.tp = (TextView) view.findViewById(R.id.eveType);
            vh.dsc = (TextView) view.findViewById(R.id.eveDesc);



            vh.nm.setText(event[position].eventName);
            vh.tp.setText(event[position].eventType);
            vh.dsc.setText(event[position].eventInfo);
            vh.curNum = event[position].eventNumber;


            //vh.cir.setForeground(R.drawable.circle);
            vh.list = (LinearLayout) view.findViewById(R.id.list);
            if (type == "kalender") {
                vh.cir = (ImageView) view.findViewById(R.id.event_color);
                vh.cir.setBackgroundColor(Color.parseColor(event[position].eventColor));
                vh.list.setOnClickListener(new View.OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        //create array of items to be displayed in dialog box
                        String[] items = {"Edit", "Remove", "Share"};
                        TextView uhh = (TextView) view.findViewById(R.id.eveName);

                        //building dialog box
                        AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
                        alert.setTitle("").setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 1) {
                                    removeEvent(vh.curNum);//uhh.getId());
                                } else if (i == 0) {
                                    gotoEditEvent(vh.curNum);//uhh.getId());
                                }
                                else if(i==2){
                                    shareEvent(vh.curNum);
                                }
                                //Toast.makeText(getActivity(), "Current Item is " + i + " id= " + uhh.getId() , Toast.LENGTH_SHORT).show();
                            }
                        });

                        //creating dialog box and displaying it
                        AlertDialog box = alert.create();
                        box.show();
                    }
                });
            }
            else{
                vh.cir = (ImageView) view.findViewById(R.id.event_color2);
               try{
                   vh.cir.setBackgroundColor(Color.parseColor(event[position].eventColor));

               }
               catch (Exception e)
               {
                   Log.d("Error Found", e.toString());

               }


            }
            return view;
        }
    }
    private class ViewHolder {
        TextView tp, nm, dsc;
        LinearLayout list;
        ImageView cir;
        int curNum;
    }

    public void TexttoSpeechButton(View view)
    {
        if(!textToSpeech.isSpeaking()) {
            textToSpeech.speak("This is the Calendar page,  You can view your current events for all days   and add new ones via the + button on the bottom right.     You can also edit or delete current events by pressing on them on the bottom of the screen  ", TextToSpeech.QUEUE_FLUSH, null, null);
        }
        else {
            Toast.makeText(getActivity(), "System Busy", Toast.LENGTH_SHORT).show();
        }
    }
}

