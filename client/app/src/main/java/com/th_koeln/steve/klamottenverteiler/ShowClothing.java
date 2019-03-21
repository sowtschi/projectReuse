package com.th_koeln.steve.klamottenverteiler;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.th_koeln.steve.klamottenverteiler.R;
import com.th_koeln.steve.klamottenverteiler.services.HttpsService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Steffen Owtschinnikow on 25.12.2017.
 * */

public class ShowClothing extends AppCompatActivity implements View.OnClickListener{

    private String clothingID;
    private String clothing;
    private String ouId = null;
    private double lat, lng;

    private ImageView imgShowClothingPicture;

    private TextView titleTextView, textViewArtDetail ,
            textViewSizeDetail, textViewGenderDetail,
            textViewStyleDetail, textViewFabricDetail,
            textViewColorDetail, textViewBrandDetail;

    private Button btnShowLocation, btnSendRequest;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showclothing_2);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        ouId = firebaseAuth.getCurrentUser().getUid();

        imgShowClothingPicture=(ImageView) findViewById(R.id.imgShowClothingPicture);
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        textViewArtDetail = (TextView) findViewById(R.id.textViewArtDetail);
        textViewSizeDetail = (TextView) findViewById(R.id.textViewSizeDetail);
        textViewGenderDetail = (TextView) findViewById(R.id.textViewGenderDetail);
        textViewStyleDetail = (TextView) findViewById(R.id.textViewStyleDetail);
        textViewFabricDetail = (TextView) findViewById(R.id.textViewFabricDetail);
        textViewColorDetail = (TextView) findViewById(R.id.textViewColorDetail);
        textViewBrandDetail = (TextView) findViewById(R.id.textViewBrandDetail);

        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        btnShowLocation.setOnClickListener(this);
        btnSendRequest = (Button) findViewById(R.id.btnSendRequest);
        btnSendRequest.setOnClickListener(this);
        clothingID = getIntent().getStringExtra("clothingID");

        //ID der Kleidung wird geholt
        IntentFilter filter = new IntentFilter();
        filter.addAction("showdetails");
        filter.addAction("showclothing");
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,filter);

        //Kleidungsdaten werden geholt
        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
        myIntent.putExtra("method","GET");
        myIntent.putExtra("from","SHOWDETAILS");
        myIntent.putExtra("url",getString(R.string.DOMAIN) + "/clothing/" + clothingID);
        startService(myIntent);


    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String from = intent.getStringExtra("from");

            // Fehler beim erstellen des Requests
            if (from.equals("NEWREQUEST")) {
                showDialog("Success", "Successfully added Request. Pls wait for partner to confirm.");
            }
            if (from.equals("NEWREQUESTFAIL")) {
                showDialog("Error","Could not add request! Already requested?");
            }
            //Fehler beim holen der Kleidungsstücke
            if (from.equals("SHOWDETAILSFAIL")) {
                showDialog("Error","Could not get clothing from Server!");
            }
            // angeforderte Kleidungsstücke verarbeiten
            if(from.equals("SHOWDETAILS")){
                try {
                    clothing = intent.getStringExtra("clothing");
                    JSONObject request = new JSONObject(clothing);

                    // Bild Daten auslesen und umwandeln ( Base64 -> Bitmap)
                    if(request.getString("image")!=null) {
                        byte[] decodedBytes = Base64.decode(request.getString("image"), 0);
                        Bitmap clothingPicture = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        imgShowClothingPicture.setImageBitmap(clothingPicture);
                    }

                    // überprüfe ob Kleidungsattribute gesetzt sind und zeige diese gegebenfalls an
                    if(!request.getString("notes").equals("") && !request.getString("notes").isEmpty() && !request.isNull("notes"))
                        titleTextView.setText(request.getString("notes"));
                    if(!request.getString("art").equals("") && !request.getString("art").isEmpty() && !request.isNull("art"))
                        textViewArtDetail.setText(request.getString("art"));
                    if(!request.getString("size").equals("") && !request.getString("size").isEmpty() && !request.isNull("size"))
                        textViewSizeDetail.setText(request.getString("size"));
                    if(!request.getString("style").equals("") && !request.getString("style").isEmpty() && !request.isNull("style"))
                        textViewStyleDetail.setText(request.getString("style"));
                    if(!request.getString("fabric").equals("") && !request.getString("fabric").isEmpty() && !request.isNull("fabric"))
                        textViewFabricDetail.setText(request.getString("fabric"));
                    if(!request.getString("gender").equals("") && !request.getString("gender").isEmpty() && !request.isNull("gender"))
                        textViewGenderDetail.setText(request.getString("gender"));
                    if(!request.getString("color").equals("") && !request.getString("color").isEmpty() && !request.isNull("color"))
                        textViewColorDetail.setText(request.getString("color"));
                    if(!request.getString("brand").equals("") && !request.getString("brand").isEmpty() && !request.isNull("brand"))
                        textViewBrandDetail.setText(request.getString("brand"));

                    lat = request.getDouble("latitude");
                    lng = request.getDouble("longitude");


                } catch (JSONException e) {
                    showDialog("Error", "Could not process clothing data!");
                }
            }

        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnShowLocation:
                Intent mapIntent = new Intent(getApplicationContext(), ShowOnMap.class);
                mapIntent.putExtra("lng", lng);
                mapIntent.putExtra("lat", lat);
                startActivity(mapIntent);
                break;

            case R.id.btnSendRequest:
                try {
                    JSONObject clothingJson = new JSONObject(clothing);
                    if (clothingJson.getString("uId").equals(ouId)) {
                        showDialog("Error", "Choosen clothing allready belongs to you!");
                    } else {
                        JSONObject request = new JSONObject();
                        // Parteien des Requests in die zu überliefernde Struktur einfügen
                        request.put("uId", ouId);
                        request.put("ouId", clothingJson.getString("uId"));

                        // Sende request
                        Intent myIntent = new Intent(getApplicationContext(), HttpsService.class);
                        myIntent.putExtra("payload", request.toString());
                        myIntent.putExtra("method", "POST");
                        myIntent.putExtra("from", "NEWREQUEST");
                        myIntent.putExtra("url", getString(R.string.DOMAIN) + "/clothing/" + clothingJson.getString("id"));
                        startService(myIntent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void showDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(ShowClothing.this).create();
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