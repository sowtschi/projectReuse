package com.th_koeln.steve.klamottenverteiler.structures;

import java.util.Date;

/**
 * Created by Michael on 10.01.2018.
 */

//Klasse um Objekte zu erstellen, die fuer die Transaktion benoetigt werden
public class myTransaktion {
    //ID des Users, Angebotes. Titel des Objekts.
    String uID, cID, titel;

    //Ort des Angebotes
    double longitude = -200;
    double latitude = -200;

    //Zeit Von Bis an Wochentagen
    Date timeFromWorkday = null;
    Date timeToWorkday = null;
    //Zeit Von Bis am Wochenende
    Date timeFromWeekend = null;
    Date timeToWeekend = null;

    //Zeitpunkt zum abholen
    Date timeToGet = null;

    //Distanz vom letzten Punkt zu diesem in Meter
    long distanceToMeFromLast = -1;
    //Zeit vom letzten Punkt zu diesem in Sekunden
    long timeToMeFromLast = -1;

    public myTransaktion(String userID, String clothingID){
        uID = userID;
        cID = clothingID;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public String getuID(){
        return uID;
    }

    public void setuID(String userID){
        uID = userID;
    }

    public String getcID(){
        return cID;
    }

    public void setcID(String clothingID){
        cID = clothingID;
    }

    public double getLongitude(){
        return longitude;
    }

    public void setLongitude(double lng){
        longitude = lng;
    }

    public double getLatitude(){
        return latitude;
    }

    public void setLatitude(double lat){
        latitude = lat;
    }

    public Date getTimeFromWorkday(){
        return timeFromWorkday;
    }

    public String getTimeFromWorkdayString(){
        String hours="99",minutes="99";
        if(timeFromWorkday!=null) {
            hours = checkHoursMinutes(timeFromWorkday.getHours() + "");
            minutes = checkHoursMinutes(timeFromWorkday.getMinutes() + "");
        }
        return hours+":"+minutes;
    }

    public void setTimeFromWorkday(Date tfWork){
        timeFromWorkday = tfWork;
    }

    public Date getTimeToWorkday(){
        return timeToWorkday;
    }

    public String getTimeToWorkdayString(){
        String hours="99",minutes="99";
        if(timeToWorkday!=null) {
            hours = checkHoursMinutes(timeToWorkday.getHours() + "");
            minutes = checkHoursMinutes(timeToWorkday.getMinutes() + "");
        }
        return hours+":"+minutes;
    }

    public void setTimeToWorkday(Date ttWork){
        timeToWorkday = ttWork;
    }

    public Date getTimeFromWeekend(){
        return timeFromWeekend;
    }

    public String getTimeFromWeekendString(){
        String hours="99",minutes="99";
        if(timeFromWeekend!=null) {
            hours = checkHoursMinutes(timeFromWeekend.getHours() + "");
            minutes = checkHoursMinutes(timeFromWeekend.getMinutes() + "");
        }
        return hours+":"+minutes;
    }

    public void setTimeFromWeekend(Date tfWeek){
        timeFromWeekend = tfWeek;
    }

    public Date getTimeToWeekend(){
        return timeToWeekend;
    }

    public String getTimeToWeekendString(){
        String hours="99",minutes="99";
        if(timeToWeekend!=null) {
            hours = checkHoursMinutes(timeToWeekend.getHours() + "");
            minutes = checkHoursMinutes(timeToWeekend.getMinutes() + "");
        }
        return hours+":"+minutes;
    }

    public void setTimeToWeekend(Date ttWeek){
        timeToWeekend = ttWeek;
    }

    public Date getTimeToGet(){
        return timeToGet;
    }

    public String getTimeToGetString(){
        String hours="",minutes="";
        if(timeToGet!=null) {
            hours = checkHoursMinutes(timeToGet.getHours() + "");
            minutes = checkHoursMinutes(timeToGet.getMinutes() + "");
        }
        return hours+":"+minutes;
    }

    public void setTimeToGet(Date ttG){
        timeToGet = ttG;
    }

    public void setDistanceToMeFromLast(long setDis){
        distanceToMeFromLast = setDis;
    }

    public long getDistanceToMeFromLast(){
        return distanceToMeFromLast;
    }

    public void setTimeToMeFromLast(long setTime){
        timeToMeFromLast = setTime;
    }

    public long getTimeToMeFromLast(){
        return timeToMeFromLast;
    }

    public String checkHoursMinutes(String checkThis){
        if(Integer.parseInt(checkThis)<10){checkThis = "0"+checkThis;}
        return checkThis;
    }

}
