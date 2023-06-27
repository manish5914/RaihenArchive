package com.example.raihenv2;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Event implements Comparable{
    public String eventName, eventInfo, eventType, eventColor;
    public int eventNumber;
    public Event(){

    }
    public Event(int currentNumber, String eventName, String eventInfo, String eventType, String eventColor){
        this.eventNumber = currentNumber;
        this.eventName = eventName;
        this.eventInfo = eventInfo;
        this.eventType = eventType;
        this.eventColor = eventColor;
    }
    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> data = new HashMap<>();
        data.put("EventNumber", eventNumber);
        data.put("EventName", eventName);
        data.put("EventInfo", eventInfo);
        data.put("EventType", eventType);
        data.put("EventColor", eventColor);
        return data;
    }

    @Override
    public int compareTo(Object o) {
        return  ((Event)o).eventNumber - this.eventNumber;
    }
}
