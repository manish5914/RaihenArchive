package com.example.raihenv2;

public class ScheduleClass implements Comparable<ScheduleClass> {
    private String fileName = "";
    private String sTime = "";
    private String eTime = "";
    private String aName = "";
    private String aDesc = "";

    public ScheduleClass(String fileName, String sTime, String eTime, String aName, String aDesc) {
        this.fileName =fileName;
        this.sTime = sTime;
        this.eTime = eTime;
        this.aName = aName;
        this.aDesc = aDesc;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getsTime() {
        return sTime;
    }

    public void setsTime(String sTime) {
        this.sTime = sTime;
    }

    public String geteTime() {
        return eTime;
    }

    public void seteTime(String eTime) {
        this.eTime = eTime;
    }

    public String getaName() {
        return aName;
    }

    public void setaName(String aName) {
        this.aName = aName;
    }

    public String getaDesc() {
        return aDesc;
    }

    public void setaDesc(String aDesc) {
        this.aDesc = aDesc;
    }

    @Override
    public int compareTo(ScheduleClass o) {
        return this.fileName.compareTo(o.fileName);
    }

}
