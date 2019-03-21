package com.th_koeln.steve.klamottenverteiler;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Nach dem erfolgreichen Ablauf einer Transaktion, bietet diese Klasse den Transaktonspartnern
 * sich gegenseitig zu bewerten. Hierfür wird dem Benutzer eine Eingabemaske mit der Möglichkeit
 * seine Bewertung zu konkretisieren geboten. Nachdem der Benutzer die erforderlichen Daten
 * eingetragen hat, werden die Daten ins JSON Dateiformat überführt und zum Server gesendet.
 *
 * Created by Steffen Owtschinnikow on 10.01.2018.
 */

public class RateUser extends AppCompatActivity {

    private EditText txtComment;
    private Spinner spinChooseRating;
    private Button btnSendRating;
    private TextView txtTransactionDetails;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final String uId= firebaseAuth.getCurrentUser().getUid();
    private JSONObject rating = new JSONObject();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        txtComment = (EditText) findViewById(R.id.txtComment);
        spinChooseRating = (Spinner) findViewById(R.id.spinChooseRating);
        btnSendRating = (Button) findViewById(R.id.btnSendRating);
        txtTransactionDetails = (TextView) findViewById(R.id.txtTransactionDetails);


        ArrayAdapter<String> aArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item);
        aArrayAdapter.add("Good");
        aArrayAdapter.add("Okay");
        aArrayAdapter.add("Bad");

        spinChooseRating.setAdapter(aArrayAdapter);

        String tId = getIntent().getStringExtra("tId");
        final String request = getIntent().getStringExtra("request");

        txtTransactionDetails.append(tId);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("RATEUSER"));

        btnSendRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String tId = getIntent().getStringExtra("tId");
                    String ouId = getIntent().getStringExtra("ouId");
                    String rFrom = getIntent().getStringExtra("rFrom");
                    String finished = getIntent().getStringExtra("finished");

                    // Speicher Zeitpunkt der Bewertung
                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    Date now = new Date();

                    String strDate = sdfDate.format(now);
                    Date date = sdfDate.parse(strDate);

                    // Erstelle Datenstruktur für die aktuelle Bewertung
                    rating.put("choice", spinChooseRating.getSelectedItem().toString());
                    rating.put("comment", txtComment.getText().toString());
                    rating.put("time",date.getTime() );
                    rating.put("from", uId);
                    rating.put("tId", tId);
                    rating.put("rFrom",rFrom);
                    rating.put("finished",finished);

                    // Sende Bewertung zum Server
                    Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                    myIntent.putExtra("payload",rating.toString());
                    myIntent.putExtra("method","POST");
                    myIntent.putExtra("from","POSTRATING");
                    myIntent.putExtra("url",getString(R.string.DOMAIN) + "/user/" + ouId + "/rating");
                    startService(myIntent);
                } catch (JSONException e) {
                    showDialog("Error", "Could not process your entries!");
                } catch (ParseException e) {
                    showDialog("Error", "Could not process your entries!");
                }
            }
        });

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get clothing results from HTTP-Service

            String from = intent.getStringExtra("from");
            if (from.equals("POSTRATINGFAIL")) {
                // Bewertung konnte nicht eingetragen werden
                showDialog("Error","Could not add rating to Server!");
            } else if (from.equals("POSTRATING")) {
                // Bewertung erfolgreich eingetragen
                showDialog("Success","Successfully added rating!");
            }

        }
    };


    private void showDialog(String title, String message) {

        AlertDialog alertDialog = new AlertDialog.Builder(RateUser.this).create();
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
}
