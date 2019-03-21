package com.th_koeln.steve.klamottenverteiler;

/**
 * Zum Editieren des eigenen Profils wird das jeweilige Profil zunächst vom Server geholt und
 * dem Benutzer Praesentiert. Zum Bearbeiten von Uhrzeiten kommt ein TimePickerDialog zum Einsatz.
 * Nachdem der Benutzer die gewuenschte Attribute angepasst hat, wird eine JSON Datenstruktur mit
 * den zu aendernden Attributen und deren neuen Werte angelegt
 * und ueber den HTTPs Service zum Server weitergeleitet.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class EditProfile extends AppCompatActivity implements View.OnClickListener {

    private Spinner spinnerGender;
    private Button btnSendData;
    private TextView textViewMyID;
    private String[] genderArr = new String[]{"Geschlecht wählen","Weiblich","Männlich"};

    private Button btnTimeFromWeekday;
    private Button btnTimeToWeekday;
    private Button btnTimeFromWeekend;
    private Button btnTimeToWeekend;

    private String txtWeekTimeBegin = "00:00";
    private String txtWeekTimeEnd = "00:00";
    private String txtWeekendTimeBegin = "00:00";
    private String txtWeekendTimeEnd = "00:00";

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final String uId = firebaseAuth.getCurrentUser().getUid();

    //Variablen fuer TimePicker
    private int DIALOG_ID = -1;
    private int hourPick, minutePick;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_2);

        spinnerGender = (Spinner) findViewById(R.id.spinnerGender);
        textViewMyID = (TextView) findViewById(R.id.textViewMyID);

        btnSendData = (Button) findViewById(R.id.btnSendData);
        btnSendData.setOnClickListener(this);

        btnTimeFromWeekend = (Button) findViewById(R.id.btnTimeFromWeekend);
        btnTimeFromWeekend.setOnClickListener(this);

        btnTimeToWeekend = (Button) findViewById(R.id.btnTimeToWeekend);
        btnTimeToWeekend.setOnClickListener(this);

        btnTimeFromWeekday = (Button) findViewById(R.id.btnTimeFromWeekday);
        btnTimeFromWeekday.setOnClickListener(this);

        btnTimeToWeekday = (Button) findViewById(R.id.btnTimeToWeekday);
        btnTimeToWeekday.setOnClickListener(this);

        ArrayAdapter<String> gAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, genderArr){
            @Override
            public boolean isEnabled(int position){
                if(position==0){return false;}
                else{return true;}
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent){
                View v = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) v;
                if(position==0){tv.setTextColor(Color.GRAY);}
                else{tv.setTextColor(Color.BLACK);}
                return v;
            }
        };
        spinnerGender.setAdapter(gAdapter);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("profile"));

        // Hole Profil-Informationen vom Server
        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        myIntent.putExtra("payload","");
        myIntent.putExtra("method","GET");
        myIntent.putExtra("from","PROFILE");
        myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId);
        startService(myIntent);

        progressDialog = new ProgressDialog(this);

        progressDialog.setMessage("Trying to get your profile..\n");
        progressDialog.show();
    }

    @Override
    protected Dialog onCreateDialog(int id){
        if(id == DIALOG_ID){
            return new TimePickerDialog(EditProfile.this, kTimePickerListener, hourPick, minutePick, true);
        }
        return null;
    }

    protected TimePickerDialog.OnTimeSetListener kTimePickerListener = new TimePickerDialog.OnTimeSetListener(){
        @Override
        public void onTimeSet(TimePicker timePicker, int i, int i1) {

            String showHour = i+"";
            String showMinute = i1+"";

            if(i<10){showHour = "0"+i;}
            if(i1<10){ showMinute = "0"+i1;}

            switch(DIALOG_ID){
                //TimeFromWeekday
                case 1: btnTimeFromWeekday.setText("Von "+showHour+":"+showMinute);
                    txtWeekTimeBegin = showHour+":"+showMinute;
                    break;
                //TimeToWeekday
                case 2: btnTimeToWeekday.setText("Bis "+showHour+":"+showMinute);
                    txtWeekTimeEnd = showHour+":"+showMinute;
                    break;
                //TimeFromWeekend
                case 3: btnTimeFromWeekend.setText("Von "+showHour+":"+showMinute);
                    txtWeekendTimeBegin = showHour+":"+showMinute;
                    break;
                //TimeToWeekend
                case 4: btnTimeToWeekend.setText("Bis "+showHour+":"+showMinute);
                    txtWeekendTimeEnd = showHour+":"+showMinute;
                    break;
            }
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get clothing results from HTTP-Service
            String from = intent.getStringExtra("from");

            if (from.equals("SEARCHPROFILEFAIL")) {
                progressDialog.dismiss();
                showDialog("Error","Could not get your profile.");

            } else if (from.equals("SEARCHPROFILE")) {
                progressDialog.dismiss();

                String profile = intent.getStringExtra("profile");
                try {
                    JSONObject profileJson = new JSONObject(profile);
                    if(profileJson.has("time")) {
                        JSONObject timeJson = new JSONObject(profileJson.getString("time"));
                        btnTimeFromWeekday.setText("Von " + timeJson.getString("txtWeekTimeBegin"));
                        txtWeekTimeBegin = timeJson.getString("txtWeekTimeBegin");
                        btnTimeToWeekday.setText("Bis " + timeJson.getString("txtWeekTimeEnd"));
                        txtWeekTimeEnd = timeJson.getString("txtWeekTimeEnd");
                        btnTimeFromWeekend.setText("Von " + timeJson.getString("txtWeekendTimeBegin"));
                        txtWeekendTimeBegin = timeJson.getString("txtWeekendTimeBegin");
                        btnTimeToWeekend.setText("Bis " + timeJson.getString("txtWeekendTimeEnd"));
                        txtWeekendTimeEnd = timeJson.getString("txtWeekendTimeEnd");
                    }
                    if(!profileJson.getString("gender").equals("?")){
                        switch(profileJson.getString("gender")){
                            case "W":
                                spinnerGender.setSelection(1);
                                break;
                            case "M":
                                spinnerGender.setSelection(2);
                                break;
                            default:
                                spinnerGender.setSelection(0);
                                break;
                        }
                    }

                    if(profileJson.has("uId")){textViewMyID.setText(profileJson.getString("uId"));}

                } catch (JSONException e) {
                    showDialog("Error","Profile data is not valid..");
                }
            } else if (from.equals("PUTPROFILE")){
                if (!isFinishing())
                    showDialog("Success","Updated profile data");
            }

        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnSendData:
                JSONObject profile = new JSONObject();
                try {

                    //Das Geschlecht
                    int tmpG = spinnerGender.getSelectedItemPosition();
                    switch (tmpG){
                        case 0:
                            break;
                        case 1:
                            profile.put("gender","W");
                            break;
                        case 2:
                            profile.put("gender", "M");
                            break;
                    }

                    if(!txtWeekendTimeBegin.equals(txtWeekendTimeEnd) &&
                            !txtWeekTimeBegin.equals(txtWeekTimeEnd)) {
                        //Die Uhrzeiten
                        JSONObject time = new JSONObject();
                        time.put("txtWeekTimeBegin", txtWeekTimeBegin);
                        time.put("txtWeekTimeEnd", txtWeekTimeEnd);
                        time.put("txtWeekendTimeBegin", txtWeekendTimeBegin);
                        time.put("txtWeekendTimeEnd", txtWeekendTimeEnd);
                        profile.put("time", time);
                    }

                    // Sende Attribute zum HTTPSService
                    Intent upIntent = new Intent(getApplicationContext(), HttpsService.class);
                    upIntent.putExtra("payload",profile.toString());
                    upIntent.putExtra("method","PUT");
                    upIntent.putExtra("from","PUTPROFILE");
                    upIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + uId);
                    startService(upIntent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btnTimeFromWeekday:
                DIALOG_ID = 1;
                showDialog(DIALOG_ID);
                break;

            case R.id.btnTimeToWeekday:
                DIALOG_ID = 2;
                showDialog(DIALOG_ID);
                break;

            case R.id.btnTimeFromWeekend:
                DIALOG_ID = 3;
                showDialog(DIALOG_ID);
                break;

            case R.id.btnTimeToWeekend:
                DIALOG_ID = 4;
                showDialog(DIALOG_ID);
                break;

        }
    }

    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(EditProfile.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        if (!isFinishing())
            alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}